package org.gama.reflection;

/**
 * @param <C> the owning type of the value to be accessed
 * @param <T> value type
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface IAccessor<C, T> extends ValueAccessPoint {
	
	T get(C c);
}
