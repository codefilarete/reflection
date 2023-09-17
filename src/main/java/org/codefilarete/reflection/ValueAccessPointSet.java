package org.codefilarete.reflection;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link Set} dedicated to {@link ValueAccessPoint}s because they are hardly comparable, so this class is a {@link TreeSet} with a
 * {@link ValueAccessPointComparator}
 * 
 * @author Guillaume Mary
 */
public class ValueAccessPointSet<C> extends TreeSet<ValueAccessPoint<C>> {
	
	public ValueAccessPointSet() {
		super(new ValueAccessPointComparator());
	}
	
	public ValueAccessPointSet(Collection<? extends ValueAccessPoint<C>> set) {
		this();
		addAll(set);
	}
}
