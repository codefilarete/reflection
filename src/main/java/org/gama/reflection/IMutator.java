package org.gama.reflection;

/**
 * @param <C> the owning type of the value to be modified
 * @param <T> value type
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface IMutator<C, T> extends ValueAccessPoint {
	
	void set(C c, T t);
}
