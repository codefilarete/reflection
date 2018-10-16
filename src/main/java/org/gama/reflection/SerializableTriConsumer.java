package org.gama.reflection;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface SerializableTriConsumer<T, U, V> extends Serializable {
	
	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param t the first input argument
	 * @param u the second input argument
	 */
	void accept(T t, U u, V v);
	
	default SerializableTriConsumer<T, U, V> andThen(SerializableTriConsumer<? super T, ? super U, ? super V> after) {
		Objects.requireNonNull(after);

		return (l, r, u) -> {
			accept(l, r, u);
			after.accept(l, r, u);
		};
	}
}