package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.Reflections;
import org.gama.lang.exception.Exceptions;

/**
 * Will help to find {@link Method}s behind method references.
 * Only works on Serializable forms of method references due to the way {@link Method}s are shelled.
 * Each instance caches its search.
 * 
 * @author Guillaume Mary
 */
public class MethodReferenceCapturer {
	
	/** A totally arbitrary value for cache size */
	private static final int DEFAULT_CACHE_SIZE = 1000;
	
	private final Map<String, Method> cache;
	
	public MethodReferenceCapturer() {
		this(DEFAULT_CACHE_SIZE);
	}
	
	public MethodReferenceCapturer(int cacheSize) {
		cache = new LRUCache(cacheSize);
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a getter
	 * @param methodReference a method reference refering to a getter
	 * @param <I> the owning class of the method
	 * @param <O> the return type of the getter
	 * @return the found method
	 */
	public <I, O> Method findMethod(SerializableFunction<I, O> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a setter
	 * @param methodReference a method reference refering to a setter
	 * @param <I> the owning class of the method
	 * @param <O> the input type of the setter
	 * @return the found method
	 */
	public <I, O> Method findMethod(SerializableBiConsumer<I, O> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Shells a {@link SerializedLambda} to find out what {@link Method} it refers to.
	 * @param serializedLambda the {@link Method} container
	 * @return the found Method
	 */
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
	
	/**
	 * Deduces argument types from a method signature extracted from a {@link SerializedLambda} through {@link SerializedLambda#getImplMethodSignature()}
	 * @param methodSignature the result of {@link SerializedLambda#getImplMethodSignature()}
	 * @return an empty array if no argument were found, not null
	 */
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
	
	/**
	 * Very simple implementation of a Least-Recently-Used cache
	 */
	private static class LRUCache extends LinkedHashMap<String, Method> {
		
		private final int cacheSize;
		
		public LRUCache(int cacheSize) {
			this.cacheSize = cacheSize;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > cacheSize;
		}
	}
}
