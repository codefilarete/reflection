package org.gama.reflection;

import java.util.function.BiConsumer;

/**
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface IMutator<C, T> {
	
	void set(C c, T t);
	
	static <C, T> IMutator<C, T> mutator(BiConsumer<C, T> consumer) {
		return consumer::accept;
	}
}
