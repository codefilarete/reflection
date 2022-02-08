package org.codefilarete.reflection;

import java.lang.reflect.Member;

/**
 * @author Guillaume Mary
 */
public interface AccessorByMember<C, T, M extends Member> extends Accessor<C, T> {
	
	M getGetter();
	
	Class<T> getPropertyType();
}
