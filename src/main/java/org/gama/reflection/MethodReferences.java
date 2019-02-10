package org.gama.reflection;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.gama.lang.Reflections;
import org.gama.lang.exception.Exceptions;
import org.gama.lang.function.SerializableTriFunction;

/**
 * Helper methods for method reference
 * 
 * @author Guillaume Mary
 */
public class MethodReferences {
	
	private static final MethodReferenceCapturer SINGLETON  = new MethodReferenceCapturer();
	
	public static <A, B> String toMethodReferenceString(SerializableFunction<A, B>  methodReference) {
		Method method = SINGLETON.findMethod(methodReference);
		return toMethodReferenceString(method);
	}
	
	public static <A, B, C> String toMethodReferenceString(SerializableBiFunction<A, B, C> methodReference) {
		Method method = SINGLETON.findMethod(methodReference);
		return toMethodReferenceString(method);
	}
	
	public static <A, B, C, D> String toMethodReferenceString(SerializableTriFunction<A, B, C, D> methodReference) {
		Method method = SINGLETON.findMethod(methodReference);
		return toMethodReferenceString(method);
	}
	
	public static <A> String toMethodReferenceString(SerializableConsumer<A> methodReference) {
		Method method = SINGLETON.findMethod(methodReference);
		return toMethodReferenceString(method);
	}
	
	public static <A, B> String toMethodReferenceString(SerializableBiConsumer<A, B> methodReference) {
		Method method = SINGLETON.findMethod(methodReference);
		return toMethodReferenceString(method);
	}
	
	public static <A, B, C> String toMethodReferenceString(SerializableTriConsumer<A, B, C>  methodReference) {
		Method method = SINGLETON.findMethod(methodReference);
		return toMethodReferenceString(method);
	}
	
	public static String toMethodReferenceString(Method method) {
		return method.getDeclaringClass().getSimpleName() + "::" + method.getName();
	}
	
	/**
	 * Gives a signature of a serializable method reference ({@link Function}, {@link java.util.function.Consumer}, etc)
	 * THIS METHOD WILL ONLY WORK WITH A METHOD REFERENCE, NOT WITH AN ANONYMOUS LAMBDA FUNCTION.
	 * This can't be enforced by signature, hence this warning.
	 * Argument must be {@link Serializable} due to the algorithm used to compute hashCode.
	 * 
	 * @param methodReference the method reference to hash
	 * @return a hashcode for the method reference
	 */
	public static int hashCodeMethodReference(Serializable methodReference) {
		SerializedLambda serializedLambda = buildSerializedLambda(methodReference);
		// Inspired by SerializedLambda#toString()
		String lambdaSignature = getTargetMethodRawSignature(serializedLambda);
		return lambdaSignature.hashCode();
	}
	
	/**
	 * Gives a raw version of the method targetted by the given {@link SerializedLambda}
	 * THIS METHOD WILL ONLY WORK WITH A METHOD REFERENCE, NOT WITH AN ANONYMOUS LAMBDA FUNCTION.
	 * @param serializedLambda a method reference
	 * @return a concatenation of method class, method name, method arguments, method return type
	 */
	public static String getTargetMethodRawSignature(SerializedLambda serializedLambda) {
		return serializedLambda.getImplClass()
				.concat(serializedLambda.getImplMethodName())
				.concat(serializedLambda.getImplMethodSignature());	// contains method arguments and return type
	}
	
	/**
	 * Gives the {@link SerializedLambda} of a no-arg constructor 
	 * 
	 * @param methodReference a reference to a no-arg constructor
	 * @param <O> returned value type of the supplier
	 * @return a {@link SerializedLambda}
	 */
	public static <O> SerializedLambda buildSerializedLambda(SerializableSupplier<O> methodReference) {
		return buildSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the {@link SerializedLambda} of a getter (method returning value without argument) or a one-arg constructor 
	 * 
	 * @param methodReference a getter
	 * @param <T> the target instance type of the getter
	 * @param <O> returned value type of the getter
	 * @return a {@link SerializedLambda}
	 */
	public static <T, O> SerializedLambda buildSerializedLambda(SerializableFunction<T, O> methodReference) {
		return buildSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the {@link SerializedLambda} of a 2-args constructor (or getter with 2 arguments)
	 *
	 * @param methodReference a 2-args constructor
	 * @param <O> constructor type, or returned value type of the getter
	 * @param <I1> first argument type
	 * @param <I2> second argument type
	 * @return a {@link SerializedLambda}
	 */
	public static <I1, I2, O> SerializedLambda buildSerializedLambda(SerializableBiFunction<I1, I2, O> methodReference) {
		return buildSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the {@link SerializedLambda} of a 3-args constructor (or getter with one argument)
	 *
	 * @param methodReference a 3-args constructor
	 * @param <O> constructor type, or returned value type of the getter
	 * @param <I1> first argument type
	 * @param <I2> second argument type
	 * @param <I3> second argument type
	 * @return a {@link SerializedLambda}
	 */
	public static <I1, I2, I3, O> SerializedLambda buildSerializedLambda(SerializableTriFunction<I1, I2, I3, O> methodReference) {
		return buildSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the {@link SerializedLambda} of a setter (method without return value but with one argument)
	 * 
	 * @param methodReference a setter
	 * @param <T> target instance type of the setter
	 * @return a {@link SerializedLambda}
	 */
	public static <T> SerializedLambda buildSerializedLambda(SerializableConsumer<T> methodReference) {
		return buildSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the {@link SerializedLambda} of a 2-args setter (method without return value but with 2 arguments)
	 * 
	 * @param methodReference a 2-args setter
	 * @param <T> target instance type of the setter
	 * @param <U> argument type
	 * @return a {@link SerializedLambda}
	 */
	public static <T, U> SerializedLambda buildSerializedLambda(SerializableBiConsumer<T, U> methodReference) {
		return buildSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the {@link SerializedLambda} of a 3-args setter (method without return value but with 3 arguments)
	 * 
	 * @param methodReference a setter
	 * @param <T> target instance type of the setter
	 * @param <U> first argument type
	 * @param <V> second argument type
	 * @return a {@link SerializedLambda}
	 */
	public static <T, U, V> SerializedLambda buildSerializedLambda(SerializableTriConsumer<T, U, V> methodReference) {
		return buildSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the SerializedLambda of a serializable method reference ({@link Function}, {@link java.util.function.Consumer}, etc)
	 * THIS METHOD WILL ONLY WORK WITH A METHOD REFERENCE, NOT WITH AN ANONYMOUS LAMBDA FUNCTION.
	 * This can't be enforced by signature, hence this warning.
	 * 
	 * @param methodReference the method reference to hash
	 * @return a SerializedLambda, not null
	 */
	private static SerializedLambda buildSerializedLambda(Serializable methodReference) {
		// algorithm made possible thanks to https://stackoverflow.com/a/25625761
		// (https://stackoverflow.com/questions/21887358/reflection-type-inference-on-java-8-lambdas)
		Method writeReplace = Reflections.getMethod(methodReference.getClass(), "writeReplace");
		writeReplace.setAccessible(true);
		Object serializedForm;
		try {
			serializedForm = writeReplace.invoke(methodReference);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// Considered as will never happen
			throw Exceptions.asRuntimeException(e);
		}
		return (SerializedLambda) serializedForm;
	}
}
