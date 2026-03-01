package org.codefilarete.reflection;

import java.lang.invoke.SerializedLambda;

import org.codefilarete.tool.Strings;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.codefilarete.tool.Reflections;

import static org.codefilarete.tool.Reflections.GET_SET_PREFIX_REMOVER;
import static org.codefilarete.tool.Reflections.IS_PREFIX_REMOVER;

/**
 * Mutator constructed with a method reference to a setter ({@link java.util.function.BiConsumer}).
 * THIS CLASS DOESN'T SUPPORT ANY ANONYMOUS LAMBDA, because {@link AbstractMutator} needs a good {@link #hashCode()} method which can hardly respect
 * its contract with an anonymous lambda.
 * 
 * @author Guillaume Mary
 * @see Accessors#mutatorByMethodReference(SerializableMutator)
 * @see Accessors#accessorByMethodReference(SerializableAccessor, SerializableMutator)
 */
@SuppressWarnings("squid:S2160")	// because super.equals() is based on getDescription() it doesn't need to be overridden in this class
public class MutatorByMethodReference<C, T> extends AbstractMutator<C, T> implements ValueAccessPointByMethodReference<C>, AccessorDefinitionDefiner<C> {
	
	private final SerializableMutator<C, T> methodReference;
	private final String methodReferenceSignature;
	private final String methodName;
	private final Class declaringClass;
	private final SerializedLambda serializedLambda;
	private final Class propertyType;
	private final AccessorDefinition accessorDefinition;
	
	/**
	 * @param methodReference a setter, ANY ANONYMOUS LAMBDA IS NOT SUPPORTED
	 * @throws RuntimeException with a compound {@link ReflectiveOperationException} in case of method reference dissect failure
	 */
	public MutatorByMethodReference(SerializableMutator<C, T> methodReference) {
		this.methodReference = methodReference;
		// we dissect the method reference to find out its equivalent method so we can keep its signature which is crucial for our hashCode
		serializedLambda = MethodReferences.buildSerializedLambda(methodReference);
		// our description is made of SerializedLambda's one
		methodName = serializedLambda.getImplMethodName();
		this.declaringClass = MethodReferences.giveImplementingClass(serializedLambda);
		this.propertyType = MethodReferenceCapturer.giveArgumentTypes(serializedLambda).getArgumentTypes()[0];
		this.methodReferenceSignature = Reflections.toString(declaringClass)
				.concat("::")
				.concat(methodName);	// we cut the method signature before return type because it doesn't seem necessary and ugly with arrays
		String propertyName = Reflections.onJavaBeanPropertyWrapperNameGeneric(
				this.methodName,
				this.methodName,
				GET_SET_PREFIX_REMOVER,
				GET_SET_PREFIX_REMOVER,
				IS_PREFIX_REMOVER,
				s -> s);
		propertyName = Strings.uncapitalize(propertyName);
		
		this.accessorDefinition = new AccessorDefinition(this.declaringClass, propertyName, this.propertyType);
	}
	
	public SerializableMutator<C, T> getMethodReference() {
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
		this.methodReference.set(c, t);
	}
	
	@Override
	protected String getSetterDescription() {
		return methodReferenceSignature;
	}
	
	@Override
	public Class<T> getPropertyType() {
		return propertyType;
	}
	
	@Override
	public AccessorDefinition asAccessorDefinition() {
		return accessorDefinition;
	}
}
