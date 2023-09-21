package org.codefilarete.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Reflections.MemberNotFoundException;
import org.codefilarete.tool.function.ThreadSafeLazyInitializer;

/**
 * @author mary
 */
public class MutatorByMethod<C, T> extends AbstractMutator<C, T>
		implements MutatorByMember<C, T, Method>, ReversibleMutator<C, T>, ValueAccessPointByMethod<C> {
	
	private final Method setter;
	private final Supplier<Accessor<C, T>> accessor;
	
	public MutatorByMethod(Method setter) {
		Reflections.ensureAccessible(setter);
		this.setter = setter;
		this.accessor = new ThreadSafeLazyInitializer<Accessor<C, T>>() {
			@Override
			protected Accessor<C, T> createInstance() {
				return findCompatibleAccessor();
			}
		};
	}
	
	MutatorByMethod(Method setter, Accessor<C, T> accessor) {
		this.setter = setter;
		this.accessor = () -> accessor;
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
	public Accessor<C, T> toAccessor() {
		return this.accessor.get();

	}
	
	private Accessor<C, T> findCompatibleAccessor() {
		Class<?> declaringClass = getSetter().getDeclaringClass();
		String propertyName = Reflections.propertyName(getSetter());
		AccessorByMethod<C, T> accessorByMethod = Accessors.accessorByMethod(declaringClass, propertyName,(Class<T>) getSetter().getParameterTypes()[0], this);
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
