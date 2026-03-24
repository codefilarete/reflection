package org.codefilarete.reflection;

/**
 * Equivalent of {@link ReadWriteAccessPoint} dedicated to property bean.
 *
 *
 * @param <C> the class declaring the property
 * @param <T> the property type
 */
public interface ReadWritePropertyAccessPoint<C, T>
		extends
		PropertyAccessor<C, T>, PropertyMutator<C, T>, ReadWriteAccessPoint<C, T>
{
	
	@Override
	PropertyAccessor<C, T> getReader();
	
	@Override
	PropertyMutator<C, T> getWriter();
	
	@Override
	default T get(C c) {
		return getReader().get(c);
	}
	
	@Override
	default void set(C c, T t) {
		getWriter().set(c, t);
	}
}
