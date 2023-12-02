package org.codefilarete.reflection;

/**
 * Contract for {@link ValueAccessPoint} classes that provides their {@link AccessorDefinition} by their own.
 * Made to open {@link AccessorDefinition#giveDefinition(ValueAccessPoint)} which only knows a set of known classes.
 * This interface allows to implement {@link #asAccessorDefinition()} to provide a {@link AccessorDefinition}.
 * Useful for anonymous classes or lambda expression.
 *
 * @param <C>
 * @author Guillaume Mary
 */
public interface AccessorDefinitionDefiner<C> extends ValueAccessPoint<C> {
	
	AccessorDefinition asAccessorDefinition();
}
