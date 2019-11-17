package org.gama.reflection;

import java.lang.invoke.SerializedLambda;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.Reflections;

/**
 * Mutator constructed with a method reference to a setter ({@link java.util.function.BiConsumer}).
 * THIS CLASS DOESN'T SUPPORT ANY ANONYMOUS LAMBDA, because {@link AbstractMutator} needs a good {@link #hashCode()} method which can hardly respect
 * its contract with an anonymous lambda.
 * 
 * @author Guillaume Mary
 * @see Accessors#mutatorByMethodReference(SerializableBiConsumer)
 * @see Accessors#accessorByMethodReference(SerializableFunction, SerializableBiConsumer)
 */
@SuppressWarnings("squid:S2160")	// because super.equals() is based on getDescription() it doesn't need to be overriden in this class 
public class MutatorByMethodReference<C, T> extends AbstractMutator<C, T> implements ValueAccessPointByMethodReference {
	
	private final SerializableBiConsumer<C, T> methodReference;
	private final String methodReferenceSignature;
	private final String methodName;
	private final Class declaringClass;
	private final SerializedLambda serializedLambda;
	private final Class propertyType;
	
	/**
	 * 
	 * @param methodReference a setter, ANY ANONYMOUS LAMBDA IS NOT SUPPORTED
	 * @throws RuntimeException with a compound {@link ReflectiveOperationException} in case of method reference dissect failure
	 */
	public MutatorByMethodReference(SerializableBiConsumer<C, T> methodReference) {
		this.methodReference = methodReference;
		// we dissect the method reference to find out its equivalent method so we can keep its signature which is crucial for our hashCode
		serializedLambda = MethodReferences.buildSerializedLambda(methodReference);
		// our description is made of SerializedLambda's one
		methodName = serializedLambda.getImplMethodName();
		String implementationClass = serializedLambda.getImplClass().replace('/', '.');
		this.declaringClass = Reflections.forName(implementationClass);
		this.propertyType = MethodReferenceCapturer.giveArgumentTypes(serializedLambda).getArgumentTypes()[0];
		this.methodReferenceSignature = implementationClass
				.concat(".")
				.concat(methodName)
				.concat(".")
				.concat(serializedLambda.getImplMethodSignature());
	}
	
	public SerializableBiConsumer<C, T> getMethodReference() {
		return methodReference;
	}
	
	@Override
	public String getMethodName() {
		return methodName;
	}
	
	@Override
	public Class<C> getDeclaringClass() {
		return declaringClass;
	}
	
	@Override
	public SerializedLambda getSerializedLambda() {
		return serializedLambda;
	}
	
	@Override
	protected void doSet(C c, T t) {
		this.methodReference.accept(c, t);
	}
	
	@Override
	protected String getSetterDescription() {
		return methodReferenceSignature;
	}
	
	@Override
	public Class<T> getPropertyType() {
		return propertyType;
	}
	
	/**
	 * Overriden because parent toString() is based on getter description which is the one in the lambda and is not compact nor easy to read
	 * @return the refered method in the form DeclaringClass::methodName
	 */
	@Override
	public String toString() {
		return Reflections.toString(declaringClass) + "::" + methodName;
	}
}
