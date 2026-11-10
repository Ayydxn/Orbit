package me.ayydxn.orbit.natives.recording;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class NativeCallRecorder
{
	private final Set<String> invokedMethods = ConcurrentHashMap.newKeySet();

	<T> T wrap(Class<T> contractType, T target)
	{
		return contractType.cast(Proxy.newProxyInstance(contractType.getClassLoader(), new Class<?>[]{contractType},
			(proxy, method, arguments) -> this.invoke(target, method, arguments)));
	}

	Set<String> getInvokedMethods()
	{
		return Set.copyOf(this.invokedMethods);
	}

	// Every abstract method on every contract that the given backend type hands out. A contract is any interface returned by a no-argument accessor.
	static Set<String> getRequiredMethods(Class<?> backendType)
	{
		Set<String> requiredMethods = new TreeSet<>();

		for (Method accessor : backendType.getMethods())
		{
			if (accessor.getParameterCount() != 0 || !accessor.getReturnType().isInterface())
				continue;

			for (Method contractMethod : accessor.getReturnType().getMethods())
			{
				if (Modifier.isAbstract(contractMethod.getModifiers()))
					requiredMethods.add(NativeCallRecorder.getSignature(contractMethod));
			}
		}

		return requiredMethods;
	}

	Set<String> getUncoveredMethods(Class<?> backendType)
	{
		Set<String> uncoveredMethods = NativeCallRecorder.getRequiredMethods(backendType);
		uncoveredMethods.removeAll(this.invokedMethods);

		return uncoveredMethods;
	}

	static String getSignature(Method method)
	{
		return method.getDeclaringClass().getSimpleName() + "#" + method.getName() +
			Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ", "(", ")"));
	}

	private Object invoke(Object target, Method method, Object[] arguments) throws Throwable
	{
		try
		{
			Object result = method.invoke(target, arguments);

			if (method.getDeclaringClass() != Object.class)
				this.invokedMethods.add(NativeCallRecorder.getSignature(method));

			return result;
		}
		catch (InvocationTargetException exception)
		{
			Throwable cause = exception.getCause();

			// A method that couldn't run at all (unbound in FFM, or missing from the JNI registration table) hasn't been exercised, so it doesn't count as covered.
			// Any other exception means the method really ran.
			if (!(cause instanceof UnsupportedOperationException) && !(cause instanceof LinkageError))
				this.invokedMethods.add(NativeCallRecorder.getSignature(method));

			throw cause;
		}
	}
}
