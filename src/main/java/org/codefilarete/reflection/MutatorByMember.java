package org.codefilarete.reflection;

import java.lang.reflect.Member;

/**
 * @author Guillaume Mary
 */
public interface MutatorByMember<C, T, M extends Member> extends PropertyMutator<C, T> {
	
	M getSetter();
	
	Class<T> getPropertyType();
}
