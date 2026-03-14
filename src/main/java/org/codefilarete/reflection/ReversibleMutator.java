package org.codefilarete.reflection;

/**
 * A {@link Mutator} than can give its "read-mirror" as an {@link Accessor} (will read same property).
 *
 * @param <C> the owning type of the value to be modified
 * @param <T> value type
 * @author Guillaume Mary
 */
public interface ReversibleMutator<C, T> extends Mutator<C, T> {
	
	Accessor<C, T> toAccessor();
}
