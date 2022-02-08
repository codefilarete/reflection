package org.codefilarete.reflection;

import java.util.Map;
import java.util.TreeMap;

/**
 * A {@link Map} dedicated to {@link ValueAccessPoint}s as key because they are hardly comparable, so this class is a {@link TreeMap} with a
 * {@link ValueAccessPointComparator}
 *
 * @author Guillaume Mary
 */
public class ValueAccessPointMap<V> extends TreeMap<ValueAccessPoint, V> {
	
	public ValueAccessPointMap() {
		super(new ValueAccessPointComparator());
	}
	
	public ValueAccessPointMap(Map<? extends ValueAccessPoint, ? extends V> map) {
		this();
		putAll(map);
	}
}
