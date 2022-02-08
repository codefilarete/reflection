package org.codefilarete.reflection;

import java.lang.reflect.Field;

import org.codefilarete.tool.Reflections;

/**
 * @author mary
 */
public class MutatorByField<C, T> extends AbstractMutator<C, T>
		implements MutatorByMember<C, T, Field>, ReversibleMutator<C, T>, ValueAccessPointByField {
	
	private final Field field;
	
	public MutatorByField(Field field) {
		super();
		this.field = field;
		Reflections.ensureAccessible(field);
	}
	
	@Override
	public Field getSetter() {
		return field;
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
	public AccessorByField<C, T> toAccessor() {
		return new AccessorByField<>(getSetter());
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
