package org.gama.reflection;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.gama.lang.Reflections;
import org.gama.lang.exception.Exceptions;

/**
 * Helper methods for method reference
 * 
 * @author Guillaume Mary
 */
public class MethodReferences {
	
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
		try {
			SerializedLambda serializedLambda = getSerializedLambda(methodReference);
			// Inspired by SerializedLambda#toString()
			String lambdaSignature = serializedLambda.getImplClass()
					.concat(serializedLambda.getImplMethodName())
					.concat(serializedLambda.getImplMethodSignature());
			return lambdaSignature.hashCode();
		} catch (ReflectiveOperationException e) {
			throw Exceptions.asRuntimeException(e);
		}
	}
	
	/**
	 * Same as {@link #getSerializedLambda(Serializable)} specialized for getter (method returning value without argument)
	 * @param methodReference a getter
	 * @param <T> the target instance type of the getter
	 * @return a {@link SerializedLambda}
	 * @throws ReflectiveOperationException in case of erroneous lookup
	 */
	public static <T> SerializedLambda getSerializedLambda(SerializableConsumer<T> methodReference) throws ReflectiveOperationException {
		return getSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Same as {@link #getSerializedLambda(Serializable)} specialized for setter (method without return value but with one argument)
	 * @param methodReference a setter
	 * @param <T> the target instance type of the setter
	 * @return a {@link SerializedLambda}
	 * @throws ReflectiveOperationException in case of erroneous lookup
	 */
	public static <T, U> SerializedLambda getSerializedLambda(SerializableBiConsumer<T, U> methodReference) throws ReflectiveOperationException {
		return getSerializedLambda((Serializable) methodReference);
	}
	
	/**
	 * Gives the SerializedLambda of a serializable method reference ({@link Function}, {@link java.util.function.Consumer}, etc)
	 * THIS METHOD WILL ONLY WORK WITH A METHOD REFERENCE, NOT WITH AN ANONYMOUS LAMBDA FUNCTION.
	 * This can't be enforced by signature, hence this warning.
	 * 
	 * @param methodReference the method reference to hash
	 * @return a SerializedLambda, not null
	 */
	public static SerializedLambda getSerializedLambda(Serializable methodReference) throws ReflectiveOperationException {
		// algorithm made possible thanks to https://stackoverflow.com/a/25625761
		// (https://stackoverflow.com/questions/21887358/reflection-type-inference-on-java-8-lambdas)
		Method writeReplace = Reflections.getMethod(methodReference.getClass(), "writeReplace");
		writeReplace.setAccessible(true);
		Object serializedForm = writeReplace.invoke(methodReference);
		return (SerializedLambda) serializedForm;
	}
}
