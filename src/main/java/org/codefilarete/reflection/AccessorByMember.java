package org.codefilarete.reflection;

import java.lang.reflect.Member;

/**
 * @author Guillaume Mary
 */
public interface AccessorByMember<C, T, M extends Member> extends PropertyAccessor<C, T> {
	
	M getGetter();
	
	Class<T> getPropertyType();
}
