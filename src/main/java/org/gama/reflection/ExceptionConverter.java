package org.gama.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.gama.lang.StringAppender;
import org.gama.lang.collection.Arrays;
import org.gama.lang.collection.Iterables;
import org.gama.lang.exception.Exceptions;
import org.gama.lang.exception.NotImplementedException;

/**
 * A tool class to convert some exceptions from default JDK to a clearer one, well ... hope so !
 *
 * @author Guillaume Mary
 */
public class ExceptionConverter {
	
	protected RuntimeException convertException(Throwable t, Object target, AbstractReflector reflector, Object... args) {
		if (t instanceof NullPointerException) {
			return new NullPointerException("Cannot invoke " + getReflectorDescription(reflector) + " on null instance");
		} else if (t instanceof InvocationTargetException || t instanceof IllegalAccessException) {
			return Exceptions.asRuntimeException(t.getCause());
		} else if (t instanceof IllegalArgumentException) {
			if ("wrong number of arguments".equals(t.getMessage())) {
				return convertWrongNumberOfArguments(reflector, args);
			} else if ("object is not an instance of declaring class".equals(t.getMessage())) {
				return convertObjectIsNotAnInstanceOfDeclaringClass(target, reflector);
			} else if (t.getMessage().startsWith("Can not set")) {
				return convertCannotSetFieldToObject(target, reflector, args.length > 0 ? args[0] : null);
			} else if ("argument type mismatch".equals(t.getMessage())) {
				return convertArgumentTypeMismatch((IllegalArgumentException) t, reflector, args);
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
			message += giveMessageForWrongNumberOfArguments(((AccessorByMethod) reflector).getGetter(), args);
		}
		return new IllegalArgumentException(message);
	}
	
	public String giveMessageForWrongNumberOfArguments(Method method, Object[] args) {
		TypeAppender parameterFormatter = new TypeAppender(100);
		parameterFormatter.ccat(method.getParameterTypes(), ", ");
		return ": expected " + parameterFormatter.toString()
				+ " but " + (Arrays.isEmpty(args) ? "none" : new StringAppender(100).ccat(args, ", ").wrap("(", ")"))
				+ " was given";
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
			message += giveMessageForConvertObjectIsNotAnInstanceOfDeclaringClass(target, declaringClass);
		}
		return new IllegalArgumentException(message);
	}
	
	public String giveMessageForConvertObjectIsNotAnInstanceOfDeclaringClass(Object target, Class<?> declaringClass) {
		return ": expected " + Reflections.toString(declaringClass) + " but " + Reflections.toString(target.getClass()) + " was given";
	}
	
	private RuntimeException convertCannotSetFieldToObject(Object target, AbstractReflector reflector, Object arg) {
		// Modifying default message because it's not really understandable "Can not set ... to ... "
		Field getter;
		if (reflector instanceof AccessorByField) {
			getter = ((AccessorByField) reflector).getGetter();
		} else if (reflector instanceof MutatorByField) {
			getter = ((MutatorByField) reflector).getSetter();
		} else {
			// this should never happen because this method only handle field access which are handled by previous ifs
			throw new NotImplementedException(reflector.getClass() + " is not handled by this convertor");
		}
		// 2 cases happen here: Object is of wrong type and has no matching field, or boxing of value is wrong
		// (cf https://docs.oracle.com/javase/tutorial/reflect/member/fieldTrouble.html) but we can't distinguish cases
		if (Reflections.findField(target.getClass(), getter.getName()) == null) {
			return new IllegalArgumentException("Field " + Reflections.toString(getter) + " doesn't exist in " + Reflections.toString(target.getClass()));
		} else if (!getter.getType().isInstance(arg)) {
			return new IllegalArgumentException("Field " + Reflections.toString(getter)
					+ " of type " + Reflections.toString(getter.getType()) + " is not compatible with " + (arg == null ? "null" : Reflections.toString(arg.getClass())));
		} else {
			return new RuntimeException("Can not set " + arg + " to " + target);
		}
	}
	
	private IllegalArgumentException convertArgumentTypeMismatch(IllegalArgumentException t, AbstractReflector reflector, Object... args) {
		if (reflector instanceof MutatorByMethod) {
			TypeAppender parameterFormatter = new TypeAppender(100);
			parameterFormatter.cat(getReflectorDescription(reflector), " expects ")
					.ccat(((MutatorByMethod) reflector).getMethod().getParameterTypes(), ", ")
					.cat(" as argument, but ")
					.ccat(Iterables.collectToList(Arrays.asList(args), Object::getClass), ", ")
					.cat(" was given");
			throw new IllegalArgumentException(parameterFormatter.toString());
		} else {
			// actually I'm not sure that something else than a MutatorByMethod can raise an "argument type mismatch" exception
			// so this code may be never get called
			throw t;
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
	
	private static class TypeAppender extends StringAppender {
		
		private TypeAppender(int capacity) {
			super(capacity);
		}
		
		@Override
		public StringAppender cat(Object s) {
			return s instanceof Class ? super.cat(Reflections.toString((Class) s)) : super.cat(s);
		}
	}
	
}
