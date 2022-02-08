package org.codefilarete.reflection;

/**
 * Marker for unresolvable mutator.
 * 
 * @author Guillaume Mary
 */
public class NonReversibleAccessor extends RuntimeException {
	
	public NonReversibleAccessor(String message) {
		super(message);
	}
	
	public NonReversibleAccessor(String message, Throwable cause) {
		super(message, cause);
	}
}
