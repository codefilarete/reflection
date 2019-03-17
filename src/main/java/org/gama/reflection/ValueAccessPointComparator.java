package org.gama.reflection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guillaume Mary
 */
public class ValueAccessPointComparator implements Comparator<ValueAccessPoint> {
	
	/** Since {@link MemberDefinition} computation can be costly we use a cache, it may be shared between instances */
	private final Map<ValueAccessPoint, MemberDefinition> cache;
	
	/**
	 * Default constructor
	 */
	public ValueAccessPointComparator() {
		this(new HashMap<>());
	}
	
	/**
	 * Constuctor that uses the given cache for {@link MemberDefinition} computation.
	 * 
	 * @param cache a {@link Map} used as cache for finding {@link MemberDefinition} of a {@link ValueAccessPoint}
	 */
	public ValueAccessPointComparator(Map<ValueAccessPoint, MemberDefinition> cache) {
		this.cache = cache;
	}
	
	@Override
	public int compare(ValueAccessPoint o1, ValueAccessPoint o2) {
		MemberDefinition memberDefinition1 = cache.computeIfAbsent(o1, MemberDefinition::giveMemberDefinition);
		MemberDefinition memberDefinition2 = cache.computeIfAbsent(o2, MemberDefinition::giveMemberDefinition);
		return memberDefinition1.compareTo(memberDefinition2);
	}
	
}
