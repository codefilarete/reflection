package org.codefilarete.reflection;

import org.codefilarete.tool.Reflections;

/**
 * @author Guillaume Mary
 */
public abstract class AbstractReflector<C> {
	
	private final ExceptionConverter exceptionConverter;
	
	protected AbstractReflector() {
		this.exceptionConverter = new ExceptionConverter();
	}
	
	protected void handleException(Throwable t, C target, Object... args) {
		RuntimeException convertedException = exceptionConverter.convertException(t, target, this, args);
		String message = "Error while applying " + getDescription()
				+ " on instance " + (target == null ? "null" : ("of " + Reflections.toString(target.getClass())));
		if (args != null && args.length > 0) {
			message = message.concat(" with value " + args[0]);
		}
		throw new RuntimeException(message, convertedException);
	}
	
	protected abstract String getDescription();
}
