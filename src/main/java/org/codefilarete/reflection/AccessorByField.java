package org.codefilarete.reflection;

import java.lang.reflect.Field;

import org.codefilarete.tool.Reflections;

/**
 * Property reader through its {@link Field}
 *
 * @author Guillaume Mary
 */
public class AccessorByField<C, T> extends AbstractAccessor<C, T>
		implements AccessorByMember<C, T, Field>, ReversibleAccessor<C, T>, ValueAccessPointByField {
	
	private final Field field;
	private final Mutator<C, T> mutator;
	
	public AccessorByField(Field field) {
		Reflections.ensureAccessible(field);
		this.field = field;
		// since MutatorByField instantiation has no cost we do it now to avoid lazy initialization which is always tricky
		this.mutator = new MutatorByField<>(field, this);
	}
	
	/**
	 * Internal (package private) constructor that doesn't ensure field accessibility.
	 * Made to avoid lazy initialization in {@link MutatorByField}.
	 *
	 * @param field the field to write to
	 */
	AccessorByField(Field field, Mutator<C, T> mutator) {
		this.field = field;
		this.mutator = mutator;
	}
	
	@Override
	public Field getGetter() {
		return this.field;
	}
	
	@Override
	public Field getField() {
		return getGetter();
	}
	
	@Override
	public Class<T> getPropertyType() {
		return (Class<T>) field.getType();
	}
	
	@Override
	protected T doGet(C c) throws IllegalAccessException {
		return (T) getGetter().get(c);
	}
	
	@Override
	public String getGetterDescription() {
		return "accessor for field " + Reflections.toString(getGetter());
	}
	
	@Override
	public Mutator<C, T> toMutator() {
		return this.mutator;
	}
	
	@Override
	public boolean equals(Object other) {
		// we base our implementation on the getter String because a setAccessible() call on the member changes its internal state
		// and I don't think it sould be taken into account for comparison
		return this == other
				|| (other instanceof AccessorByField && getGetter().toString().equals(((AccessorByField) other).getGetter().toString()));
	}
	
	@Override
	public int hashCode() {
		return getGetter().hashCode();
	}
}
