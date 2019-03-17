package org.gama.reflection;

import java.lang.reflect.Method;

/**
 * Common ancestor of {@link AccessorByMethod} and {@link MutatorByMethod} so one can get their method (getter / setter) without casting. 
 * 
 * @author Guillaume Mary
 */
public interface ValueAccessPointByMethod extends ValueAccessPoint {
	
	Method getMethod();
}
