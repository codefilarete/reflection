package org.gama.reflection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.gama.lang.Reflections;
import org.gama.lang.Reflections.MemberNotFoundException;

/**
 * @author Guillaume Mary
 */
public class AccessorByMethod<C, T> extends AbstractAccessor<C, T>
		implements AccessorByMember<C, T, Method>, IReversibleAccessor<C, T>, ValueAccessPointByMethod {
	
	private final Method getter;
	
	private final Object[] methodParameters;
	
	public AccessorByMethod(Method getter) {
		this(getter, new Object[getter.getParameterTypes().length]);
	}
	
	public AccessorByMethod(Method getter, Object ... arguments) {
		this.getter = getter;
		Reflections.ensureAccessible(getter);
		this.methodParameters = arguments;
	}
	
	/**
	 * Constructor for a getter-equivalent method
	 * 
	 * @param declaringClass type that declares the method
	 * @param getterName name of the accessor
	 */
	public AccessorByMethod(Class<C> declaringClass, String getterName) {
		this(Reflections.getMethod(declaringClass, getterName));
	}
	
	/**
	 * Constructor for a getter that already has an argument value
	 * 
	 * @param declaringClass type that declares the method
	 * @param methodName a one-arg method
	 * @param inputType argument type of the method
	 * @param input the argument value
	 * @param <I> argument type
	 */
	public <I> AccessorByMethod(Class<C> declaringClass, String methodName, Class<I> inputType, I input) {
		this(Reflections.getMethod(declaringClass, methodName, inputType), input);
	}
	
	@Override
	public Method getGetter() {
		return getter;
	}
	
	@Override
	public Method getMethod() {
		return getGetter();
	}
	
	@Override
	public T get(C c) {
		return get(c, methodParameters);
	}
	
	/**
	 * Sets parameters
	 * 
	 * @param values expecting to have at least 1 element
	 * @return this
	 */
	public AccessorByMethod<C, T> setParameters(Object ... values) {
		for (int i = 0; i < values.length; i++) {
			setParameter(i, values[i]);
		}
		return this;
	}
	
	/**
	 * Sets parameters at index
	 *
	 * @param index the parameter index to be set
	 * @param value value of the parameter
	 * @return this
	 */
	public AccessorByMethod<C, T> setParameter(int index, Object value) {
		this.methodParameters[index] = value;
		return this;
	}
	
	/**
	 * @param index expected to be positive and in parameters size bound
	 * @return value of the parameter at index
	 */
	public Object getParameter(@Nonnegative int index) {
		return methodParameters[index];
	}
	
	/**
	 * Applies this getter on the given bean, with params.
	 * Parameters already set with {@link #setParameter(int, Object)} or {@link #setParameters(Object...)} won't be used.
	 * 
	 * @param c an Object
	 * @param params arguments
	 * @return result of the called method
	 */
	public T get(@Nonnull C c, Object ... params) {
		try {
			return doGet(c, params);
		} catch (ReflectiveOperationException | RuntimeException t) {
			handleException(t, c, params);
			// shouldn't happen
			return null;
		}
	}
	
	@Override
	// NB: set final to force override doGet(C, Object ...) and so to avoid mistake
	protected final T doGet(C c) throws IllegalAccessException, InvocationTargetException {
		return doGet(c, new Object[0]);
	}
	
	protected T doGet(C c, Object ... args) throws IllegalAccessException, InvocationTargetException {
		return (T) getGetter().invoke(c, args);
	}
	
	@Override
	protected String getGetterDescription() {
		return Reflections.toString(getGetter());
	}
	
	/**
	 * @return a mutator based on the equivalent setter method if exists, else based on field direct access
	 * @throws NonReversibleAccessor if neither setter nor field could be found
	 */
	@Override
	public MutatorByMember<C, T, ? extends Member> toMutator() {
		Class<?> declaringClass = getGetter().getDeclaringClass();
		String propertyName = Reflections.propertyName(getGetter());
		MutatorByMethod<C, T> mutatorByMethod = Accessors.mutatorByMethod((Class<C>) declaringClass, propertyName, (Class<T>) getGetter().getReturnType());
		if (mutatorByMethod == null) {
			try {
				return Accessors.mutatorByField(declaringClass, propertyName);
			} catch (MemberNotFoundException e) {
				throw new NonReversibleAccessor("Can't find a mutator for " + Reflections.toString(getGetter()), e);
			}
		} else {
			return mutatorByMethod;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		// We base our implementation on the getter String because a setAccessible() call on the member changes its internal state
		// and I don't think it sould be taken into account for comparison
		// We could base it on getGetterDescription() but it requires more computation
		return this == other || 
				(other instanceof AccessorByMethod
						&& getGetter().toString().equals(((AccessorByMethod) other).getGetter().toString())
						&& Arrays.equals(methodParameters, ((AccessorByMethod) other).methodParameters));
	}
	
	@Override
	public int hashCode() {
		return 31 * getGetter().hashCode() + Arrays.hashCode(methodParameters);
	}
}
