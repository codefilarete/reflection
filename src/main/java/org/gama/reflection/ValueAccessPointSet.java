package org.gama.reflection;

import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link Set} dedicated to {@link ValueAccessPoint}s because they are hardly comparable, so this class is a {@link TreeSet} with a
 * {@link ValueAccessPointComparator}
 * 
 * @author Guillaume Mary
 */
public class ValueAccessPointSet extends TreeSet<ValueAccessPoint> {
	
	public ValueAccessPointSet() {
		super(new ValueAccessPointComparator());
	}
	
	public ValueAccessPointSet(Set<? extends ValueAccessPoint> set) {
		this();
		addAll(set);
	}
}
