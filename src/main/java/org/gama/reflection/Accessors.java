package org.gama.reflection;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.Reflections;
import org.gama.lang.Strings;
import org.gama.lang.reflect.MethodDispatcher;

/**
 * @author Guillaume Mary
 */
@ParametersAreNonnullByDefault
public final class Accessors {
	
	public static <C, T> AccessorByMethod<C, T> accessorByMethod(Field field) {
		return accessorByMethod(field.getDeclaringClass(), field.getName());
	}
	
	public static <C, T> AccessorByMethod<C, T> accessorByMethod(Class clazz, String propertyName) {
		String capitalizedProperty = Strings.capitalize(propertyName);
		Method getter = Reflections.findMethod(clazz, "get" + capitalizedProperty);
		if (getter == null) {
			// try for boolean
			Field field = Reflections.findField(clazz, propertyName);
			if (field != null && Boolean.class.isAssignableFrom(field.getType())) {
				getter = Reflections.findMethod(clazz, "is" + capitalizedProperty);
			} // nothing found : neither get nor is => return null
		}
		return getter == null ? null : new AccessorByMethod<>(getter);
	}
	
	public static <C, T> AccessorByMethodReference<C, T> accessorByMethodReference(SerializableFunction<C, T> getter) {
		return new AccessorByMethodReference<>(getter);
	}
	
	public static <C, T> IReversibleAccessor<C, T> accessorByMethodReference(SerializableFunction<C, T> getter, SerializableBiConsumer<C, T> setter) {
		AccessorByMethodReference<C, T> fallback = new AccessorByMethodReference<>(getter);
		return new MethodDispatcher()
				// reverse methods are redirected to a redirecting lambda
				.redirect(IReversibleAccessor.class, new IReversibleAccessor<C, T>() {
					@Override
					public IMutator<C, T> toMutator() {
						return new MutatorByMethodReference<>(setter);
					}
					
					@Override
					public T get(C o) {
						return fallback.get(o);
					}
				})
				// fallback goes to default instance
				.fallbackOn(fallback)
				.build(IReversibleAccessor.class);
	}
	
	public static <C, T> AccessorByField<C, T> accessorByField(Field field) {
		return new AccessorByField<>(field);
	}
	
	public static <C, T> AccessorByField<C, T> accessorByField(Class<C> clazz, String propertyName) {
		Field propertyField = Reflections.getField(clazz, propertyName);
		return accessorByField(propertyField);
	}
	
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Field field) {
		return mutatorByMethod((Class<C>) field.getDeclaringClass(), field.getName());
	}
	
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Class<C> clazz, String propertyName) {
		Field propertyField = Reflections.getField(clazz, propertyName);
		Class<?> inputType = propertyField.getType();
		return mutatorByMethod(clazz, propertyName, inputType);
	}
	
	public static <C, T> MutatorByMethodReference<C, T> mutatorByMethodReference(SerializableBiConsumer<C, T> setter) {
		return new MutatorByMethodReference<>(setter);
	}
	
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Class<C> clazz, String propertyName, Class<?> inputType) {
		String capitalizedProperty = Strings.capitalize(propertyName);
		Method setter = Reflections.findMethod(clazz, "set" + capitalizedProperty, inputType);
		return setter == null ? null : new MutatorByMethod<>(setter);
	}
	
	public static <C, T> MutatorByField<C, T> mutatorByField(Field field) {
		return new MutatorByField<>(field);
	}
	
	public static <C, T> MutatorByField<C, T> mutatorByField(Class clazz, String propertyName) {
		Field propertyField = Reflections.getField(clazz, propertyName);
		return mutatorByField(propertyField);
	}
	
	public static Field wrappedField(AccessorByMethod accessorByMethod) {
		Method getter = accessorByMethod.getGetter();
		return Reflections.wrappedField(getter);
	}
	
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
		IAccessor<C, T> propertyGetter = accessorByMethod(clazz, propertyName);
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
		IMutator<C, T> propertySetter = mutatorByMethod(clazz, propertyName);
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
			// Determining if the method is an accessor or a mutator to give the good arguments to the final PropertyAccessor constructor
			Method method = (Method) member;
			AbstractReflector<Object> reflector = Reflections.onJavaBeanPropertyWrapperName(method,
					AccessorByMethod::new,
					MutatorByMethod::new,
					AccessorByMethod::new);
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
	
	private Accessors() {
		// utility class
	}
}
