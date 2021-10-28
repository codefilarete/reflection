package org.gama.reflection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.gama.lang.Reflections;
import org.gama.lang.StringAppender;
import org.gama.lang.bean.ClassIterator;
import org.gama.lang.collection.Iterables;

/**
 * A common representation of "class member", in a sense of property accessor. So it means Fields, Methods and MethodReferences.
 * Main goal is to make a majority of {@link ValueAccessPoint} comparable between each other even if they are not of same type :
 * a {@link MutatorByField} would have the same {@link AccessorDefinition} as an {@link AccessorByMethod} for the same property.
 * 
 * @author Guillaume Mary
 * @see #giveDefinition(ValueAccessPoint)
 */
public class AccessorDefinition implements Comparable<AccessorDefinition> {
	
	private static final MethodReferenceCapturer METHOD_REFERENCE_CAPTURER = new MethodReferenceCapturer();
	
	/**
	 * Gives a {@link AccessorDefinition} that are similar if they access the same property, whatever type they are : doesn't make difference
	 * between a {@link MutatorByField}, {@link AccessorByMethod} or {@link AccessorByMethodReference} if the goal is to access the same field.
	 * 
	 * @param o any {@link ValueAccessPoint}, null autorized but will throw an {@link UnsupportedOperationException}
	 * @return a common representation of given input
	 * @throws UnsupportedOperationException when member can't be found because given {@link ValueAccessPoint} is not a known concrete type
	 */
	public static AccessorDefinition giveDefinition(@Nullable ValueAccessPoint o) {
		AccessorDefinition result;
		if (o instanceof AccessorChain) {
			result = giveDefinition((AccessorChain) o);
		} else if (o instanceof PropertyAccessor) {
			result = giveDefinition(((PropertyAccessor) o).getAccessor());
		} else if (o instanceof AbstractReflector) {
			result = giveDefinition((AbstractReflector) o);
		} else {
			throw new UnsupportedOperationException("Accessor type is unsupported to compute its definition : " + (o == null ? "null" : Reflections.toString(o.getClass())));
		}
		return result;
	}
	
	/**
	 * Dedicated to accessor / mutator by field, method and method reference
	 * @param o one to accessor / mutator by field, method and method reference
	 * @return a {@link AccessorDefinition} describing input
	 */
	private static AccessorDefinition giveDefinition(AbstractReflector o) {
		String memberName = null;
		Class declarator = null;
		Class memberType = null;
		if (o instanceof ValueAccessPointByField) {
			Field member = ((ValueAccessPointByField) o).getField();
			memberName = member.getName();
			declarator = member.getDeclaringClass();
			memberType = member.getType();
		} else if (o instanceof ValueAccessPointByMethod) {
			Method member = ((ValueAccessPointByMethod) o).getMethod();
			memberName = Reflections.propertyName(member.getName());
			declarator = member.getDeclaringClass();
			if (o instanceof Accessor) {
				memberType = member.getReturnType();
			} else {
				memberType = member.getParameterTypes()[0];
			}
		} else if (o instanceof ValueAccessPointByMethodReference) {
			memberName = Reflections.propertyName(((ValueAccessPointByMethodReference) o).getMethodName());
			declarator = ((ValueAccessPointByMethodReference) o).getDeclaringClass();
			Method method = METHOD_REFERENCE_CAPTURER.findMethod(((ValueAccessPointByMethodReference) o).getSerializedLambda());
			if (o instanceof Accessor) {
				memberType = method.getReturnType();
			} else {
				memberType = method.getParameterTypes()[0];
			}
		}
		
		return new AccessorDefinition(declarator, memberName, memberType);
	}
	
	/**
	 * Dedicated to {@link AccessorChain}
	 * @param o an {@link AccessorChain}
	 * @return a {@link AccessorDefinition} describing input
	 */
	private static AccessorDefinition giveDefinition(AccessorChain o) {
		StringAppender stringAppender = new StringAppender() {
			@Override
			public StringAppender cat(Object s) {
				if (s instanceof Accessor) {
					return super.cat(giveDefinition((Accessor) s).getName());
				} else {
					return super.cat(s);
				}
			}
		};
		stringAppender.ccat(o.getAccessors(), ".");
		Accessor firstAccessor = (Accessor) Iterables.first(o.getAccessors());
		Accessor lastAccessor = (Accessor) Iterables.last(o.getAccessors());
		return new AccessorDefinition(
				giveDefinition(firstAccessor).getDeclaringClass(),
				stringAppender.toString(),
				giveDefinition(lastAccessor).getMemberType()
		);
	}
	
	/**
	 * @param o any {@link ValueAccessPoint}
	 * @return a short representation of the given {@link ValueAccessPoint} : owner + name (spearator depends on accessor kind)
	 */
	public static String toString(@Nullable ValueAccessPoint o) {
		String result;
		if (o == null) {
			result = "null";
		} else if (o instanceof AccessorByMember) {
			result = toString(((AccessorByMember) o).getGetter());
		} else if (o instanceof AccessorByMethodReference) {
			result = MethodReferences.toMethodReferenceString(((AccessorByMethodReference) o).getMethodReference());
		} else if (o instanceof MutatorByMember) {
			result = toString(((MutatorByMember) o).getSetter());
		} else if (o instanceof MutatorByMethodReference) {
			result = MethodReferences.toMethodReferenceString(((MutatorByMethodReference) o).getMethodReference());
		} else if (o instanceof PropertyAccessor) {
			result = toString(((PropertyAccessor) o).getAccessor());
		} else if (o instanceof AccessorChain) {
			StringAppender chainPrint = new StringAppender();
			((AccessorChain) o).getAccessors().forEach(accessor -> chainPrint.cat(toString((Accessor) accessor)).cat(" > "));
			result = chainPrint.cutTail(3).toString();
		} else {
			throw new UnsupportedOperationException("Don't know how find out member definition for " + Reflections.toString(o.getClass()));
		}
		return result;
	}
	
	private static String toString(Member member) {
		String result;
		if (member instanceof Method) {
			result = Reflections.toString((Method) member);
		} else {
			result = Reflections.toString((Field) member);
		}
		return result;
	}
	
	/**
	 * @param accessPoints several {@link ValueAccessPoint}s
	 * @return the concatenation of each call to {@link #toString(ValueAccessPoint)} for every element of the collection, separated by ">"
	 */
	public static String toString(Collection<ValueAccessPoint> accessPoints) {
		StringAppender chainPrint = new StringAppender();
		accessPoints.forEach(accessor -> chainPrint.cat(toString(accessor)).cat(" > "));
		return chainPrint.cutTail(3).toString();
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
	public AccessorDefinition(Class declaringClass, String name, Class memberType) {
		this.declaringClass = declaringClass;
		this.name = name;
		this.memberType = memberType;
	}
	
	public <T> Class<T> getDeclaringClass() {
		return declaringClass;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Implementation to complies with presence of {@link #compareTo(AccessorDefinition)}
	 * 
	 * @param obj another object
	 * @return true if {@link #compareTo(AccessorDefinition)} returns 0
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof AccessorDefinition) {
			return compareTo((AccessorDefinition) obj) == 0;
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
	public int compareTo(@Nonnull AccessorDefinition o) {
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
