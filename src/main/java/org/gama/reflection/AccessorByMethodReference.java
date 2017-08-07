package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * Accessor constructed with a method reference to a getter ({@link java.util.function.Function}).
 * THIS CLASS DOESN'T SUPPORT ANY ANONYMOUS LAMBDA, because {@link AbstractAccessor} needs a good {@link #hashCode()} method which can hardly respect
 * its contract with an anonymous lambda.
 *
 * @author Guillaume Mary
 */
public class AccessorByMethodReference<C, T> extends AbstractAccessor<C, T> {
	
	private final Function<C, T> methodReference;
	private final String methodReferenceSignature;

	/**
	 * 
	 * @param methodReference
	 * @throws RuntimeException with a compound {@link ReflectiveOperationException} in case of method reference dissect failure
	 */
	public AccessorByMethodReference(SerializableFunction<C, T> methodReference) {
		this.methodReference = methodReference;
		// we dissect the method reference to find out its equivalent method so we can keep its signature which is crucial for our hashCode
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(methodReference);
		// our description is made of SerializedLambda's one
		this.methodReferenceSignature = serializedLambda.getImplClass()
				.concat(".")
				.concat(serializedLambda.getImplMethodName())
				.concat(".")
				.concat(serializedLambda.getImplMethodSignature());
	}
	
	@Override
	protected T doGet(C c) throws IllegalAccessException, InvocationTargetException {
		return methodReference.apply(c);
	}
	
	@Override
	protected String getGetterDescription() {
		return methodReferenceSignature;
	}
}
