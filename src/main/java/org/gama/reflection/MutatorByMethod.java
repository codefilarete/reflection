package org.gama.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.gama.lang.Reflections;
import org.gama.lang.Reflections.MemberNotFoundException;

/**
 * @author mary
 */
public class MutatorByMethod<C, T> extends AbstractMutator<C, T>
		implements MutatorByMember<C, T, Method>, IReversibleMutator<C, T>, ValueAccessPointByMethod {
	
	private final Method setter;
	
	protected final Object[] methodParameters;
	
	public MutatorByMethod(Method setter) {
		super();
		this.setter = setter;
		Reflections.ensureAccessible(setter);
		int parametersLength = this.setter.getParameterTypes().length;
		// method parameters instanciation to avoid extra array instanciation on each set(..) call 
		this.methodParameters = new Object[parametersLength];
	}
	
	public MutatorByMethod(Method setter, Object ... arguments) {
		this.setter = setter;
		Reflections.ensureAccessible(setter);
		this.methodParameters = arguments;
	}
	
	/**
	 * Constructor for a setter-equivalent method
	 *
	 * @param declaringClass type that declares the method
	 * @param setterName name of the mutator
	 * @param argTypes argument types
	 */
	public MutatorByMethod(Class<C> declaringClass, String setterName, Class ... argTypes) {
		this(Reflections.getMethod(declaringClass, setterName, argTypes));
	}
	
	@Override
	public Method getSetter() {
		return setter;
	}
	
	@Override
	public Method getMethod() {
		return getSetter();
	}
	
	@Override
	public Class<T> getPropertyType() {
		return (Class<T>) getMethod().getParameterTypes()[0];
	}
	
	@Override
	protected void doSet(C c, T t) throws IllegalAccessException, InvocationTargetException {
		fixMethodParameters(t);
		try {
			getSetter().invoke(c, methodParameters);
		} catch (RuntimeException e) {
			// converting "argument type mismatch" cases
			throw new ExceptionConverter().convertException(e, c, this, t);
		}
	}
	
	protected void fixMethodParameters(T t) {
		this.methodParameters[0] = t;
	}
	
	@Override
	protected String getSetterDescription() {
		return Reflections.toString(getSetter());
	}
	
	/**
	 * @return an accessor based on the equivalent getter method if exists, else based on field direct access
	 * @throws NonReversibleAccessor if neither getter nor field could be found
	 */
	@Override
	public AccessorByMember<C, T, ? extends Member> toAccessor() {
		Class<?> declaringClass = getSetter().getDeclaringClass();
		String propertyName = Reflections.propertyName(getSetter());
		AccessorByMethod<C, T> accessorByMethod = Accessors.accessorByMethod(declaringClass, propertyName);
		if (accessorByMethod == null) {
			try {
				return Accessors.accessorByField((Class<C>) declaringClass, propertyName);
			} catch (MemberNotFoundException e) {
				throw new NonReversibleAccessor("Can't find a mutator for " + Reflections.toString(getSetter()), e);
			}
		} else {
			return accessorByMethod;
		}
	}
	
	
	@Override
	public boolean equals(Object other) {
		// we base our implementation on the setter description because a setAccessible() call on the member changes its internal state
		// and I don't think it sould be taken into account for comparison
		return this == other ||
				(other instanceof MutatorByMethod
						&& getSetterDescription().equals(((MutatorByMethod) other).getSetterDescription())
						&& Arrays.equals(methodParameters, ((MutatorByMethod) other).methodParameters));
	}
	
	@Override
	public int hashCode() {
		return 31 * getSetter().hashCode() + Arrays.hashCode(methodParameters);
	}
}
