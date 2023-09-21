package org.codefilarete.reflection;

import java.lang.reflect.Array;

/**
 * @author Guillaume Mary
 */
// NB: I didn't manage to create AbstractAccessor<C[], C> without having a "C cannot be cast to Object[]" from MetaModelAccessorBuilder
public class ArrayAccessor<C> extends AbstractAccessor<C, C> implements ReversibleAccessor<C, C> {
	
	private int index;
	private final ArrayMutator<C> mutator;
	
	public ArrayAccessor() {
		this.mutator = new ArrayMutator<>(0, this);
	}
	
	public ArrayAccessor(int index) {
		this.index = index;
		this.mutator = new ArrayMutator<>(index, this);
	}
	
	ArrayAccessor(int index, ArrayMutator<C> mutator) {
		this.index = index;
		this.mutator = mutator;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	/**
	 * Same as {@link #get(Object)} but using a direct access to the array
	 */
	public C get(C[] cs) {
		return cs[getIndex()];
	}
	
	@Override
	public C get(C c) {
		try {
			return doGet(c);
		} catch (RuntimeException t) {
			handleException(t, c);
			// shouldn't happen
			return null;
		}
	}
	
	@Override
	protected C doGet(C cs) {
		return (C) Array.get(cs, getIndex());
	}
	
	@Override
	protected String getGetterDescription() {
		return "array accessor for index " + index;
	}
	
	@Override
	public ArrayMutator<C> toMutator() {
		return this.mutator;
	}
}
