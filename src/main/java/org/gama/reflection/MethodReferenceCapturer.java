package org.gama.reflection;

import javax.annotation.Nonnull;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.gama.lang.Reflections;
import org.gama.lang.exception.Exceptions;
import org.gama.lang.function.SerializableTriConsumer;
import org.gama.lang.function.SerializableTriFunction;

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
	
	private final Map<String, Executable> cache;
	
	public MethodReferenceCapturer() {
		this(DEFAULT_CACHE_SIZE);
	}
	
	public MethodReferenceCapturer(int cacheSize) {
		cache = new LRUCache(cacheSize);
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a getter
	 * 
	 * @param methodReference a method reference refering to a getter
	 * @param <I> the owning class of the method
	 * @param <O> the return type of the getter
	 * @return the found method
	 */
	public <I, O> Method findMethod(SerializableFunction<I, O> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a getter with one argument
	 * 
	 * @param methodReference a method reference refering to a 1-arg getter
	 * @param <I> the owning class of the method
	 * @param <A1> the argument type
	 * @param <O> the return type of the getter
	 * @return the found method
	 */
	public <I, A1, O> Method findMethod(SerializableBiFunction<I, A1, O> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a getter with two arguments
	 *
	 * @param methodReference a method reference refering to a 2-args getter
	 * @param <I> the owning class of the method
	 * @param <A1> the first argument type
	 * @param <A2> the second argument type
	 * @param <O> the return type of the getter
	 * @return the found method
	 */
	public <I, A1, A2, O> Method findMethod(SerializableTriFunction<I, A1, A2, O> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a no-arg no-return function
	 *
	 * @param methodReference a method reference refering to a no-arg no-return function
	 * @param <I> the owning class of the method
	 * @return the found method
	 */
	public <I> Method findMethod(SerializableConsumer<I> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a setter
	 * 
	 * @param methodReference a method reference refering to a setter
	 * @param <I> the owning class of the method
	 * @param <A1> the input type of the setter
	 * @return the found method
	 */
	public <I, A1> Method findMethod(SerializableBiConsumer<I, A1> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Method} of a setter with two arguments
	 *
	 * @param methodReference a method reference refering to a 2-args setter
	 * @param <I> the owning class of the method
	 * @param <A1> the first argument type
	 * @param <A2> the first argument type
	 * @return the found method
	 */
	public <I, A1, A2> Method findMethod(SerializableTriConsumer<I, A1, A2> methodReference) {
		return findMethod(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Constructor} of constructor reference
	 *
	 * @param methodReference a method reference refering to a no-arg constructor
	 * @param <O> the owning class of the constructor which is also the return/instance type
	 * @return the found method
	 */
	public <O> Constructor findConstructor(SerializableSupplier<O> methodReference) {
		return findConstructor(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Constructor} of 1-arg constructor reference
	 *
	 * @param methodReference a method reference refering to a 1-arg constructor
	 * @param <O> the owning class of the constructor which is also the return/instance type
	 * @param <A1> the first arugment type
	 * @return the found method
	 */
	public <O, A1> Constructor findConstructor(SerializableFunction<A1, O> methodReference) {
		return findConstructor(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Looks for the equivalent {@link Constructor} of 2-args constructor reference
	 *
	 * @param methodReference a method reference refering to a 2-args constructor
	 * @param <O> the owning class of the constructor which is also the return/instance type
	 * @param <A1> the second arugment type
	 * @param <A2> the first arugment type
	 * @return the found method
	 */
	public <A1, A2, O> Constructor findConstructor(SerializableBiFunction<A1, A2, O> methodReference) {
		return findConstructor(MethodReferences.buildSerializedLambda(methodReference));
	}
	
	/**
	 * Shells a {@link SerializedLambda} to find out what {@link Method} it refers to.
	 * @param serializedLambda the {@link Method} container
	 * @return the found Method
	 */
	public Method findMethod(SerializedLambda serializedLambda) {
		String targetMethodRawSignature = MethodReferences.getTargetMethodRawSignature(serializedLambda);
		return (Method) findExecutable(serializedLambda, targetMethodRawSignature);
	}
	
	/**
	 * Shells a {@link SerializedLambda} to find out what {@link Method} it refers to.
	 * @param serializedLambda the {@link Method} container
	 * @return the found Method
	 */
	public Constructor findConstructor(SerializedLambda serializedLambda) {
		String targetMethodRawSignature = MethodReferences.getTargetMethodRawSignature(serializedLambda);
		return handleConstructorCast(findExecutable(serializedLambda, targetMethodRawSignature));
	}
	
	private Constructor handleConstructorCast(Executable executable) {
		try {
			return (Constructor) executable;
		} catch (ClassCastException e) {
			// Throwing a better suited exception to prevent loss of time to debug ... according to experience
			if ("java.lang.reflect.Method cannot be cast to java.lang.reflect.Constructor".equals(e.getMessage())) {
				Method method = (Method) executable;
				Class returnType = method.getReturnType();
				if (!Modifier.isStatic(returnType.getModifiers())) {
					throw new UnsupportedOperationException("Capturing by reference a non-static inner classes constructor is not supported, make "
							+ Reflections.toString(returnType) + " to be static or an outer class of " + Reflections.toString(returnType.getEnclosingClass()));
				} else {
					// case here is a private constructor of a static inner class : for any (not really understood) reason, in such a case some bytecode
					// wraps the call to the constructor so the serialized lambda is no more a direct access to it (hence the ClassCastException),
					// but the constructor can be deduced from method return type
					Constructor constructor = Reflections.getConstructor(method.getReturnType(), method.getParameterTypes());
					Reflections.ensureAccessible(constructor);
					return constructor;
				}
			} else {
				throw e;
			}
		}
	}
	
	/**
	 * Find any {@link Executable} behind the given {@link SerializedLambda}
	 * 
	 * @param serializedLambda any non null {@link SerializedLambda}
	 * @param targetExecutableRawSignature the executable signature to be used for caching
	 * @return the {@link Executable} in the given {@link SerializedLambda}
	 */
	private Executable findExecutable(SerializedLambda serializedLambda, String targetExecutableRawSignature) {
		return cache.computeIfAbsent(targetExecutableRawSignature, s -> {
			Class<?> clazz;
			try {
				clazz = Class.forName(serializedLambda.getImplClass().replace("/", "."));
			} catch (ClassNotFoundException e) {
				// Should not happen since the class was Serialized so it exists !
				throw Exceptions.asRuntimeException(e);
			}
			// looking for argument types
			String methodSignature = serializedLambda.getImplMethodSignature();
			Class[] argsClasses;
			try {
				argsClasses = giveArgumentTypes(methodSignature);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Can't find method reference for "
						+ serializedLambda.getImplClass() + "." + serializedLambda.getImplMethodName(), e);
			}
			// method or constructor case ?
			if (serializedLambda.getImplMethodName().equals("<init>")) {
				return Reflections.getConstructor(clazz, argsClasses);
			} else {
				return Reflections.findMethod(clazz, serializedLambda.getImplMethodName(), argsClasses);
			}
		});
	}
	
	/**
	 * Deduces argument types from a method signature extracted from a {@link SerializedLambda} through {@link SerializedLambda#getImplMethodSignature()}
	 * @param methodSignature the result of {@link SerializedLambda#getImplMethodSignature()}
	 * @return an empty array if no argument were found, not null
	 */
	@Nonnull
	Class[] giveArgumentTypes(String methodSignature) throws ClassNotFoundException {
		Class[] argsClasses;
		int closeArgsIndex = methodSignature.indexOf(')');
		if (closeArgsIndex != 1) {
			String argumentTypeSignature = methodSignature.substring(1, closeArgsIndex);
			argsClasses = new ArgumentTypeSignatureParser(argumentTypeSignature).parse();
		} else {
			argsClasses = new Class[0];
		}
		return argsClasses;
	}
	
	/**
	 * Very simple implementation of a Least-Recently-Used cache
	 */
	static class LRUCache extends LinkedHashMap<String, Executable> {
		
		private final int cacheSize;
		
		LRUCache(int cacheSize) {
			this.cacheSize = cacheSize;
		}
		
		/**
		 * Implemented to remove the given entry if cache size overflows : not depending on entry, only on cache size
		 * @param eldest the least recently added entry (computed by caller)
		 * @return true if current cache size overflows expected cache size (given at construction time)
		 */
		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > cacheSize;
		}
	}
	
	/**
	 * Small class aimed at parsing arguments types 
	 */
	private static class ArgumentTypeSignatureParser {
		
		private int currPos = 0;
		private String className;
		private int typeDefSize;
		private final char[] signatureChars;
		
		private ArgumentTypeSignatureParser(String signature) {
			this.signatureChars = signature.toCharArray();
		}
		
		Class[] parse() throws ClassNotFoundException {
			List<Class> result = new ArrayList<>(5);
			while(currPos < signatureChars.length) {
				// 4 cases to take into account : a combination of 2
				// - object vs primitive type : object starts with 'L' followed by class name which packages are separated with /, primitive is only 1 char
				// - arrays are distincted by a '[' prefix
				boolean typeIsArray = signatureChars[currPos] == '[';
				boolean isObjectType = signatureChars[currPos + (typeIsArray ? /* add 1 for '[' */ 1 : 0)] == 'L';
				lookAHeadForType(isObjectType, typeIsArray);
				currPos += typeDefSize;
				result.add(Reflections.forName(className));
			}
			return result.toArray(new Class[0]);
		}
		
		/**
		 * Consumes signature characters to find out object or primitive type definition.
		 * Updates className and typeDefSize fields.
		 * 
		 * @param isObjectType indicates if type is an object one (vs primitive) : crucial for parsing
		 * @param isArray indicates if type is an array : needed to compute typeDefSize in primitive case
		 */
		private void lookAHeadForType(boolean isObjectType, boolean isArray) {
			if (isObjectType) {
				// Object type ends with ';'
				int typeDefEnd = currPos;
				while(signatureChars[++typeDefEnd] != ';') {
					// iteration is already done in condition, so ther's nothing to do here
				}
				typeDefSize = typeDefEnd - currPos + 1;
				className = cleanClassName(new String(signatureChars, currPos, typeDefSize));
			} else {
				typeDefSize = 1 + (isArray ? /* add 1 for '[' */ 1 : 0);
				className = new String(signatureChars, currPos, typeDefSize);
			}
		}
		
		/**
		 * Cleans the given class name to comply with {@link Reflections#forName(String)} expected input
		 * @param objectClass a class name with '/' and ;
		 * @return
		 */
		private static String cleanClassName(String objectClass) {
			if (objectClass.charAt(0) == 'L') {
				// class name : starts with 'L' and ends with ';' : we remove them
				objectClass = objectClass.substring(1, objectClass.length() - 1);
			}
			// other case [L...; (object array) is accepted without modification
			objectClass = objectClass.replace('/', '.');
			return objectClass;
		}
		
	}
}
