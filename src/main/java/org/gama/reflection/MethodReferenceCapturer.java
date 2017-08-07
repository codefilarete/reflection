package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.Reflections;
import org.gama.lang.exception.Exceptions;

/**
 * Will help to find {@link Method}s behind method references.
 * Only works on Serializable forms of method references due to the way {@link Method}s are shelled.
 * 
 * @author Guillaume Mary
 */
public class MethodReferenceCapturer {
	
	/** A totally arbitrary value for cache size */
	private static final int DEFAULT_CACHE_SIZE = 1000;
	
	private final int cacheSize;
	
	private Map<String, Method> cache = new LinkedHashMap<String, Method>() {
		@Override
		protected boolean removeEldestEntry(Entry eldest) {
			return size() > cacheSize;
		}
	};
	
	public MethodReferenceCapturer() {
		this(DEFAULT_CACHE_SIZE);
	}
	
	public MethodReferenceCapturer(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	public <I, O> Method findMethod(SerializableFunction<I, O> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	public <I, O> Method findMethod(SerializableBiConsumer<I, O> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	public Method findMethod(SerializedLambda serializedLambda) {
		String targetMethodRawSignature = MethodReferences.getTargetMethodRawSignature(serializedLambda);
		return cache.computeIfAbsent(targetMethodRawSignature, s -> {
			Class<?> clazz;
			try {
				clazz = Class.forName(serializedLambda.getImplClass().replace("/", "."));
			} catch (ClassNotFoundException e) {
				// Should not happen since the class was Serialized so it exists !
				throw Exceptions.asRuntimeException(e);
			}
			// looking for argument types
			String methodSignature = serializedLambda.getImplMethodSignature();
			Class[] argsClasses = giveArgumentTypes(methodSignature);
			return Reflections.findMethod(clazz, serializedLambda.getImplMethodName(), argsClasses);
		});
	}
	
	private Class[] giveArgumentTypes(String methodSignature) {
		Class[] argsClasses;
		int closeArgsIndex = methodSignature.indexOf(")");
		if (closeArgsIndex != 1) {
			String[] argsTypes = methodSignature.substring(1, closeArgsIndex).split(",");
			argsClasses = new Class[argsTypes.length];
			for (int i = 0, argsTypesLength = argsTypes.length; i < argsTypesLength; i++) {
				String argsType = argsTypes[i];
				try {
					argsClasses[i] = Reflections.forName(argsType);
				} catch (ClassNotFoundException e) {
					// should not happen
					throw new RuntimeException(e);
				}
			}
		} else {
			argsClasses = new Class[0];
		}
		return argsClasses;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		// we clear to avoid any memory leak due to methods as values
		this.cache.clear();
	}
}
