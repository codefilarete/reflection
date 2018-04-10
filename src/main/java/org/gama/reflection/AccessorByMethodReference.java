package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.util.function.Function;

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
public class AccessorByMethodReference<C, T> extends AbstractAccessor<C, T> {
	
	private final Function<C, T> getter;
	private final String methodReferenceSignature;

	/**
	 * 
	 * @param getter
	 * @throws RuntimeException with a compound {@link ReflectiveOperationException} in case of method reference dissect failure
	 */
	public AccessorByMethodReference(SerializableFunction<C, T> getter) {
		this.getter = getter;
		// we dissect the method reference to find out its equivalent method so we can keep its signature which is crucial for our hashCode
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(getter);
		// our description is made of SerializedLambda's one
		this.methodReferenceSignature = serializedLambda.getImplClass()
				.concat(".")
				.concat(serializedLambda.getImplMethodName())
				// we cut the method signature before return type because it doesn't seem necessary and ugly with arrays
				.concat(Strings.head(serializedLambda.getImplMethodSignature(), ")").toString());
	}
	
	@Override
	protected T doGet(C c) {
		return getter.apply(c);
	}
	
	@Override
	protected String getGetterDescription() {
		return "method reference for " + methodReferenceSignature;
	}
}
