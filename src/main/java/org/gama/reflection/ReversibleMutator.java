package org.gama.reflection;

/**
 * A {@link Mutator} than can give its "read-mirror" as an {@link Accessor} (will read same property).
 * 
 * @author Guillaume Mary
 */
public interface ReversibleMutator<C, T> extends Mutator<C, T> {
	
	Accessor<C, T> toAccessor();
}
