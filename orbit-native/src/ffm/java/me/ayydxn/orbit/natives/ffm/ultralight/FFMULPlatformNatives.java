package me.ayydxn.orbit.natives.ffm.ultralight;

import me.ayydxn.orbit.natives.ffm.callbacks.FFMCallbackTable;
import me.ayydxn.orbit.natives.ultralight.ULPlatformNatives;
import me.ayydxn.orbit.ultralight.font.ULFontFile;
import me.ayydxn.orbit.ultralight.platform.ULFileSystem;
import me.ayydxn.orbit.ultralight.platform.ULFontLoader;
import me.ayydxn.orbit.ultralight.platform.ULLogger;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;

import static java.lang.foreign.ValueLayout.*;

@SuppressWarnings("resource")
public final class FFMULPlatformNatives implements ULPlatformNatives
{
    private final FFMULPlatformUtils support = new FFMULPlatformUtils();

    private LoggerCallbacks loggerCallbacks;
    private FileSystemCallbacks fileSystemCallbacks;
    private FontLoaderCallbacks fontLoaderCallbacks;

    @Override
    public synchronized void setLogger(ULLogger logger)
    {
        LoggerCallbacks loggerCallbacks = new LoggerCallbacks(logger, this.support);
        loggerCallbacks.table().passByValueTo("ulPlatformSetLogger");

        FFMULPlatformNatives.closeIfPresent(this.loggerCallbacks);

        this.loggerCallbacks = loggerCallbacks;
    }

    @Override
    public synchronized void setFileSystem(ULFileSystem fileSystem)
    {
        FileSystemCallbacks fileSystemCallbacks = new FileSystemCallbacks(fileSystem, this.support);
        fileSystemCallbacks.table().passByValueTo("ulPlatformSetFileSystem");

        FFMULPlatformNatives.closeIfPresent(this.fileSystemCallbacks);

        this.fileSystemCallbacks = fileSystemCallbacks;
    }

    @Override
    public synchronized void setFontLoader(ULFontLoader fontLoader)
    {
        FontLoaderCallbacks fontLoaderCallbacks = new FontLoaderCallbacks(fontLoader, this.support);
        fontLoaderCallbacks.table().passByValueTo("ulPlatformSetFontLoader");

        FFMULPlatformNatives.closeIfPresent(this.fontLoaderCallbacks);

        this.fontLoaderCallbacks = fontLoaderCallbacks;
    }

    @Override
    public synchronized void shutdown()
    {
        FFMULPlatformNatives.closeIfPresent(this.loggerCallbacks);
        FFMULPlatformNatives.closeIfPresent(this.fileSystemCallbacks);
        FFMULPlatformNatives.closeIfPresent(this.fontLoaderCallbacks);

        this.loggerCallbacks = null;
        this.fileSystemCallbacks = null;
        this.fontLoaderCallbacks = null;
    }

    private static void closeIfPresent(PlatformCallbacks callbacks)
    {
        if (callbacks != null)
            callbacks.table().close();
    }

    private static final class LoggerCallbacks implements PlatformCallbacks
    {
        private final ULLogger logger;
        private final FFMULPlatformUtils support;
        private final FFMCallbackTable table;

        private LoggerCallbacks(ULLogger logger, FFMULPlatformUtils platformUtils)
        {
            this.logger = logger;
            this.support = platformUtils;
            this.table = FFMCallbackTable.builder(MethodHandles.lookup(), this, "ULLogger")
                .callback("log_message", "logMessage", FunctionDescriptor.ofVoid(JAVA_INT, ADDRESS))
                .build();
        }

        @Override
        public FFMCallbackTable table()
        {
            return this.table;
        }

        private void logMessage(int level, MemorySegment message)
        {
            this.logger.logMessage(this.support.readString(message), ULLogger.Level.createFromNativeValue(level));
        }
    }

    private static final class FileSystemCallbacks implements PlatformCallbacks
    {
        private final ULFileSystem fileSystem;
        private final FFMULPlatformUtils support;
        private final FFMCallbackTable table;

        private FileSystemCallbacks(ULFileSystem fileSystem, FFMULPlatformUtils platformUtils)
        {
            this.fileSystem = fileSystem;
            this.support = platformUtils;
            this.table = FFMCallbackTable.builder(MethodHandles.lookup(), this, "ULFileSystem")
                .callback("file_exists", "fileExists", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS))
                .callback("get_file_mime_type", "getFileMimeType", FunctionDescriptor.of(ADDRESS, ADDRESS), () -> platformUtils.createString(""))
                .callback("get_file_charset", "getFileCharset", FunctionDescriptor.of(ADDRESS, ADDRESS), () -> platformUtils.createString(""))
                .callback("open_file", "openFile", FunctionDescriptor.of(ADDRESS, ADDRESS))
                .build();
        }

        @Override
        public FFMCallbackTable table()
        {
            return this.table;
        }

        private boolean fileExists(MemorySegment path)
        {
            return this.fileSystem.fileExists(this.support.readString(path));
        }

        private MemorySegment getFileMimeType(MemorySegment path)
        {
            String mimeType = this.fileSystem.getFileMimeType(this.support.readString(path));

            return this.support.createString(mimeType != null ? mimeType : "application/unknown");
        }

        private MemorySegment getFileCharset(MemorySegment path)
        {
            String charset = this.fileSystem.getFileCharset(this.support.readString(path));

            return this.support.createString(charset != null ? charset : "utf-8");
        }

        private MemorySegment openFile(MemorySegment path)
        {
            byte[] data = this.fileSystem.openFile(this.support.readString(path));

            return data != null ? this.support.createBuffer(data) : MemorySegment.NULL;
        }
    }

    private static final class FontLoaderCallbacks implements PlatformCallbacks
    {
        private final ULFontLoader fontLoader;
        private final FFMULPlatformUtils support;
        private final FFMCallbackTable table;

        private FontLoaderCallbacks(ULFontLoader fontLoader, FFMULPlatformUtils platformUtils)
        {
            this.fontLoader = fontLoader;
            this.support = platformUtils;
            this.table = FFMCallbackTable.builder(MethodHandles.lookup(), this, "ULFontLoader")
                .callback("get_fallback_font", "getFallbackFont", FunctionDescriptor.of(ADDRESS), () -> platformUtils.createString(""))
                .callback("get_fallback_font_for_characters", "getFallbackFontForCharacters",
                    FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT, JAVA_BOOLEAN), () -> platformUtils.createString(""))
                .callback("load", "load", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT, JAVA_BOOLEAN))
                .build();
        }

        @Override
        public FFMCallbackTable table()
        {
            return this.table;
        }

        private MemorySegment getFallbackFont()
        {
            String fallbackFont = this.fontLoader.getFallbackFont();

            return this.support.createString(fallbackFont != null ? fallbackFont : "Arial");
        }

        private MemorySegment getFallbackFontForCharacters(MemorySegment characters, int weight, boolean italic)
        {
            String fallbackFont = this.fontLoader.getFallbackFontForCharacters(this.support.readString(characters), weight, italic);

            return this.support.createString(fallbackFont != null ? fallbackFont : "Arial");
        }

        private MemorySegment load(MemorySegment family, int weight, boolean italic)
        {
            ULFontFile fontFile = this.fontLoader.load(this.support.readString(family), weight, italic);
            if (fontFile == null)
                return MemorySegment.NULL; // Null is a valid return value and makes Ultralight fall back to another font.

            if (fontFile.getSource() == ULFontFile.Source.FILE_PATH)
                return this.support.createFontFileFromFilePath(fontFile.getFilePath());

            return this.support.createFontFileFromBuffer(this.support.createBuffer(fontFile.getBuffer()));
        }
    }

    private interface PlatformCallbacks
    {
        FFMCallbackTable table();
    }
}
