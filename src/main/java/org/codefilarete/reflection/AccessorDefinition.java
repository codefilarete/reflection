package org.codefilarete.reflection;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;

import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.StringAppender;
import org.codefilarete.tool.collection.Iterables;

/**
 * A common representation of "class member", in the meaning of property accessor. So it means Fields, Methods and MethodReferences.
 * Main goal is to make a majority of {@link ValueAccessPoint} comparable between each other even if they are not of same type :
 * a {@link MutatorByField} would have the same {@link AccessorDefinition} than an {@link AccessorByMethod} for the same property.
 * 
 * @author Guillaume Mary
 * @see #giveDefinition(ValueAccessPoint)
 */
public class AccessorDefinition {
	
	private static final MethodReferenceCapturer METHOD_REFERENCE_CAPTURER = new MethodReferenceCapturer();
	
	/**
	 * Gives an {@link AccessorDefinition} defining given {@link ValueAccessPoint}.
	 * 
	 * @param accessPoint any {@link ValueAccessPoint}
	 * @return a common representation of given input
	 * @throws UnsupportedOperationException when member can't be found because given {@link ValueAccessPoint} is not a known concrete type
	 */
	public static AccessorDefinition giveDefinition(ValueAccessPoint<?> accessPoint) {
		AccessorDefinition result;
		if (accessPoint instanceof AccessorChain) {
			result = giveDefinition((AccessorChain) accessPoint);
		} else if (accessPoint instanceof PropertyAccessor) {
			result = giveDefinition(((PropertyAccessor) accessPoint).getAccessor());
		} else if (accessPoint instanceof AbstractReflector) {
			result = giveDefinition((AbstractReflector) accessPoint);
		} else if (accessPoint instanceof AccessorDefinitionDefiner) {
			result = ((AccessorDefinitionDefiner) accessPoint).asAccessorDefinition();
		} else {
			throw new UnsupportedOperationException("Accessor type is unsupported to compute its definition : " + (accessPoint == null ? "null" : Reflections.toString(accessPoint.getClass())));
		}
		return result;
	}
	
	/**
	 * Gives an {@link AccessorDefinition} defining given {@link Method}.
	 *
	 * @param method any {@link Method}
	 * @return a common representation of given input
	 */
	public static AccessorDefinition giveDefinition(Method method) {
		return new AccessorDefinition(method.getDeclaringClass(), Reflections.propertyName(method), method.getReturnType());
	}
	
	/**
	 * Gives an {@link AccessorDefinition} defining given {@link Field}.
	 *
	 * @param field any {@link Field}
	 * @return a common representation of given input
	 */
	public static AccessorDefinition giveDefinition(Field field) {
		return new AccessorDefinition(field.getDeclaringClass(), field.getName(), field.getType());
	}
	
	/**
	 * Dedicated to accessor / mutator by field, method and method reference
	 * @param o any accessor / mutator by field, method and method reference
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
			Method member = ((ValueAccessPointByMethod<?>) o).getMethod();
			memberName = Reflections.propertyName(member.getName());
			declarator = member.getDeclaringClass();
			if (o instanceof Accessor) {
				memberType = member.getReturnType();
			} else {
				memberType = member.getParameterTypes()[0];
			}
		} else if (o instanceof ValueAccessPointByMethodReference) {
			memberName = Reflections.propertyName(((ValueAccessPointByMethodReference<?>) o).getMethodName());
			declarator = ((ValueAccessPointByMethodReference<?>) o).getDeclaringClass();
			Method method = METHOD_REFERENCE_CAPTURER.findMethod(((ValueAccessPointByMethodReference<?>) o).getSerializedLambda());
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
	private static AccessorDefinition giveDefinition(AccessorChain<?, ?> o) {
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
		Accessor firstAccessor = Iterables.first(o.getAccessors());
		Accessor lastAccessor = Iterables.last(o.getAccessors());
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
	public static String toString(@Nullable ValueAccessPoint<?> o) {
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
	public static String toString(Collection<ValueAccessPoint<?>> accessPoints) {
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
	 * Implementation based on strict equality of {@link #getDeclaringClass()}, {@link #getName()} and {@link #getMemberType()}
	 * 
	 * @param obj another object
	 * @return true if {@link #getDeclaringClass()}, {@link #getName()} and {@link #getMemberType()} of current instance
	 * and given one are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof AccessorDefinition) {
			AccessorDefinition other = (AccessorDefinition) obj;
			
			return other.getDeclaringClass().equals(this.getDeclaringClass())
					&& other.getName().equals(this.getName())
					&& other.getMemberType().equals(this.getMemberType());
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
	
	public Class getMemberType() {
		return memberType;
	}
	
	@Override
	public String toString() {
		return Reflections.toString(getDeclaringClass()) + '.' + getName();
	}
}
