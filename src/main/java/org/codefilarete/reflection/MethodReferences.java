package org.codefilarete.reflection;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Strings;
import org.codefilarete.tool.exception.Exceptions;
import org.codefilarete.tool.function.SerializableTriConsumer;
import org.codefilarete.tool.function.SerializableTriFunction;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;

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
	
	public static <A, B, C> String toMethodReferenceString(SerializableTriConsumer<A, B, C> methodReference) {
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
	 * Gives a raw version of the method targeted by the given {@link SerializedLambda}
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
	 * Left public for cases not taking into account by other buildSerializedLambda(..) methods.
	 * 
	 * @param methodReference the method reference to hash
	 * @return a SerializedLambda, not null
	 */
	public static SerializedLambda buildSerializedLambda(Serializable methodReference) {
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
	
	/**
	 * Gives the class that implements the method referenced by the given lambda.
	 * Example : if String::length is given, then CharSequence.class is returned because CharSequence defines length()
	 *
	 * @param serializedLambda a lambda representing a method reference, not any anonymous lambda
	 * @return the class that implements method referenced by the given lambda
	 * @see #giveInstantiatedClass(SerializedLambda)
	 */
	public static Class giveImplementingClass(SerializedLambda serializedLambda) {
		String implementationClass = serializedLambda.getImplClass().replace('/', '.');
		return Reflections.forName(implementationClass);
	}
	
	/**
	 * Gives the class that is directly referenced by the given method reference lambda.
	 * Example : if String::length is given, then String.class is returned, not CharSequence.class whereas
	 * CharSequence defines length()
	 *
	 * @param serializedLambda a lambda representing a method reference, not any anonymous lambda
	 * @return the class that is directly referenced by the given method reference lambda.
	 * @see #giveImplementingClass(SerializedLambda)
	 */
	public static Class giveInstantiatedClass(SerializedLambda serializedLambda) {
		// We don't use getImplClass() because it less accurate when lambda is a method reference pointing to a method
		// subclass while method is defined in an upper class : getImplClass points to class defining method (the parent one)
		// while getInstantiatedMethodType gives user-pointed method
		String instantiatedMethodType = serializedLambda.getInstantiatedMethodType();
		// it has the following structure:
		// parenthesis implementing_class arguments_types parenthesis return_type
		int closingParenthesisIndex = instantiatedMethodType.indexOf(')');
		String implementingClassAndArgumentsTypes = instantiatedMethodType.substring(1, closingParenthesisIndex);
		
		// NB : we need ";" to be kept because it's in String representing type : see Class.getName()
		List<String> types = Strings.split(implementingClassAndArgumentsTypes, ';', true);
		return Reflections.forName(types.get(0).replace("/", "."));
	}
}
