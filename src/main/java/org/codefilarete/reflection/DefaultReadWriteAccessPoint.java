package org.codefilarete.reflection;

import org.codefilarete.tool.function.Predicates;

/**
 * A class sharing both abilities to write and read an access point.
 * 
 * @author Guillaume Mary
 * @see Accessors
 */
public class DefaultReadWriteAccessPoint<C, T>
		implements ReadWriteAccessPoint<C, T>,
		ReversibleAccessor<C, T>, ReversibleMutator<C, T> {
	
	/**
	 * Static constructor, because its signature would conflict with the one with {@link Accessor}, {@link Mutator}
	 * @param accessor a method reference to a getter
	 * @param mutator a method reference to a setter
	 * @param <C> bean type
	 * @param <T> property type
	 * @return a {@link DefaultReadWriteAccessPoint} that will access a property through the given getter and getter
	 */
	public static <C, T> DefaultReadWriteAccessPoint<C, T> fromMethodReference(SerializableAccessor<C, T> accessor, SerializableMutator<C, T> mutator) {
		return new DefaultReadWriteAccessPoint<>(new AccessorByMethodReference<>(accessor), new MutatorByMethodReference<>(mutator));
	}
	
	private final Accessor<C, T> accessor;
	private final Mutator<C, T> mutator;
	
	public DefaultReadWriteAccessPoint(ReversibleAccessor<C, T> accessor) {
		this(accessor, accessor.toMutator());
	}
	
	public DefaultReadWriteAccessPoint(ReversibleMutator<C, T> mutator) {
		this(mutator.toAccessor(), mutator);
	}
	
	public DefaultReadWriteAccessPoint(Accessor<C, T> accessor, Mutator<C, T> mutator) {
		this.accessor = accessor;
		this.mutator = mutator;
	}
	
	@Override
	public Accessor<C, T> getReader() {
		return accessor;
	}
	
	@Override
	public Mutator<C, T> getWriter() {
		return mutator;
	}
	
	/**
	 * Shortcut for {@link #getReader()}.get(c)
	 * @param c the source instance
	 * @return the result of the invocation of the readWriteAccessPoint onto c argument
	 */
	@Override
	public T get(C c) {
		return this.accessor.get(c);
	}
	
	/**
	 * Shortcut for {@link #getWriter()}.set(c, t)
	 * @param c the source instance
	 * @param t the argument of the setter
	 */
	public void set(C c, T t) {
		this.mutator.set(c, t);
	}
	
	@Override
	public Accessor<C, T> toAccessor() {
		return accessor;
	}
	
	@Override
	public Mutator<C, T> toMutator() {
		return mutator;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof DefaultReadWriteAccessPoint)) {
			return super.equals(obj);
		} else {
			return Predicates.equalOrNull(this.getReader(), ((DefaultReadWriteAccessPoint) obj).getReader())
					&& Predicates.equalOrNull(this.getWriter(), ((DefaultReadWriteAccessPoint) obj).getWriter());
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
		return "property readWriteAccessPoint by " + (getReader() instanceof AbstractAccessor
				? ((AbstractAccessor<C, T>) getReader()).getGetterDescription()
				: getReader().toString());
	}
}
