package org.gama.reflection;

import org.gama.lang.bean.Objects;

/**
 * A class for managing accesses (reading and writing) of a bean property.
 * 
 * @author Guillaume Mary
 * @see Accessors
 */
public class PropertyAccessor<C, T> implements IReversibleAccessor<C, T>, IReversibleMutator<C, T> {
	
	private final IAccessor<C, T> accessor;
	private final IMutator<C, T> mutator;
	
	public PropertyAccessor(IReversibleAccessor<C, T> accessor) {
		this(accessor, accessor.toMutator());
	}
	
	public PropertyAccessor(IReversibleMutator<C, T> mutator) {
		this(mutator.toAccessor(), mutator);
	}
	
	public PropertyAccessor(IAccessor<C, T> accessor, IMutator<C, T> mutator) {
		this.accessor = accessor;
		this.mutator = mutator;
	}
	
	public IAccessor<C, T> getAccessor() {
		return accessor;
	}
	
	public IMutator<C, T> getMutator() {
		return mutator;
	}
	
	/**
	 * Shortcut for {@link #getAccessor()}.get(c)
	 * @param c the source instance
	 * @return the result of the invokation of the accessor onto c argument
	 */
	@Override
	public T get(C c) {
		return this.accessor.get(c);
	}
	
	/**
	 * Shortcut for {@link #getMutator()}.set(c, t)
	 * @param c the source instance
	 * @param t the argument of the setter
	 */
	public void set(C c, T t) {
		this.mutator.set(c, t);
	}
	
	/**
	 * Same as {@link #getAccessor()}
	 * @return {@link #getAccessor()}
	 */
	@Override
	public IAccessor<C, T> toAccessor() {
		return getAccessor();
	}
	
	/**
	 * Same as {@link #getMutator()}
	 * @return {@link #getMutator()}
	 */
	@Override
	public IMutator<C, T> toMutator() {
		return getMutator();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof PropertyAccessor)) {
			return super.equals(obj);
		} else {
			return Objects.equalsWithNull(this.getAccessor(), ((PropertyAccessor) obj).getAccessor())
					&& Objects.equalsWithNull(this.getMutator(), ((PropertyAccessor) obj).getMutator());
		}
	}
	
	@Override
	public int hashCode() {
		// Implementation based on both accessor and mutator. Accessor is taken first but it doesn't matter
		return 31 * getAccessor().hashCode() + getMutator().hashCode();
	}
	
	/**
	 * Implemented for trace in errors or debug
	 * @return the getter description if available, else defaults to super.toString()
	 */
	@Override
	public String toString() {
		if (getAccessor() instanceof AbstractAccessor) {
			return ((AbstractAccessor<C, T>) getAccessor()).getGetterDescription();
		}
		return super.toString();
	}
}
