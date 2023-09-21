package org.codefilarete.reflection;

import java.lang.reflect.Array;

/**
 * @author Guillaume Mary
 */
// NB: I didn't manage to create AbstractMutator<C[], C> without having a "C cannot be cast to Object[]" from MetaModelAccessorBuilder
public class ArrayMutator<C> extends AbstractMutator<C, C> implements ReversibleMutator<C, C> {
	
	private int index;
	private final ArrayAccessor<C> accessor;
	
	public ArrayMutator() {
		this.accessor = new ArrayAccessor<>(0, this);
	}
	
	public ArrayMutator(int index) {
		this.index = index;
		this.accessor = new ArrayAccessor<>(index, this);
	}
	
	public ArrayMutator(int index, ArrayAccessor<C> accessor) {
		this.index = index;
		this.accessor = accessor;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	/**
	 * Same as {@link #set(Object, Object)} but using a direct access to the array
	 */
	public void set(C[] cs, C c) {
		cs[index] = c;
	}
	
	@Override
	public void set(C c, C other) {
		try {
			doSet(c, other);
		} catch (Throwable throwable) {
			handleException(throwable, c);
		}
	}
	
	@Override
	protected void doSet(C cs, C c) {
		Array.set(cs, getIndex(), c);
	}
	
	@Override
	protected String getSetterDescription() {
		return "array mutator on index " + index;
	}
	
	@Override
	public ArrayAccessor<C> toAccessor() {
		return this.accessor;
	}
}
