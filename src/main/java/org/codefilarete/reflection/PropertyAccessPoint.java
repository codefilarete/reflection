package org.codefilarete.reflection;

/**
 * Common interface for marking {@link ValueAccessPoint} targeting a bean property.
 * Thus, this excludes {@link java.util.List} or {@link java.lang.reflect.Array} accessors.
 *
 * @param <C> class declaring the property
 * @param <T> the property type
 * @author Guillaume Mary
 */
public interface PropertyAccessPoint<C, T> extends ValueAccessPoint<C> {
}
