package org.gama.reflection;

/**
 * General contract of a way to get read-access to a property. Then {@link #get(Object)} can be invoked with an instance of supported type to
 * retrieve a value.
 * 
 * @param <C> the owning type of the value to be accessed
 * @param <T> value type
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface Accessor<C, T> extends ValueAccessPoint {
	
	T get(C c);
}
