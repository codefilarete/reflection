package org.gama.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;

import org.gama.lang.Reflections;
import org.gama.lang.StringAppender;
import org.gama.lang.collection.Arrays;
import org.gama.lang.exception.Exceptions;

/**
 * @author Guillaume Mary
 */
public class ExceptionConverter {
	
	protected RuntimeException convertException(Throwable t, Object target, AbstractReflector reflector, Object... args) {
		if (t instanceof NullPointerException) {
			return new NullPointerException("Cannot call " + getReflectorDescription(reflector) + " on null instance");
		} else if (t instanceof InvocationTargetException || t instanceof IllegalAccessException) {
			return Exceptions.asRuntimeException(t.getCause());
		} else if (t instanceof IllegalArgumentException) {
			if ("wrong number of arguments".equals(t.getMessage())) {
				return convertWrongNumberOfArguments(reflector, args);
			} else if("object is not an instance of declaring class".equals(t.getMessage())) {
				return convertObjectIsNotAnInstanceOfDeclaringClass(target, reflector);
			} else if(t.getMessage().startsWith("Can not set")) {
				return convertCannotSetFieldToObject(target, reflector, args.length > 0 ? args[0] : null);
			} else {
				return Exceptions.asRuntimeException(t);
			}
		} else {
			return Exceptions.asRuntimeException(t);
		}
	}
	
	private IllegalArgumentException convertWrongNumberOfArguments(AbstractReflector reflector, Object... args) {
		String message = "wrong number of arguments for " + getReflectorDescription(reflector);
		if (reflector instanceof AccessorByMethod) {
			Class<?>[] parameterTypes = ((AccessorByMethod) reflector).getGetter().getParameterTypes();
			StringAppender parameterFormatter = new StringAppender(100);
			parameterFormatter.ccat(parameterTypes, ", ");
			message += ": expected " + parameterFormatter.toString()
					+ " but " + (Arrays.isEmpty(args) ? "none" : new StringAppender(100).ccat(args, ", ").wrap("(", ")"))
					+ " was given";
		}
		return new IllegalArgumentException(message);
	}
	
	private IllegalArgumentException convertObjectIsNotAnInstanceOfDeclaringClass(Object target, AbstractReflector reflector) {
		String message = "object is not an instance of declaring class";
		if (reflector instanceof AccessorByMember || reflector instanceof MutatorByMember) {
			Member member;
			if (reflector instanceof AccessorByMember) {
				member = ((AccessorByMember) reflector).getGetter();
			} else {
				member = ((MutatorByMember) reflector).getSetter();
			}
			Class<?> declaringClass = member.getDeclaringClass();
			message += ": expected " + declaringClass.getName() + " but was " + target.getClass().getName();
		}
		return new IllegalArgumentException(message);
	}
	
	private RuntimeException convertCannotSetFieldToObject(Object target, AbstractReflector reflector, Object arg) {
		// Modifying default message because it's not really understandable "Can not set ... to ... "
		Field getter = null;
		if (reflector instanceof AccessorByField) {
			getter = ((AccessorByField) reflector).getGetter();
		}
		if (reflector instanceof MutatorByField) {
			getter = ((MutatorByField) reflector).getSetter();
		}
		// 2 cases happen here: Object is of wrong type and has no matching field, or boxing of value is wrong
		// (cf https://docs.oracle.com/javase/tutorial/reflect/member/fieldTrouble.html) but we can't distinguish cases
		if (Reflections.findField(target.getClass(), getter.getName()) == null) {
			return new IllegalArgumentException(target.getClass() + " doesn't have field " + getter.getName());
		} else if (!getter.getType().isInstance(arg)) {
			String fieldDescription = getter.getDeclaringClass().getName() + "." + getter.getName();
			return new IllegalArgumentException("Field " + fieldDescription + " of type " + getter.getType().getName() + " can't be used with " + arg.getClass().getName());
		} else {
			return new RuntimeException("Can't convert exception");
		}
	}
	
	private String getReflectorDescription(AbstractReflector reflector) {
		if (reflector instanceof AbstractAccessor) {
			return ((AbstractAccessor) reflector).getGetterDescription();
		}
		if (reflector instanceof AbstractMutator) {
			return ((AbstractMutator) reflector).getSetterDescription();
		}
		throw new IllegalArgumentException("Unknown reflector " + reflector);
	}
	
}
