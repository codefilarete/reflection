package org.codefilarete.reflection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparator for {@link ValueAccessPoint} that makes them not different if their goal is to access same property
 * whatever the way they use (by field, by method, by method reference ...)
 * Based on {@link #compareTo(AccessorDefinition, AccessorDefinition)}.
 * This class also uses a cache to store {@link AccessorDefinition} (which helps comparison) which can be shared
 * between instances. See {@link #ValueAccessPointComparator(Map)} to give your shared {@link Map} instance.
 * 
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
	 * Constructor that uses the given cache for {@link AccessorDefinition} computation.
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
		return compareTo(accessorDefinition1, accessorDefinition2);
	}
	
	protected int compareTo(AccessorDefinition o1, AccessorDefinition o2) {
		int comparison = o2.getDeclaringClass().getName().compareTo(o1.getDeclaringClass().getName());
		if (comparison == 0) {
			comparison = o2.getName().compareTo(o1.getName());
			if (comparison == 0) {
				comparison = o2.getMemberType().toString().compareTo(o1.getMemberType().toString());
			}
		}
		return comparison;
	}
	
}
