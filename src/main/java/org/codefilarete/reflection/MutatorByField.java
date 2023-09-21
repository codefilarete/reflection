package org.codefilarete.reflection;

import java.lang.reflect.Field;

import org.codefilarete.tool.Reflections;

/**
 * Property writer through its {@link Field}
 *
 * @author Guillaume Mary
 */
public class MutatorByField<C, T> extends AbstractMutator<C, T>
		implements MutatorByMember<C, T, Field>, ReversibleMutator<C, T>, ValueAccessPointByField {
	
	private final Field field;
	private final Accessor<C, T> accessor;
	
	public MutatorByField(Field field) {
		Reflections.ensureAccessible(field);
		this.field = field;
		// since MutatorByField instantiation has no cost we do it now to avoid lazy initialization which is always tricky
		this.accessor = new AccessorByField<>(field, this);
	}
	
	/**
	 * Internal (package private) constructor that doesn't ensure field accessibility.
	 * Made to avoid lazy initialization in {@link AccessorByField}.
	 *
	 * @param field the field to write to
	 */
	MutatorByField(Field field, Accessor<C, T> accessor) {
		this.field = field;
		this.accessor = accessor;
	}
	
	@Override
	public Field getSetter() {
		return this.field;
	}
	
	@Override
	public Field getField() {
		return getSetter();
	}
	
	@Override
	public Class<T> getPropertyType() {
		return (Class<T>) field.getType();
	}
	
	@Override
	protected void doSet(C c, T t) throws IllegalAccessException {
		getSetter().set(c, t);
	}
	
	@Override
	protected String getSetterDescription() {
		return "mutator for field " + Reflections.toString(getSetter());
	}
	
	@Override
	public Accessor<C, T> toAccessor() {
		return this.accessor;
	}
	
	@Override
	public boolean equals(Object other) {
		// we base our implementation on the setter description because a setAccessible() call on the member changes its internal state
		// and I don't think it sould be taken into account for comparison
		return this == other
				|| (other instanceof MutatorByField && getSetterDescription().equals(((MutatorByField) other).getSetterDescription()));
	}
	
	@Override
	public int hashCode() {
		return getSetter().hashCode();
	}
}
