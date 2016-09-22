package org.gama.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.gama.lang.Strings;

/**
 * @author Guillaume Mary
 */
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
	
	public static <C, T> AccessorByField<C, T> accessorByField(Field field) {
		return new AccessorByField<>(field);
	}
	
	public static <C, T> AccessorByField<C, T> accessorByField(Class<C> clazz, String propertyName) {
		Field propertyField = Reflections.findField(clazz, propertyName);
		return accessorByField(propertyField);
	}
	
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Field field) {
		return mutatorByMethod((Class<C>) field.getDeclaringClass(), field.getName());
	}
	
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Class<C> clazz, String propertyName) {
		Field propertyField = Reflections.findField(clazz, propertyName);
		Class<?> inputType = propertyField.getType();
		return mutatorByMethod(clazz, propertyName, inputType);
	}
	
	public static <C, T> MutatorByMethod<C, T> mutatorByMethod(Class<C> clazz, String propertyName, Class<?> inputType) {
		String capitalizedProperty = Strings.capitalize(propertyName);
		Method setter = Reflections.findMethod(clazz, "set" + capitalizedProperty, inputType);
		return setter == null ? null : new MutatorByMethod<>(setter);
	}
	
	public static <C, T> MutatorByField<C, T> mutatorByField(Field field) {
		return new MutatorByField<>(field);
	}
	
	public static <C, T> MutatorByField<C, T> mutatorByField(Class clazz, String propertyName) throws NoSuchFieldException {
		Field propertyField = Reflections.findField(clazz, propertyName);
		if (propertyField == null) {
			throw new NoSuchFieldException("Class " + clazz.getName() + " doesn't have a field name " + propertyName);
		}
		return mutatorByField(propertyField);
	}
	
	public static Field wrappedField(AccessorByMethod accessorByMethod) {
		Method getter = accessorByMethod.getGetter();
		return wrappedField(getter);
	}
	
	public static Field wrappedField(Method fieldWrapper) {
		String propertyName = propertyName(fieldWrapper);
		return Reflections.findField(fieldWrapper.getDeclaringClass(), propertyName);
	}
	
	public static String propertyName(Method fieldWrapper) {
		String methodName = fieldWrapper.getName();
		String propertyName;
		propertyName = Reflections.onJavaBeanPropertyWrapperName(fieldWrapper, () -> methodName.substring(3), () -> methodName.substring(3), () -> methodName.substring(2));
		propertyName = Strings.uncapitalize(propertyName);
		return propertyName;
	}
	
	public static IAccessor of(Member member) {
		if (member instanceof Field) {
			return new AccessorByField((Field) member);
		} else if (member instanceof Method) {
			return new AccessorByMethod((Method) member);
		} else {
			throw new IllegalArgumentException("Member cannot be used as an accessor : " + member);
		}
	}
	
	private Accessors() {
	}
}
