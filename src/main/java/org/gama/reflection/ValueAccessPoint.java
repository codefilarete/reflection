package org.gama.reflection;

/**
 * A marker interface for {@link IMutator} and {@link IAccessor} hence acting as a generic representation of a property accessor.
 * Mainly use whenever they must be mixed all together in a generic type such as a {@link java.util.Set} or {@link java.util.Map} because
 * they can be handled by {@link ValueAccessPointSet} and {@link ValueAccessPointMap} thanks to {@link ValueAccessPointComparator}.
 *
 * @author Guillaume Mary
 * @see ValueAccessPointComparator
 * @see ValueAccessPointSet
 * @see ValueAccessPointMap
 */
public interface ValueAccessPoint {
	
}
