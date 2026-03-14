package org.codefilarete.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.codefilarete.tool.Reflections;

/**
 * @author Guillaume Mary
 */
public class ListMutator<C extends List<T>, T> extends AbstractMutator<C, T> implements ReversibleMutator<C, T> {
	
	private static final Method SET = Reflections.findMethod(List.class, "set", Integer.TYPE, Object.class);
	
	private int index;
	
	private final AccessorByMethod<C, T> listSetAccessor = new AccessorByMethod<>(SET);
	
	public ListMutator() {
	}
	
	public ListMutator(int index) {
		this();
		setIndex(index);
	}
	
	public void setIndex(int index) {
		this.listSetAccessor.setParameter(0, index);
	}
	
	public int getIndex() {
		// preventing NullPointerException
		Object parameter = listSetAccessor.getParameter(0);
		return parameter == null ? 0 : (int) parameter;
		
	}
	
	@Override
	protected void doSet(C c, T t) {
		c.set(getIndex(), t);
	}
	
	@Override
	protected String getSetterDescription() {
		return Reflections.toString(List.class) + ".set(" + getIndex() +")";
	}
	
	@Override
	public ListAccessor<C, T> toAccessor() {
		return new ListAccessor<>(getIndex());
	}
}
