package org.codefilarete.reflection;

import org.codefilarete.tool.function.Predicates;

/**
 * Equivalent of {@link ReadWriteAccessPoint} dedicated to property bean, which means that it doesn't allow accessibility
 * for {@link ListAccessor} or {@link ArrayAccessor}.
 *
 * @param <C> the class declaring the property
 * @param <T> the property type
 * @author Guillaume Mary
 */
public class DefaultReadWritePropertyAccessPoint<C, T>
		implements ReversibleAccessor<C, T>, ReversibleMutator<C, T>,
		ReadWritePropertyAccessPoint<C, T>
{
	/**
	 * Static constructor, because its signature would conflict with the one with {@link Accessor}, {@link Mutator}
	 * @param accessor a method reference to a getter
	 * @param mutator a method reference to a setter
	 * @param <C> bean type
	 * @param <T> property type
	 * @return a {@link DefaultReadWriteAccessPoint} that will access a property through the given getter and getter
	 */
	public static <C, T> DefaultReadWritePropertyAccessPoint<C, T> fromMethodReference(SerializablePropertyAccessor<C, T> accessor, SerializablePropertyMutator<C, T> mutator) {
		return new DefaultReadWritePropertyAccessPoint<>(new AccessorByMethodReference<>(accessor), new MutatorByMethodReference<>(mutator));
	}
	
	private final PropertyAccessor<C, T> accessor;
	private final PropertyMutator<C, T> mutator;
	
	public DefaultReadWritePropertyAccessPoint(ReversibleAccessor<C, T> accessor) {
		this((PropertyAccessor<C, T>) accessor, (PropertyMutator<C, T>) accessor.toMutator());
	}
	
	public DefaultReadWritePropertyAccessPoint(ReversibleMutator<C, T> mutator) {
		this((PropertyAccessor<C, T>) mutator.toAccessor(), (PropertyMutator<C, T>) mutator);
	}
	
	public DefaultReadWritePropertyAccessPoint(PropertyAccessor<C, T> accessor, PropertyMutator<C, T> mutator) {
		this.accessor = accessor;
		this.mutator = mutator;
	}
	
	@Override
	public PropertyAccessor<C, T> getReader() {
		return accessor;
	}
	
	@Override
	public PropertyMutator<C, T> getWriter() {
		return mutator;
	}
	
	@Override
	public PropertyAccessor<C, T> toAccessor() {
		return accessor;
	}
	
	@Override
	public PropertyMutator<C, T> toMutator() {
		return mutator;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!(other instanceof DefaultReadWritePropertyAccessPoint)) {
			return super.equals(other);
		} else {
			return Predicates.equalOrNull(this.getReader(), ((DefaultReadWritePropertyAccessPoint) other).getReader())
					&& Predicates.equalOrNull(this.getWriter(), ((DefaultReadWritePropertyAccessPoint) other).getWriter());
		}
	}
	
	@Override
	public int hashCode() {
		// Implementation based on both readWriteAccessPoint and readWriteAccessPoint. Accessor is taken first but it doesn't matter
		return 31 * getReader().hashCode() + getWriter().hashCode();
	}
	
	/**
	 * Implemented for trace in errors or debug
	 * @return the getter description if available, else defaults to super.toString()
	 */
	@Override
	public String toString() {
		return "property readWriteAccessPoint by " + (this.getReader() instanceof AbstractAccessor
				? ((AbstractAccessor<C, T>) this.getReader()).getGetterDescription()
				: this.getReader().toString());
	}
}
