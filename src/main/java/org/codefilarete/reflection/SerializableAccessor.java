package org.codefilarete.reflection;

import java.io.Serializable;

/**
 * A {@link Serializable} version of {@link Accessor} to make it decompilable and unshell the referred Method Reference
 * behind it.
 *
 * @param <C> the owning type of the value to be accessed
 * @param <T> the value / property type
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface SerializableAccessor<C, T> extends Accessor<C, T>, Serializable {
	
}