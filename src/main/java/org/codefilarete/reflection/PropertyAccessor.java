package org.codefilarete.reflection;

/**
 * Common interface for marking a {@link PropertyAccessPoint} as an accessor of a bean property.
 *
 * @param <C> class declaring the property
 * @param <T> the property type
 * @author Guillaume Mary
 */
public interface PropertyAccessor<C, T> extends PropertyAccessPoint<C, T>, Accessor<C, T> {
}
