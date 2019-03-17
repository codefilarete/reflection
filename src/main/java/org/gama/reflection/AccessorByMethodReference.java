package org.gama.reflection;

import java.lang.invoke.SerializedLambda;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.Strings;

/**
 * Accessor constructed with a method reference to a getter ({@link java.util.function.Function}).
 * THIS CLASS DOESN'T SUPPORT ANY ANONYMOUS LAMBDA, because {@link AbstractAccessor} needs a good {@link #hashCode()} method which can hardly respect
 * its contract with an anonymous lambda.
 *
 * @author Guillaume Mary
 * @see Accessors#accessorByMethodReference(SerializableFunction)
 * @see Accessors#accessorByMethodReference(SerializableFunction, SerializableBiConsumer) 
 */
@SuppressWarnings("squid:S2160")	// because super.equals() is based on getDescription() it doesn't need to be overriden in this class
public class AccessorByMethodReference<C, T> extends AbstractAccessor<C, T> implements ValueAccessPointByMethodReference {
	
	private final SerializableFunction<C, T> methodReference;
	private final String methodReferenceSignature;
	private final String methodName;
	private final String declaringClass;
	private final SerializedLambda serializedLambda;
	
	/**
	 * 
	 * @param methodReference a getter
	 * @throws RuntimeException with a compound {@link ReflectiveOperationException} in case of method reference dissect failure
	 */
	public AccessorByMethodReference(SerializableFunction<C, T> methodReference) {
		this.methodReference = methodReference;
		// we dissect the method reference to find out its equivalent method so we can keep its signature which is crucial for our hashCode
		serializedLambda = MethodReferences.buildSerializedLambda(methodReference);
		// our description is made of SerializedLambda's one
		methodName = serializedLambda.getImplMethodName();
		declaringClass = serializedLambda.getImplClass().replace('/', '.');
		this.methodReferenceSignature = declaringClass
				.concat(".")
				.concat(methodName)
				// we cut the method signature before return type because it doesn't seem necessary and ugly with arrays
				.concat(Strings.head(serializedLambda.getImplMethodSignature(), ")").toString());
	}
	
	public SerializableFunction<C, T> getMethodReference() {
		return methodReference;
	}
	
	@Override
	public String getMethodName() {
		return this.methodName;
	}
	
	@Override
	public String getDeclaringClass() {
		return declaringClass;
	}
	
	@Override
	public SerializedLambda getSerializedLambda() {
		return serializedLambda;
	}
	
	@Override
	protected T doGet(C c) {
		return methodReference.apply(c);
	}
	
	@Override
	protected String getGetterDescription() {
		return "method reference for " + methodReferenceSignature;
	}
}
