package org.gama.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.gama.lang.bean.Objects;

/**
 * @author Guillaume Mary
 */
public class PropertyAccessor<C, T> implements IReversibleAccessor<C, T>, IReversibleMutator<C, T> {
	
	public static <C, T> PropertyAccessor<C, T> forProperty(Field field) {
		return new PropertyAccessor<>(new AccessorByField<>(field), new MutatorByField<>(field));
	}
	
	public static <C, T> PropertyAccessor<C, T> forProperty(Class<C> clazz, String propertyName) {
		IAccessor<C, T> propertyGetter = accessor(clazz, propertyName);
		IMutator<C, T> propertySetter = mutator(clazz, propertyName);
		return new PropertyAccessor<>(propertyGetter, propertySetter);
	}
	
	/**
	 * Create an {@link IAccessor} for the given property of the given class. Do it with conventional getter or a direct access to the field.
	 * 
	 * @param clazz the class owning the property
	 * @param propertyName the name of the property
	 * @param <C> the type of the class owning the property
	 * @param <T> the type of the field (returned by the getter)
	 * @return a new {@link IAccessor}
	 */
	public static <C, T> IAccessor<C, T> accessor(Class<C> clazz, String propertyName) {
		IAccessor<C, T> propertyGetter = Accessors.accessorByMethod(clazz, propertyName);
		if (propertyGetter == null) {
			propertyGetter = new AccessorByField<>(Reflections.findField(clazz, propertyName));
		}
		return propertyGetter;
	}
	
	/**
	 * Create an {@link IMutator} for the given property of the given class. Do it with conventional setter or a direct access to the field.
	 *
	 * @param clazz the class owning the property
	 * @param propertyName the name of the property
	 * @param <C> the type of the class owning the property
	 * @param <T> the type of the field (first parameter of the setter)
	 * @return a new {@link IMutator}
	 */
	public static <C, T> IMutator<C, T> mutator(Class<C> clazz, String propertyName) {
		IMutator<C, T> propertySetter = Accessors.mutatorByMethod(clazz, propertyName);
		if (propertySetter == null) {
			propertySetter = new MutatorByField<>(Reflections.findField(clazz, propertyName));
		}
		return propertySetter;
	}
	
	/**
	 * Give an adequate {@link PropertyAccessor} according to the given {@link Member}
	 * @param member a member to be transformed as a {@link PropertyAccessor}
	 * @param <C> the declaring class of the {@link Member}
	 * @param <T> the type of the {@link Member}
	 * @return a new {@link PropertyAccessor} with accessor and mutator alloqing to access to the member
	 */
	public static <C, T> PropertyAccessor<C, T> of(Member member) {
		if (member instanceof Field) {
			return new PropertyAccessor<>(new AccessorByField<>((Field) member));
		} else if (member instanceof Method) {
			// Determining if the method is an accessor or a mutator for given the good arguments to the final PropertyAccessor constructor
			AbstractReflector<Object> reflector = Reflections.onJavaBeanPropertyWrapperName((Method) member,
					() -> new AccessorByMethod<>((Method) member),
					() -> new MutatorByMethod<>((Method) member),
					() -> new AccessorByMethod<>((Method) member));
			if (reflector instanceof IReversibleAccessor) {
				return new PropertyAccessor<>((IReversibleAccessor<C, T>) reflector);
			} else if (reflector instanceof IReversibleMutator) {
				return new PropertyAccessor<>((IReversibleMutator<C, T>) reflector);
			} else {
				// unreachable because preceding ifs check all conditions
				throw new IllegalArgumentException("Member cannot be determined as a getter or a setter : " + member);
			}
		} else {
			throw new IllegalArgumentException("Member cannot be used as an accessor : " + member);
		}
	}
	
	
	private final IAccessor<C, T> accessor;
	private final IMutator<C, T> mutator;
	
	public PropertyAccessor(IReversibleAccessor<C, T> accessor) {
		this(accessor, accessor.toMutator());
	}
	
	public PropertyAccessor(IReversibleMutator<C, T> mutator) {
		this(mutator.toAccessor(), mutator);
	}
	
	public PropertyAccessor(IAccessor<C, T> accessor, IMutator<C, T> mutator) {
		this.accessor = accessor;
		this.mutator = mutator;
	}
	
	public IAccessor<C, T> getAccessor() {
		return accessor;
	}
	
	public IMutator<C, T> getMutator() {
		return mutator;
	}
	
	@Override
	public T get(C c) {
		return this.accessor.get(c);
	}
	
	public void set(C c, T t) {
		this.mutator.set(c, t);
	}
	
	@Override
	public IAccessor<C, T> toAccessor() {
		return getAccessor();
	}
	
	@Override
	public IMutator<C, T> toMutator() {
		return getMutator();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof PropertyAccessor)) {
			return super.equals(obj);
		} else {
			return Objects.equalsWithNull(this.getAccessor(), ((PropertyAccessor) obj).getAccessor())
					&& Objects.equalsWithNull(this.getMutator(), ((PropertyAccessor) obj).getMutator());
		}
	}
	
	@Override
	public int hashCode() {
		// Implementation based on both accessor and mutator. Accessor is taken first but it doesn't matter
		return 31 * getAccessor().hashCode() + getMutator().hashCode();
	}
}
