package org.gama.reflection;

import java.lang.invoke.SerializedLambda;

/**
 * Common ancestor of {@link AccessorByMethodReference} and {@link MutatorByMethodReference} so one can get their common description without casting. 
 * 
 * @author Guillaume Mary
 */
public interface ValueAccessPointByMethodReference extends ValueAccessPoint {
	
	String getMethodName();
	
	Class getDeclaringClass();
	
	SerializedLambda getSerializedLambda();
	
	Class getPropertyType();
}
