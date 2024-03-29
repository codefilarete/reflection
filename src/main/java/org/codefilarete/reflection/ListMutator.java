package org.codefilarete.reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.codefilarete.tool.Reflections;

/**
 * @author Guillaume Mary
 */
public class ListMutator<C extends List<T>, T> extends MutatorByMethod<C, T> {
	
	private int index;
	
	public ListMutator() {
		super(Reflections.findMethod(List.class, "set", Integer.TYPE, Object.class));
	}
	
	public ListMutator(int index) {
		this();
		setIndex(index);
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	protected void doSet(C c, T t) throws IllegalAccessException, InvocationTargetException {
		c.set(getIndex(), t);	// faster than invoke()
	}
	
	@Override
	public ListAccessor<C, T> toAccessor() {
		return new ListAccessor<>(getIndex());
	}
}
