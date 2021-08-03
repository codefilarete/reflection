package org.gama.reflection;

/**
 * General contract of a way to get write-access to a property. Then {@link #set(Object, Object)} can be invoked with an instance of supported
 * type and the value to write.
 *  
 * @param <C> the owning type of the value to be modified
 * @param <T> value type
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface Mutator<C, T> extends ValueAccessPoint {
	
	void set(C c, T t);
}
