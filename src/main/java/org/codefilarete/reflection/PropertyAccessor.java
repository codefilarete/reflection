package org.codefilarete.reflection;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.codefilarete.tool.function.Predicates;

/**
 * A class for managing accesses (reading and writing) of a bean property.
 * 
 * @author Guillaume Mary
 * @see Accessors
 */
public class PropertyAccessor<C, T> implements ReversibleAccessor<C, T>, ReversibleMutator<C, T> {
	
	/**
	 * Static constructor, because its signature would conflict with the one with {@link Accessor}, {@link Mutator}
	 * @param accessor a method reference to a getter
	 * @param mutator a method reference to a setter
	 * @param <C> bean type
	 * @param <T> property type
	 * @return a {@link PropertyAccessor} that will access a property through the given getter and getter
	 */
	public static <C, T> PropertyAccessor<C, T> fromMethodReference(SerializableFunction<C, T> accessor, SerializableBiConsumer<C, T> mutator) {
		return new PropertyAccessor<>(new AccessorByMethodReference<>(accessor), new MutatorByMethodReference<>(mutator));
	}
	
	private final Accessor<C, T> accessor;
	private final Mutator<C, T> mutator;
	
	public PropertyAccessor(ReversibleAccessor<C, T> accessor) {
		this(accessor, accessor.toMutator());
	}
	
	public PropertyAccessor(ReversibleMutator<C, T> mutator) {
		this(mutator.toAccessor(), mutator);
	}
	
	public PropertyAccessor(Accessor<C, T> accessor, Mutator<C, T> mutator) {
		this.accessor = accessor;
		this.mutator = mutator;
	}
	public Accessor<C, T> getAccessor() {
		return accessor;
	}
	
	public Mutator<C, T> getMutator() {
		return mutator;
	}
	
	/**
	 * Shortcut for {@link #getAccessor()}.get(c)
	 * @param c the source instance
	 * @return the result of the invocation of the accessor onto c argument
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
	public Accessor<C, T> toAccessor() {
		return getAccessor();
	}
	
	/**
	 * Same as {@link #getMutator()}
	 * @return {@link #getMutator()}
	 */
	@Override
	public Mutator<C, T> toMutator() {
		return getMutator();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof PropertyAccessor)) {
			return super.equals(obj);
		} else {
			return Predicates.equalOrNull(this.getAccessor(), ((PropertyAccessor) obj).getAccessor())
					&& Predicates.equalOrNull(this.getMutator(), ((PropertyAccessor) obj).getMutator());
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
		return "property accessor by " + (getAccessor() instanceof AbstractAccessor
				? ((AbstractAccessor<C, T>) getAccessor()).getGetterDescription()
				: getAccessor().toString());
	}
}
