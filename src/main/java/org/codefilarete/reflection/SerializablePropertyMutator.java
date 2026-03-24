package org.codefilarete.reflection;

/**
 * A dedicated version of {@link SerializableMutator} for bean property : since there's no way to constraint a method
 * reference to target a bean property, the only way is to highlight it through some well-named interface, which is
 * what this interface does.
 *
 * @param <C> the type of the class declaring the property to be accessed
 * @param <T> the type of the property to be accessed
 * @author Guillaume Mary
 */
@FunctionalInterface
public interface SerializablePropertyMutator<C, T> extends PropertyMutator<C, T>, SerializableMutator<C, T> {
}
