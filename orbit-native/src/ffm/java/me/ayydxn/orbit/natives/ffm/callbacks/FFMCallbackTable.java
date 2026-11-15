package me.ayydxn.orbit.natives.ffm.callbacks;

import me.ayydxn.orbit.natives.ffm.utils.NativeFunctionSymbol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class FFMCallbackTable implements AutoCloseable
{
    private static final Linker LINKER = Linker.nativeLinker();
    private static final Logger LOGGER = LogManager.getLogger("Orbit - Natives (FFM)");
    private static final MethodHandle REPORT;

    static
    {
        try
        {
            REPORT = MethodHandles.lookup().findStatic(FFMCallbackTable.class, "report", MethodType.methodType(void.class, String.class, Throwable.class));
        }
        catch (ReflectiveOperationException exception)
        {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private final Arena arena;
    private final StructLayout layout;
    private final MemorySegment segment;

    private FFMCallbackTable(Arena arena, StructLayout layout, MemorySegment segment)
    {
        this.arena = arena;
        this.layout = layout;
        this.segment = segment;
    }

    public static Builder builder(MethodHandles.Lookup lookup, Object target, String structName)
    {
        return new Builder(lookup, target, structName);
    }

    public StructLayout layout()
    {
        return this.layout;
    }

    public MemorySegment segment()
    {
        return this.segment;
    }

    public void passByValueTo(String functionName)
    {
        NativeFunctionSymbol.of(functionName, FunctionDescriptor.ofVoid(this.layout)).invoke(this.segment);
    }

    @Override
    public void close()
    {
        this.arena.close();
    }

    private static void report(String callback, Throwable throwable)
    {
        LOGGER.error("Uncaught exception in native callback '{}'", callback, throwable);
    }

    public static final class Builder
    {
        private final MethodHandles.Lookup lookup;
        private final Object target;
        private final String structName;
        private final List<Entry> entries = new ArrayList<>();

        private Builder(MethodHandles.Lookup lookup, Object target, String structName)
        {
            this.lookup = lookup;
            this.target = target;
            this.structName = structName;
        }

        public Builder callback(String fieldName, String methodName, FunctionDescriptor descriptor, Supplier<?> fallback)
        {
            this.entries.add(new Entry(fieldName, methodName, descriptor, fallback));

            return this;
        }

        public Builder callback(String fieldName, String methodName, FunctionDescriptor descriptor)
        {
            return this.callback(fieldName, methodName, descriptor, null);
        }

        public FFMCallbackTable build()
        {
            Arena arena = Arena.ofShared();

            try
            {
                MemoryLayout[] fields = this.entries.stream()
                    .map(entry -> (MemoryLayout) ValueLayout.ADDRESS.withName(entry.fieldName()))
                    .toArray(MemoryLayout[]::new);

                StructLayout structLayout = MemoryLayout.structLayout(fields).withName(this.structName);
                MemorySegment memorySegment = arena.allocate(structLayout);

                for (Entry entry : this.entries)
                {
                    MethodHandle methodHandle = this.lookup.findVirtual(this.target.getClass(), entry.methodName(), entry.descriptor().toMethodType())
                        .bindTo(this.target);

                    MethodHandle guarded = Builder.guard(this.structName + "." + entry.fieldName(), methodHandle, entry.fallback());
                    MemorySegment stub = LINKER.upcallStub(guarded, entry.descriptor(), arena);

                    long offset = structLayout.byteOffset(MemoryLayout.PathElement.groupElement(entry.fieldName()));
                    memorySegment.set(ValueLayout.ADDRESS, offset, stub);
                }

                return new FFMCallbackTable(arena, structLayout, memorySegment);
            }
            catch (ReflectiveOperationException | RuntimeException exception)
            {
                arena.close();

                throw new IllegalStateException("Couldn't bind the callbacks for " + this.structName, exception);
            }
        }

        private static MethodHandle guard(String description, MethodHandle target, Supplier<?> fallback) throws ReflectiveOperationException
        {
            MethodType type = target.type();
            Class<?> returnType = type.returnType();

            MethodHandle fallbackValue;
            if (fallback != null)
            {
                fallbackValue = MethodHandles.lookup()
                    .findVirtual(Supplier.class, "get", MethodType.methodType(Object.class))
                    .bindTo(fallback)
                    .asType(MethodType.methodType(returnType));
            }
            else if (returnType == MemorySegment.class)
            {
                // A null MemorySegment returned from an upcall would take the whole JVM down, so NULL is the default here.
                fallbackValue = MethodHandles.constant(MemorySegment.class, MemorySegment.NULL);
            }
            else
            {
                fallbackValue = MethodHandles.zero(returnType);
            }

            // (Throwable, <the callback's arguments>) -> <fallback value>, which logs the throwable first.
            MethodHandle report = MethodHandles.insertArguments(REPORT, 0, description);
            MethodHandle handler = MethodHandles.collectArguments(fallbackValue, 0, report);

            handler = MethodHandles.dropArguments(handler, 1, type.parameterList());

            return MethodHandles.catchException(target, Throwable.class, handler);
        }

        private record Entry(String fieldName, String methodName, FunctionDescriptor descriptor, Supplier<?> fallback)
        {
        }
    }
}
