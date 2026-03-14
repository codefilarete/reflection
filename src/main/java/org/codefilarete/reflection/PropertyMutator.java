package org.codefilarete.reflection;

/**
 * Common interface for marking a {@link PropertyAccessPoint} as a mutator of a bean property.
 *
 * @param <C> class declaring the property
 * @param <T> the property type
 * @author Guillaume Mary
 */
public interface PropertyMutator<C, T> extends PropertyAccessPoint<C, T>, Mutator<C, T> {
}
