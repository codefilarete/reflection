package org.codefilarete.reflection;

import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.tool.collection.Collections;
import org.codefilarete.tool.collection.Iterables;

import java.util.List;

/**
 * Equivalent to {@link AccessorChain} and {@link AccessorChainMutator} as an all-in-one accessor and mutator.
 * However, as a difference with {@link AccessorChainMutator}, this class is symmetric and you get access to the same
 * objet through its getter and mutator ({@link AccessorChainMutator}'s getter gives access to the penultimate object of the
 * chain). If needed, one can get a penultimate object accessor with {@link #toChainWithoutLastAccessPoint()}.
 *
 * Moreover, this class implements {@link ReadWritePropertyAccessPoint} to emphase its goal: access beans properties.
 *
 * @param <C>
 * @param <X>
 * @param <T>
 * @author Guillaume Mary
 */
public class ReadWriteAccessorChain<C, X, T> 
		implements ReadWritePropertyAccessPoint<C, T>, ValueAccessPointChain, AccessorDefinitionDefiner<C> {
	
	private final AccessorChain<C, T> accessorChain;
	private final AccessorChainMutator<C, X, T> mutatorChain;
	private final PropertyMutator<C, T> mutator;
	
	public ReadWriteAccessorChain(PropertyAccessor<C, X> rootAccessor, ReadWriteAccessPoint<X, T> mutator) {
		this.accessorChain = new AccessorChain<>(Arrays.asList(rootAccessor, mutator));
		this.mutatorChain = new AccessorChainMutator<>(Arrays.asList(rootAccessor), mutator);
		this.mutator = this.mutatorChain::set;
	}
	
	public ReadWriteAccessorChain(List<? extends Accessor<?, ?>> rootAccessors, ReadWriteAccessPoint<X, T> lastAccessPoint) {
		this.accessorChain = new AccessorChain<>(Collections.cat(rootAccessors, Arrays.asList(lastAccessPoint)));
		this.mutatorChain = new AccessorChainMutator<>(rootAccessors, lastAccessPoint);
		this.mutator = this.mutatorChain::set;
	}
	
	public ReadWriteAccessorChain(AccessorChain<C, T> accessorChain) {
		this.accessorChain = accessorChain;
		this.mutatorChain = (AccessorChainMutator<C, X, T>) accessorChain.toMutator();
		this.mutator = this.mutatorChain::set;
	}
	
	public ReadWriteAccessorChain(AccessorChainMutator<C, X, T> accessorChain) {
		this.accessorChain = accessorChain.toAccessor();
		this.mutatorChain = accessorChain;
		this.mutator = this.mutatorChain::set;
	}
	
	@Override
	public List<? extends ValueAccessPoint<?>> getAccessors() {
		return this.accessorChain.getAccessors();
	}
	
	@Override
	public PropertyAccessor<C, T> getReader() {
		return accessorChain;
	}
	
	@Override
	public PropertyMutator<C, T> getWriter() {
		return mutator;
	}
	
	public ReadWriteAccessorChain<C, ?, X> toChainWithoutLastAccessPoint() {
		ReadWriteAccessorChain<C, Object, X> result = new ReadWriteAccessorChain<>(new AccessorChain<>(Iterables.cutTail(this.accessorChain.getAccessors())));
		result.setNullValueHandler(mutatorChain.getNullValueHandler());
		return result;
	}
	
	@Override
	public ReadWriteAccessorChain<C, X, T> setNullValueHandler(AccessorChain.NullValueHandler nullValueHandler) {
		accessorChain.setNullValueHandler(nullValueHandler);
		mutatorChain.setNullValueHandler(nullValueHandler);
		return this;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!(other instanceof ReadWriteAccessorChain)) {
			return super.equals(other);
		} else {
			return accessorChain.equals(((ReadWriteAccessorChain<?, ?, ?>) other).accessorChain) && this.mutatorChain.equals(((ReadWriteAccessorChain<?, ?, ?>) other).mutatorChain);
		}
	}
	
	@Override
	public int hashCode() {
		return 31 * accessorChain.hashCode() + this.mutatorChain.hashCode();
	}
	
	/**
	 * Implemented because readWriteAccessPoint::get and readWriteAccessPoint::get doesn't make good candidates for {@link AccessorDefinition}
	 * and are not decompilable by {@link AccessorDefinition} logic.
	 * 
	 * @return the {@link AccessorDefinition} of the readWriteAccessPoint chain
	 */
	@Override
	public AccessorDefinition asAccessorDefinition() {
		return AccessorDefinition.giveDefinition(this.accessorChain);
	}
	
	@Override
	public String toString() {
		return this.accessorChain.toString();
	}
}
