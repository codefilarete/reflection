package org.gama.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.gama.lang.Reflections.MemberNotFoundException;

/**
 * @author mary
 */
public class MutatorByMethod<C, T> extends AbstractMutator<C, T>
		implements MutatorByMember<C, T, Method>, ReversibleMutator<C, T>, ValueAccessPointByMethod {
	
	private final Method setter;
	
	public MutatorByMethod(Method setter) {
		super();
		this.setter = setter;
		Reflections.ensureAccessible(setter);
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
		try {
			getSetter().invoke(c, t);
		} catch (RuntimeException e) {
			// converting "argument type mismatch" cases
			throw new ExceptionConverter().convertException(e, c, this, t);
		}
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
				(other instanceof MutatorByMethod && getSetterDescription().equals(((MutatorByMethod) other).getSetterDescription()));
	}
	
	@Override
	public int hashCode() {
		return getSetter().hashCode();
	}
}
