package org.codefilarete.reflection;

/**
 * Equivalent of {@link ReadWriteAccessPoint} dedicated to property bean.
 *
 *
 * @param <C> the class declaring the property
 * @param <T> the property type
 */
public class ReadWritePropertyAccessPoint<C, T>
		implements ReversibleAccessor<C, T>, ReversibleMutator<C, T>,
		PropertyAccessor<C, T>, PropertyMutator<C, T>
{
	
	private final PropertyAccessor<C, T> accessor;
	private final PropertyMutator<C, T> mutator;
	
	public ReadWritePropertyAccessPoint(ReversibleAccessor<C, T> accessor) {
		this((PropertyAccessor<C, T>) accessor, (PropertyMutator<C, T>) accessor.toMutator());
	}
	
	public ReadWritePropertyAccessPoint(ReversibleMutator<C, T> mutator) {
		this((PropertyAccessor<C, T>) mutator.toAccessor(), (PropertyMutator<C, T>) mutator);
	}
	
	public ReadWritePropertyAccessPoint(PropertyAccessor<C, T> accessor, PropertyMutator<C, T> mutator) {
		this.accessor = accessor;
		this.mutator = mutator;
	}
	public PropertyAccessor<C, T> getAccessor() {
		return accessor;
	}
	
	public PropertyMutator<C, T> getMutator() {
		return mutator;
	}
	
	@Override
	public T get(C c) {
		return accessor.get(c);
	}
	
	@Override
	public void set(C c, T t) {
		mutator.set(c, t);
	}
	
	@Override
	public Accessor<C, T> toAccessor() {
		return accessor;
	}
	
	@Override
	public Mutator<C, T> toMutator() {
		return mutator;
	}
	
	/**
	 * Implemented for trace in errors or debug
	 * @return the getter description if available, else defaults to super.toString()
	 */
	@Override
	public String toString() {
		return "property accessor by " + (getAccessor() instanceof AbstractAccessor
				? ((AbstractAccessor<C, T>) getAccessor()).getGetterDescription()
				: getAccessor().toString());
	}
}
