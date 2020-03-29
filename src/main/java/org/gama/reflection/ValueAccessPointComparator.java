package org.gama.reflection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guillaume Mary
 */
public class ValueAccessPointComparator implements Comparator<ValueAccessPoint> {
	
	/** Since {@link AccessorDefinition} computation can be costly we use a cache, it may be shared between instances */
	private final Map<ValueAccessPoint, AccessorDefinition> cache;
	
	/**
	 * Default constructor
	 */
	public ValueAccessPointComparator() {
		this(new HashMap<>());
	}
	
	/**
	 * Constuctor that uses the given cache for {@link AccessorDefinition} computation.
	 * 
	 * @param cache a {@link Map} used as cache for finding {@link AccessorDefinition} of a {@link ValueAccessPoint}
	 */
	public ValueAccessPointComparator(Map<ValueAccessPoint, AccessorDefinition> cache) {
		this.cache = cache;
	}
	
	@Override
	public int compare(ValueAccessPoint o1, ValueAccessPoint o2) {
		AccessorDefinition accessorDefinition1 = cache.computeIfAbsent(o1, AccessorDefinition::giveDefinition);
		AccessorDefinition accessorDefinition2 = cache.computeIfAbsent(o2, AccessorDefinition::giveDefinition);
		return accessorDefinition1.compareTo(accessorDefinition2);
	}
	
}
