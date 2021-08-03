package org.gama.reflection;

/**
 * An {@link Accessor} than can give its "write-mirror" as a {@link Mutator} (will write same property).
 * 
 * @author Guillaume Mary
 */
public interface ReversibleAccessor<C, T> extends Accessor<C, T> {
	
	Mutator<C, T> toMutator();
	
}
