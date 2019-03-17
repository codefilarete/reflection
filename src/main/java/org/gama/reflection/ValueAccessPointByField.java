package org.gama.reflection;

import java.lang.reflect.Field;

/**
 * Common ancestor of {@link AccessorByField} and {@link MutatorByField} so one can get their field without casting. 
 *
 * @author Guillaume Mary
 */
public interface ValueAccessPointByField {
	
	Field getField();
}
