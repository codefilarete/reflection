package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.util.function.BiConsumer;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * Mutator constructed with a method reference to a setter ({@link java.util.function.BiConsumer}).
 * THIS CLASS DOESN'T SUPPORT ANY ANONYMOUS LAMBDA, because {@link AbstractMutator} needs a good {@link #hashCode()} method which can hardly respect
 * its contract with an anonymous lambda.
 * 
 * @author Guillaume Mary
 * @see Accessors#mutatorByMethodReference(SerializableBiConsumer)
 * @see Accessors#accessorByMethodReference(SerializableFunction, SerializableBiConsumer)
 */
public class MutatorByMethodReference<C, T> extends AbstractMutator<C, T> {
	
	private final BiConsumer<C, T> methodReference;
	private final String methodReferenceSignature;
	
	/**
	 * 
	 * @param methodReference a setter, ANY ANONYMOUS LAMBDA IS NOT SUPPORTED
	 * @throws RuntimeException with a compound {@link ReflectiveOperationException} in case of method reference dissect failure
	 */
	public MutatorByMethodReference(SerializableBiConsumer<C, T> methodReference) {
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
	protected void doSet(C c, T t) {
		this.methodReference.accept(c, t);
	}
	
	@Override
	protected String getSetterDescription() {
		return methodReferenceSignature;
	}
}
