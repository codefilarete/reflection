package org.gama.reflection;

import java.lang.reflect.Member;

/**
 * @author Guillaume Mary
 */
public interface MutatorByMember<C, T, M extends Member> extends Mutator<C, T> {
	
	M getSetter();
	
	Class<T> getPropertyType();
}
