package org.gama.reflection;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import org.gama.lang.Reflections;
import org.gama.lang.StringAppender;
import org.gama.lang.bean.ClassIterator;
import org.gama.lang.collection.Iterables;

/**
 * A representation of "class member", in a sense of property accessor. So it means Fields, Methods and MethodReferences.
 * Main goal is to make a majority of {@link ValueAccessPoint} comparable between each other even if they are not of same type :
 * a {@link MutatorByField} would have the same {@link MemberDefinition} as an {@link AccessorByMethod} for the same property.
 * 
 * @author Guillaume Mary
 * @see #giveMemberDefinition(ValueAccessPoint)
 */
public class MemberDefinition implements Comparable<MemberDefinition> {
	
	private static final MethodReferenceCapturer METHOD_REFERENCE_CAPTURER = new MethodReferenceCapturer();
	
	/**
	 * Gives a {@link MemberDefinition} that are similar if they access the same property, whatever type they are : doesn't make difference
	 * between a {@link MutatorByField}, {@link AccessorByMethod} or {@link AccessorByMethodReference} if the goal is to access the same field.
	 * 
	 * @param o any {@link ValueAccessPoint}
	 * @return a common representation of given input
	 */
	public static MemberDefinition giveMemberDefinition(ValueAccessPoint o) {
		MemberDefinition result;
		if (o instanceof AbstractReflector) {
			result = giveMemberDefinition((AbstractReflector) o);
		} else if (o instanceof PropertyAccessor) {
			result = giveMemberDefinition((AbstractReflector) ((PropertyAccessor) o).getAccessor());
		} else if (o instanceof AccessorChain) {
			result = giveMemberDefinition((AccessorChain) o);
		} else {
			throw new UnsupportedOperationException("Don't know how find out member definition for " + Reflections.toString(o.getClass()));
		}
		return result;
	}
	
	/**
	 * Dedicated to accessor / mutator by field, method and method reference
	 * @param o one to accessor / mutator by field, method and method reference
	 * @return a {@link MemberDefinition} describing input
	 */
	private static MemberDefinition giveMemberDefinition(AbstractReflector o) {
		String memberName = null;
		Class declarator = null;
		Class memberType = null;
		if (o instanceof ValueAccessPointByField) {
			Field member = ((ValueAccessPointByField) o).getField();
			memberName = member.getName();
			declarator = member.getDeclaringClass();
			memberType = ((Field) member).getType();
		} else if (o instanceof ValueAccessPointByMethod) {
			Method member = ((ValueAccessPointByMethod) o).getMethod();
			memberName = Reflections.propertyName(member.getName());
			declarator = member.getDeclaringClass();
			if (o instanceof IAccessor) {
				memberType = member.getReturnType();
			} else {
				memberType = member.getParameterTypes()[0];
			}
		} else if (o instanceof ValueAccessPointByMethodReference) {
			memberName = Reflections.propertyName(((ValueAccessPointByMethodReference) o).getMethodName());
			declarator = Reflections.forName(((ValueAccessPointByMethodReference) o).getDeclaringClass());
			Method method = METHOD_REFERENCE_CAPTURER.findMethod(((ValueAccessPointByMethodReference) o).getSerializedLambda());
			if (o instanceof IAccessor) {
				memberType = method.getReturnType();
			} else {
				memberType = method.getParameterTypes()[0];
			}
		}
		
		return new MemberDefinition(declarator, memberName, memberType);
	}
	
	/**
	 * Dedicated to {@link AccessorChain}
	 * @param o an {@link AccessorChain}
	 * @return a {@link MemberDefinition} describing input
	 */
	private static MemberDefinition giveMemberDefinition(AccessorChain o) {
		StringAppender stringAppender = new StringAppender() {
			@Override
			public StringAppender cat(Object s) {
				if (s instanceof IAccessor) {
					return super.cat(giveMemberDefinition((IAccessor) s).getName());
				} else {
					return super.cat(s);
				}
			}
		};
		stringAppender.ccat(o.getAccessors(), ".");
		IAccessor firstAccessor = (IAccessor) Iterables.first(o.getAccessors());
		IAccessor lastAccessor = (IAccessor) Iterables.last(o.getAccessors());
		return new MemberDefinition(
				giveMemberDefinition(firstAccessor).getDeclaringClass(),
				stringAppender.toString(),
				giveMemberDefinition(lastAccessor).getMemberType()
		);
	}
	
	
	private final Class declaringClass;
	private final String name;
	private final Class memberType;
	
	/**
	 * Constructor with mandatory attributes
	 * 
	 * @param declaringClass the owning class of the member
	 * @param name name of the member
	 * @param memberType member type (input for setter, return type for getter, type for field)
	 */
	public MemberDefinition(Class declaringClass, String name, Class memberType) {
		this.declaringClass = declaringClass;
		this.name = name;
		this.memberType = memberType;
	}
	
	/**
	 * @param o any {@link ValueAccessPoint}
	 * @return a short representation of the given {@link ValueAccessPoint}
	 */
	public static String toString(ValueAccessPoint o) {
		String result;
		if (o instanceof AccessorByMember) {
			Member member = ((AccessorByMember) o).getGetter();
			if (member instanceof Method) {
				result = Reflections.toString((Method) member);
			} else {
				result = Reflections.toString((Field) member);
			}
		} else if (o instanceof AccessorByMethodReference) {
			result = MethodReferences.toMethodReferenceString(((AccessorByMethodReference) o).getMethodReference());
		} else if (o instanceof MutatorByMember) {
			Member member = ((MutatorByMember) o).getSetter();
			if (member instanceof Method) {
				result = Reflections.toString((Method) member);
			} else {
				result = Reflections.toString((Field) member);
			}
		} else if (o instanceof MutatorByMethodReference) {
			result = MethodReferences.toMethodReferenceString(((MutatorByMethodReference) o).getMethodReference());
		} else if (o instanceof PropertyAccessor) {
			IAccessor accessor = ((PropertyAccessor) o).getAccessor();
			result = toString(accessor);
		} else if (o instanceof AccessorChain) {
			StringAppender chainPrint = new StringAppender();
			((AccessorChain) o).getAccessors().forEach(accessor -> chainPrint.cat(toString((IAccessor) accessor)).cat(" > "));
			result = chainPrint.cutTail(3).toString();
		} else {
			throw new UnsupportedOperationException("Don't know how find out member definition for " + Reflections.toString(o.getClass()));
		}
		return result;
	}
	
	public Class getDeclaringClass() {
		return declaringClass;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Implementation to complies with presence of {@link #compareTo(MemberDefinition)}
	 * 
	 * @param obj another object
	 * @return true if {@link #compareTo(MemberDefinition)} returns 0
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof MemberDefinition) {
			return compareTo((MemberDefinition) obj) == 0;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int result = declaringClass.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + memberType.hashCode();
		return result;
	}
	
	@Override
	public int compareTo(@Nonnull MemberDefinition o) {
		if (name.equals(o.name)) {
			ClassIterator classIterator1 = new ClassIterator(declaringClass);
			ClassIterator classIterator2 = new ClassIterator(o.declaringClass);
			List<Class> copy1 = Iterables.copy(classIterator1);
			List<Class> copy2 = Iterables.copy(classIterator2);
			return Iterables.intersect(copy1, copy2).isEmpty() ? (o.declaringClass.getName().compareTo(declaringClass.getName())) : 0;
		} else {
			return o.name.compareTo(name);
		}
	}
	
	public Class getMemberType() {
		return memberType;
	}
	
	@Override
	public String toString() {
		return Reflections.toString(getDeclaringClass()) + '.' + getName();
	}
}
