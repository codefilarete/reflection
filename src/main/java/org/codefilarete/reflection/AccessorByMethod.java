package org.codefilarete.reflection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Reflections.MemberNotFoundException;
import org.codefilarete.tool.function.ThreadSafeLazyInitializer;

import static org.codefilarete.tool.Reflections.propertyName;

/**
 * {@link Accessor} that wraps a {@link Method} to provide its value.
 * Has {@link ReversibleAccessor} behavior through a lazily initialized internal {@link Mutator}.
 *
 * @author Guillaume Mary
 */
public class AccessorByMethod<C, T> extends AbstractAccessor<C, T>
		implements AccessorByMember<C, T, Method>, ReversibleAccessor<C, T>, ValueAccessPointByMethod<C> {
	
	private final Method getter;
	
	private final Object[] methodParameters;
	
	/** For {@link ReversibleAccessor implementation} */
	private final Supplier<Mutator<C, T>> mutator;
	
	/**
	 * Create an instance based on the given method coordinates.
	 *
	 * @param declaringClass the class that owns the method
	 * @param getterName the method name to be found in given class
	 * @param argTypes optional argument types of the method
	 */
    public AccessorByMethod(Class<C> declaringClass, String getterName, Class ... argTypes) {
        this(Reflections.getMethod(declaringClass, getterName, argTypes));
    }
	
	/**
	 * Create an instance based on the given {@link Method}. The method is expected to take no argument (getter)
	 * @param getter a property accessor
	 */
	public AccessorByMethod(Method getter) {
		this(getter, new Object[getter.getParameterTypes().length]);
	}
	
	/**
	 * Create an instance based on the given {@link Method}. The method is expected to take some arguments, their values are also given now.
	 *
	 * @param getter a property accessor
	 * @param arguments the values to pass to the method when invoking {@link #get(Object)}
	 */
	public AccessorByMethod(Method getter, Object ... arguments) {
		Reflections.ensureAccessible(getter);
		this.getter = getter;
		this.methodParameters = arguments;
		this.mutator = new ThreadSafeLazyInitializer<Mutator<C, T>>() {
			@Override
			protected Mutator<C, T> createInstance() {
				return findCompatibleMutator();
			}
		};
	}
	
	AccessorByMethod(Method getter, Object[] methodParameters, Mutator<C, T> mutator) {
		this.getter = getter;
		this.methodParameters = methodParameters;
		this.mutator = () -> mutator;
	}
	
	/**
	 * Constructor for a getter-equivalent method
	 * 
	 * @param declaringClass type that declares the method
	 * @param getterName name of the accessor
	 */
	public AccessorByMethod(Class<C> declaringClass, String getterName) {
		this(Reflections.getMethod(declaringClass, getterName));
	}
	
	/**
	 * Constructor for a getter that already has an argument value
	 * 
	 * @param declaringClass type that declares the method
	 * @param methodName a one-arg method
	 * @param inputType argument type of the method
	 * @param input the argument value
	 * @param <I> argument type
	 */
	public <I> AccessorByMethod(Class<C> declaringClass, String methodName, Class<I> inputType, I input) {
		this(Reflections.getMethod(declaringClass, methodName, inputType), input);
	}
	
	@Override
	public Method getGetter() {
		return getter;
	}
	
	@Override
	public Method getMethod() {
		return getGetter();
	}
	
	@Override
	public Class<T> getPropertyType() {
		return (Class<T>) getMethod().getReturnType();
	}
	
	@Override
	public T get(C c) {
		return get(c, methodParameters);
	}
	
	/**
	 * Sets parameters
	 * 
	 * @param values expecting to have at least 1 element
	 * @return this
	 */
	public AccessorByMethod<C, T> setParameters(Object ... values) {
		for (int i = 0; i < values.length; i++) {
			setParameter(i, values[i]);
		}
		return this;
	}
	
	/**
	 * Sets parameters at index
	 *
	 * @param index the parameter index to be set
	 * @param value value of the parameter
	 * @return this
	 */
	public AccessorByMethod<C, T> setParameter(int index, Object value) {
		this.methodParameters[index] = value;
		return this;
	}
	
	/**
	 * @param index expected to be positive and in parameters size bound
	 * @return value of the parameter at index
	 */
	public Object getParameter(@Nonnegative int index) {
		return methodParameters[index];
	}
	
	/**
	 * Applies this getter on the given bean, with params.
	 * Parameters already set with {@link #setParameter(int, Object)} or {@link #setParameters(Object...)} won't be used.
	 * 
	 * @param c an Object
	 * @param params arguments
	 * @return result of the called method
	 */
	public T get(@Nonnull C c, Object ... params) {
		try {
			return doGet(c, params);
		} catch (ReflectiveOperationException | RuntimeException t) {
			handleException(t, c, params);
			// shouldn't happen
			return null;
		}
	}
	
	@Override
	// NB: set final to force override doGet(C, Object ...) and so to avoid mistake
	protected final T doGet(C c) throws IllegalAccessException, InvocationTargetException {
		return doGet(c, new Object[0]);
	}
	
	protected T doGet(C c, Object ... args) throws IllegalAccessException, InvocationTargetException {
		return (T) getGetter().invoke(c, args);
	}
	
	@Override
	protected String getGetterDescription() {
		return Reflections.toString(getGetter());
	}
	
	/**
	 * @return a mutator based on the equivalent setter method if exists, else based on field direct access
	 * @throws NonReversibleAccessor if neither setter nor field could be found
	 */
	@Override
	public Mutator<C, T> toMutator() {
		// Note : this will ask for findCompatibleMutator() if not initialized
		return this.mutator.get();
	}
	
	private MutatorByMember<C, T, ? extends Member> findCompatibleMutator() {
		Class<?> declaringClass = getter.getDeclaringClass();
		String propertyName = propertyName(getter);
		MutatorByMethod<C, T> mutatorByMethod = Accessors.mutatorByMethod((Class<C>) declaringClass, propertyName, (Class<T>) getter.getReturnType(), this);
		if (mutatorByMethod == null) {
			try {
				return Accessors.mutatorByField(declaringClass, propertyName, this);
			} catch (MemberNotFoundException e) {
				throw new NonReversibleAccessor("Can't find a mutator for " + Reflections.toString(getter), e);
			}
		} else {
			return mutatorByMethod;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		// We base our implementation on the getter String because a setAccessible() call on the member changes its internal state
		// and I don't think it sould be taken into account for comparison
		// We could base it on getGetterDescription() but it requires more computation
		return this == other || 
				(other instanceof AccessorByMethod
						&& getGetter().toString().equals(((AccessorByMethod) other).getGetter().toString())
						&& Arrays.equals(methodParameters, ((AccessorByMethod) other).methodParameters));
	}
	
	@Override
	public int hashCode() {
		return 31 * getGetter().hashCode() + Arrays.hashCode(methodParameters);
	}
}
