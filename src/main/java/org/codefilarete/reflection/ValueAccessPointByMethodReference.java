package org.codefilarete.reflection;

import java.lang.invoke.SerializedLambda;

/**
 * Common ancestor of {@link AccessorByMethodReference} and {@link MutatorByMethodReference} so one can get their common description without casting. 
 * 
 * @author Guillaume Mary
 */
public interface ValueAccessPointByMethodReference<C> extends ValueAccessPoint<C> {
	
	String getMethodName();
	
	Class<C> getDeclaringClass();
	
	SerializedLambda getSerializedLambda();
	
	Class getPropertyType();
}
