package org.codefilarete.reflection;

/**
 * Contract for classes that want to share both abilities to write and read an access point : it inherits from
 * {@link Accessor} and {@link Mutator}.
 * 
 * @author Guillaume Mary
 * @see Accessors
 */
public interface ReadWriteAccessPoint<C, T>
		extends Accessor<C, T>, Mutator<C, T> {
	
	// Naming note : those methods can't be named toAccessor() and toMutator() because there return types are not
	// compatible on AccessorChainMutator chain, because this particular class is not symmetric and return an
	// intermediary accessor : the last before the mutator.
	
	Accessor<C, T> getReader();
	
	Mutator<C, T> getWriter();
}
