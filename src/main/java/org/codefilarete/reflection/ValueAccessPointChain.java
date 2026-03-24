package org.codefilarete.reflection;

import java.util.List;

import static org.codefilarete.reflection.AccessorChain.*;

/**
 * Common contract for chain of {@link ValueAccessPoint}
 *
 * @author Guillaume Mary
 */
public interface ValueAccessPointChain {
	
	List<? extends ValueAccessPoint<?>> getAccessors();
	
	ValueAccessPointChain setNullValueHandler(NullValueHandler nullValueHandler);
}
