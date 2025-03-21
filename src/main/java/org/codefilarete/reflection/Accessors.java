package org.codefilarete.reflection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Reflections.MemberNotFoundException;
import org.codefilarete.tool.Strings;
import org.codefilarete.tool.collection.Iterables;
import org.codefilarete.tool.reflect.MethodDispatcher;

import static org.codefilarete.tool.Reflections.propertyName;

/**
 * @author Guillaume Mary
 */
@ParametersAreNonnullByDefault
public final class Accessors {
	
	/**
	 * Helper to get method input type. Set as static to benefit from its cache.
	 */
	private static final MethodReferenceCapturer methodCapturer = new MethodReferenceCapturer();
	
	public static <C, T> AccessorByMethod<C, T> accessorByMethod(Field field) {
		Method getter = findGetter(field.getDeclaringClass(), field.getName(), field.getType());
		return getter == null ? null : new AccessorByMethod<>(getter);
	}
	
	/**
	 * Shortcut to create a {@link AccessorByMethod} from a class and a property name.
	 * Java bean naming convention will be applied to find out property getter name : prefixed with "get" or "is".
	 * Returns null is getter method is not found.
	 *
	 * @param clazz any class 
	 * @param propertyName a property name owned by the class or one of its parent
	 * @param <C> owning class type
	 * @param <T> getter return type, which is property type too
	 * @return null if getter method is not found
	 */
	public static <C, T> AccessorByMethod<C, T> accessorByMethod(Class clazz, String propertyName) {
		Field field = Reflections.findField(clazz, propertyName);
		return accessorByMethod(field);
	}
	
	public static <C, T> AccessorByMethod<C, T> accessorByMethod(Class clazz, String propertyName, Class<T> inputType) {
		Method getter = findGetter(clazz, propertyName, inputType);
		return getter == null ? null : new AccessorByMethod<>(getter);
	}
	
	@Nullable
	public static <C, T> AccessorByMethod<C, T> accessorByMethod(Class clazz, String propertyName, Class<T> inputType, Mutator<C, T> mutator) {
		Method getter = findGetter(clazz, propertyName, inputType);
		if (getter == null) {
			return null;
		} else {
			Reflections.ensureAccessible(getter);
			return new AccessorByMethod<>(getter, new Object[getter.getParameterTypes().length], mutator);
		}
	}
	
	@Nullable
	private static <T> Method findGetter(Class clazz, String propertyName, Class<T> inputType) {
		String capitalizedProperty = Strings.capitalize(propertyName);
		String methodPrefix;
		if (boolean.class.isAssignableFrom(inputType) || Boolean.class.isAssignableFrom(inputType)) {
			methodPrefix = "is";
		} else {
			methodPrefix = "get";
		}
		return Reflections.findMethod(clazz, methodPrefix + capitalizedProperty);
	}
	
	public static <C, T> AccessorByMethodReference<C, T> accessorByMethodReference(SerializableFunction<C, T> getter) {
		return new AccessorByMethodReference<>(getter);
	}
	
	public static <C, T> ReversibleAccessor<C, T> accessorByMethodReference(SerializableFunction<C, T> getter, SerializableBiConsumer<C, T> setter) {
		AccessorByMethodReference<C, T> fallback = new AccessorByMethodReference<>(getter);
		return new MethodDispatcher()
				// reverse methods are redirected to a redirecting lambda
				.redirect(ReversibleAccessor.class, new ReversibleAccessor<C, T>() {
					@Override
					public Mutator<C, T> toMutator() {
						return new MutatorByMethodReference<>(setter);
					}
					
					@Override
					public T get(C o) {
						return fallback.get(o);
					}
				})
				// fallback goes to default instance
				.fallbackOn(fallback)
				.build(ReversibleAccessor.class);
	}
	
	public static <C, T> AccessorByField<C, T> accessorByField(Field field) {
		return new AccessorByField<>(field);
	}
	
	public static <C, T> AccessorByField<C, T> accessorByField(Class<C> clazz, String propertyName) {
		Field propertyField = Reflections.getField(clazz, propertyName);
		return accessorByField(propertyField);
	}
	
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Field field) {
		// we do our best : no argument is given because we couldn't determine it
		return new MutatorByMethod<>(Reflections.getMethod(field.getDeclaringClass(), "set" + Strings.capitalize(field.getName()), field.getType()));
	}
	
	/**
	 * Shortcut to create a {@link MutatorByMethod} from a class and a property name.
	 * Java bean naming convention will be applied to find out property setter name : prefixed with "set".
	 * Returns null is setter method is not found.
	 * 
	 * @param clazz any class 
	 * @param propertyName a property name owned by the class or one of its parent
	 * @param <C> owning class type
	 * @param <T> setter input type, which is property type too
	 * @return null if setter method is not found
	 */
	@Nullable
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Class<C> clazz, String propertyName) {
		Field field = Reflections.getField(clazz, propertyName);
		try {
			return mutatorByMethod(field);
		} catch (MemberNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Shortcut to create a {@link MutatorByMethod} from a class, a property name, and its type.
	 * Java bean naming convention will be applied to find out property setter name : prefixed with "set".
	 * Returns null is setter method is not found.
	 *
	 * @param clazz any class 
	 * @param propertyName a property name owned by the class or one of its parent
	 * @param inputType property type
	 * @param <C> owning class type
	 * @param <T> setter input type, which is property type too
	 * @return null if setter method is not found
	 */
	@Nullable
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Class<C> clazz, String propertyName, Class<T> inputType) {
		String capitalizedProperty = Strings.capitalize(propertyName);
		Method setter = Reflections.findMethod(clazz, "set" + capitalizedProperty, inputType);
		return setter == null ? null : new MutatorByMethod<>(setter);
	}
	
	@Nullable
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Class<C> clazz, String propertyName, Class<T> inputType, Accessor<C, T> accessor) {
		String capitalizedProperty = Strings.capitalize(propertyName);
		Method setter = Reflections.findMethod(clazz, "set" + capitalizedProperty, inputType);
		if (setter == null) {
			return null;
		} else {
			Reflections.ensureAccessible(setter);
			return new MutatorByMethod<>(setter, accessor);
		}
	}
	
	public static <C, T> MutatorByMethodReference<C, T> mutatorByMethodReference(SerializableBiConsumer<C, T> setter) {
		return new MutatorByMethodReference<>(setter);
	}
	
	public static <C, T> MutatorByField<C, T> mutatorByField(Field field) {
		return new MutatorByField<>(field);
	}
	
	public static <C, T> MutatorByField<C, T> mutatorByField(Class clazz, String propertyName) {
		Field propertyField = Reflections.getField(clazz, propertyName);
		return mutatorByField(propertyField);
	}
	
	public static <C, T> MutatorByField<C, T> mutatorByField(Class clazz, String propertyName, Accessor<C, T> accessor) {
		Field propertyField = Reflections.getField(clazz, propertyName);
		Reflections.ensureAccessible(propertyField);
		return new MutatorByField<>(propertyField, accessor);
	}
	
	public static Field wrappedField(AccessorByMethod accessorByMethod) {
		Method getter = accessorByMethod.getGetter();
		return Reflections.wrappedField(getter);
	}
	
	public static <C, T> PropertyAccessor<C, T> propertyAccessor(Field field) {
		return new PropertyAccessor<>(new AccessorByField<>(field), new MutatorByField<>(field));
	}
	
	public static <C, T> PropertyAccessor<C, T> propertyAccessor(Class<C> clazz, String propertyName) {
		AccessorByMember<C, T, ?> propertyGetter = accessor(clazz, propertyName);
		Mutator<C, T> propertySetter = mutator(clazz, propertyName, propertyGetter.getPropertyType());
		return new PropertyAccessor<>(propertyGetter, propertySetter);
	}
	
	/**
	 * Creates an {@link Accessor} for the given property of the given class. Does it with conventional getter or a direct access to the field.
	 * 
	 * @param clazz the class owning the property
	 * @param propertyName the name of the property
	 * @param <C> the type of the class owning the property
	 * @param <T> the type of the field (returned by the getter)
	 * @return a new {@link Accessor}
	 */
	public static <C, T, M extends Member> AccessorByMember<C, T, M> accessor(Class<C> clazz, String propertyName) {
		AccessorByMember<C, T, ?> propertyGetter = accessorByMethod(clazz, propertyName);
		if (propertyGetter == null) {
			// NB: we use getField instead of findField because the latest returns null if field wasn't found
			// so AccessorByField will throw a NPE later
			propertyGetter = new AccessorByField<>(Reflections.getField(clazz, propertyName));
		}
		return (AccessorByMember<C, T, M>) propertyGetter;
	}
	
	/**
	 * Creates an {@link Accessor} for the given property of the given class. Does it with conventional getter or a direct access to the field.
	 * 
	 * @param clazz the class owning the property
	 * @param propertyName the name of the property
	 * @param <C> the type of the class owning the property
	 * @param <T> the type of the field (returned by the getter)
	 * @return a new {@link Accessor}
	 */
	public static <C, T, M extends Member> AccessorByMember<C, T, M> accessor(Class<C> clazz, String propertyName, Class<T> propertyType) {
		AccessorByMember<C, T, ?> propertyGetter = accessorByMethod(clazz, propertyName);
		if (propertyGetter == null) {
			// NB: we use getField instead of findField because the latest returns null if field wasn't found
			// so AccessorByField will throw a NPE later
			Field foundField = Reflections.getField(clazz, propertyName);
			if (!Reflections.isAssignableFrom(propertyType, foundField.getType())) {
				throw new MemberNotFoundException(
						Reflections.toString(clazz) + "." + propertyName,
						"Member type doesn't match expected one for field " + Reflections.toString(foundField)
						+ ": expected " + Reflections.toString(propertyType) + " but is " + Reflections.toString(foundField.getType()));
			}
			propertyGetter = new AccessorByField<>(foundField);
		}
		return (AccessorByMember<C, T, M>) propertyGetter;
	}
	
	/**
	 * Creates an {@link Mutator} for the given property of the given class. Does it with conventional setter or a direct access to the field.
	 *
	 * @param clazz the class owning the property
	 * @param propertyName the name of the property
	 * @param <C> the type of the class owning the property
	 * @param <T> the type of the field (first parameter of the setter)
	 * @return a new {@link Mutator}
	 */
	public static <C, T, M extends Member> MutatorByMember<C, T, M> mutator(Class<C> clazz, String propertyName) {
		Field field = Reflections.getField(clazz, propertyName);
		try {
			return (MutatorByMember<C, T, M>) mutatorByMethod(field);
		} catch (MemberNotFoundException e) {
			return (MutatorByMember<C, T, M>) new MutatorByField<>(field);
		}
	}
	
	/**
	 * Creates an {@link Mutator} for the given property of the given class. Does it with conventional setter or a direct access to the field.
	 *
	 * @param clazz the class owning the property
	 * @param propertyName the name of the property
	 * @param <C> the type of the class owning the property
	 * @param <T> the type of the field (first parameter of the setter)
	 * @return a new {@link Mutator}
	 */
	public static <C, T, M extends Member> MutatorByMember<C, T, M> mutator(Class<C> clazz, String propertyName, Class<T> propertyType) {
		MutatorByMember<C, T, ?> propertySetter = mutatorByMethod(clazz, propertyName, propertyType);
		if (propertySetter == null) {
			// NB: we use getField instead of findField because the latest returns null if field wasn't found
			// so AccessorByField will throw a NPE later
			Field foundField = Reflections.getField(clazz, propertyName);
			if (!Reflections.isAssignableFrom(propertyType, foundField.getType())) {
				throw new MemberNotFoundException(
						Reflections.toString(clazz) + "." + propertyName,
						"Member type doesn't match expected one for field " + Reflections.toString(foundField)
						+ ": expected " + Reflections.toString(propertyType) + " but is " + Reflections.toString(foundField.getType()));
			}
			propertySetter = new MutatorByField<>(foundField);
		}
		return (MutatorByMember<C, T, M>) propertySetter;
	}
	
	public static <C, E> PropertyAccessor<C, E> accessor(SerializableFunction<C, E> getter) {
		AccessorByMethodReference<C, E> methodReference = accessorByMethodReference(getter);
		return new PropertyAccessor<>(
				methodReference,
				mutator(methodReference.getDeclaringClass(), propertyName(methodReference.getMethodName()), methodReference.getPropertyType())
		);
	}
	
	public static <C, E> PropertyAccessor<C, E> mutator(SerializableBiConsumer<C, E> setter) {
		MutatorByMethodReference<C, E> methodReference = mutatorByMethodReference(setter);
		return new PropertyAccessor<>(
				accessor(methodReference.getDeclaringClass(), propertyName(methodReference.getMethodName()), methodReference.getPropertyType()),
				methodReference
		);
	}
	
	/**
	 * Gives an adequate {@link PropertyAccessor} according to the given {@link Member}
	 * @param member a member to be transformed as a {@link PropertyAccessor}
	 * @param <C> the declaring class of the {@link Member}
	 * @param <T> the type of the {@link Member}
	 * @return a new {@link PropertyAccessor} with accessor and mutator alloqing to access to the member
	 */
	public static <C, T> PropertyAccessor<C, T> accessor(Member member) {
		if (member instanceof Field) {
			return new PropertyAccessor<>(new AccessorByField<>((Field) member));
		} else if (member instanceof Method) {
			// Determining if the method is an accessor or a mutator to give the good arguments to the final PropertyAccessor constructor
			Method method = (Method) member;
			AbstractReflector<Object> reflector = Reflections.onJavaBeanPropertyWrapperName(method,
					AccessorByMethod::new,
					MutatorByMethod::new,
					AccessorByMethod::new);
			if (reflector instanceof ReversibleAccessor) {
				return new PropertyAccessor<>((ReversibleAccessor<C, T>) reflector);
			} else if (reflector instanceof ReversibleMutator) {
				return new PropertyAccessor<>((ReversibleMutator<C, T>) reflector);
			} else {
				// unreachable because preceding ifs check all conditions
				throw new IllegalArgumentException("Member cannot be determined as a getter or a setter : " + member);
			}
		} else {
			throw new IllegalArgumentException("Member cannot be used as an accessor : " + member);
		}
	}
	
	
	/**
	 * Gives input type of a mutator. Implementation is based on well-known mutator classes and is not expected to be generic
	 * 
	 * @param mutator any {@link Mutator}
	 * @return input type of a mutator : input value of a setter and type of a field
	 */
	public static Class giveInputType(Mutator mutator) {
		if (mutator instanceof MutatorByMember) {
			Member member = ((MutatorByMember) mutator).getSetter();
			if (member instanceof Method) {
				return ((Method) member).getParameterTypes()[0];
			} else if (member instanceof Field) {
				return ((Field) member).getType();
			} else {
				// for future new MutatorByMember that are neither a Field nor a Method ... should not happen 
				throw new UnsupportedOperationException("Mutator type is not implemented : " + mutator);
			}
		} else if (mutator instanceof MutatorByMethodReference) {
			return methodCapturer.findMethod(((MutatorByMethodReference) mutator).getMethodReference()).getParameterTypes()[0];
		} else if (mutator instanceof PropertyAccessor) {
			return giveInputType(((PropertyAccessor) mutator).getMutator());
		} else if (mutator instanceof AccessorChainMutator) {
			return giveInputType(((AccessorChainMutator) mutator).getMutator());
		} else {
			// for future new MutatorByMember that are neither a Field nor a Method ... should not happen 
			throw new UnsupportedOperationException("Mutator type is not implemented : " + mutator);
		}
	}
	
	/**
	 * Gives input type of a mutator. Implementation is based on well-known mutator classes and is not expected to be generic
	 * 
	 * @param accessor any {@link Accessor}
	 * @return input type of a mutator : input value of a setter and type of a field
	 */
	public static Class giveReturnType(Accessor accessor) {
		if (accessor instanceof AccessorByMember) {
			Member member = ((AccessorByMember) accessor).getGetter();
			if (member instanceof Method) {
				return ((Method) member).getReturnType();
			} else if (member instanceof Field) {
				return ((Field) member).getType();
			} else {
				// for future new MutatorByMember that are neither a Field nor a Method ... should not happen 
				throw new UnsupportedOperationException("Mutator type is not implemented : " + accessor);
			}
		} else if (accessor instanceof AccessorByMethodReference) {
			return methodCapturer.findMethod(((AccessorByMethodReference) accessor).getMethodReference()).getReturnType();
		} else if (accessor instanceof PropertyAccessor) {
			return giveReturnType(((PropertyAccessor) accessor).getAccessor());
		} else if (accessor instanceof AccessorChain) {
			return giveReturnType(Iterables.last((List<Accessor>) ((AccessorChain) accessor).getAccessors()));
		} else {
			// for future new MutatorByMember that are neither a Field nor a Method ... should not happen 
			throw new UnsupportedOperationException("Accessor type is not implemented : " + accessor);
		}
	}
	
	private Accessors() {
		// utility class
	}
}
