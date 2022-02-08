package org.gama.reflection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.danekja.java.util.function.serializable.SerializableFunction;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.bean.Objects;
import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.tool.collection.Collections;
import org.codefilarete.tool.collection.Iterables;

import static org.gama.reflection.Accessors.giveInputType;

/**
 * Chain of {@link Accessor}s that behaves as a {@link Accessor}
 * Behavior of null-encountered-values during {@link #get(Object)} is controlled through a {@link NullValueHandler}, by default {@link NullPointerException}
 * will be thrown, see {@link #setNullValueHandler(NullValueHandler)} to change it. This class proposes some other default behavior such as
 * {@link #RETURN_NULL} or {@link #INITIALIZE_VALUE}
 * 
 * @author Guillaume Mary
 */
public class AccessorChain<C, T> extends AbstractAccessor<C, T> implements ReversibleAccessor<C, T> {
	
	public static <IN, A, OUT> AccessorChain<IN, OUT> chain(SerializableFunction<IN, A> function1, SerializableFunction<A, OUT> function2) {
		return new AccessorChain<>(new AccessorByMethodReference<>(function1), new AccessorByMethodReference<>(function2));
	}
	
	/**
	 * Creates a chain that :
	 * - returns null when any accessor on path returns null
	 * - initializes values (instanciate bean) on path when its mutator is used
	 * (voluntary dissimetric behavior)
	 *
	 * @param accessors list of {@link Accessor} to be used by chain
	 * @see #RETURN_NULL
	 * @see ValueInitializerOnNullValue#giveValueType(Accessor, Class)
	 * @see #forModel(List, BiFunction) 
	 */
	public static <IN, OUT> AccessorChain<IN, OUT> forModel(List<Accessor> accessors) {
		return forModel(accessors, null);
	}
	
	/**
	 * Creates a chain that :
	 * - returns null when any accessor on path returns null
	 * - initializes values (instanciate bean) on path when its mutator is used
	 * (voluntary dissimetric behavior)
	 * 
	 * @param accessors list of {@link Accessor} to be used by chain
	 * @param valueTypeDeterminer must be given if a bean type is badly determined by default mecanism
	 * 		  (returning Object on generic for instance, or wrong Collection concrete type), null accepted (means default mecanism)
	 * @see #RETURN_NULL
	 * @see ValueInitializerOnNullValue#giveValueType(Accessor, Class)
	 */
	public static <IN, OUT> AccessorChain<IN, OUT> forModel(List<Accessor> accessors, @Nullable BiFunction<Accessor, Class, Class> valueTypeDeterminer) {
		return new AccessorChain<IN, OUT>(accessors) {
			
			private final AccessorChainMutator<IN, Object, OUT> mutator = (AccessorChainMutator<IN, Object, OUT>) super.toMutator()
					.setNullValueHandler(new ValueInitializerOnNullValue(valueTypeDeterminer));
			
			@Override
			public AccessorChainMutator toMutator() {
				return mutator;
			}
		}.setNullValueHandler(AccessorChain.RETURN_NULL);
	}
	
	/**
	 * Will throw a {@link NullPointerException} if a link in an accessor chain returns null.
	 * Default behavior
	 */
	public static final NullValueHandler THROW_NULLPOINTEREXCEPTION = new NullPointerExceptionThrowerOnNullValue();
	
	/** Will return null if a link in an accessor chain returns null */
	public static final NullValueHandler RETURN_NULL = new NullReturnerOnNullValue();
	
	/** Will instanciate needed value (and set it) if a link in an accessor chain returns null */
	public static final NullValueHandler INITIALIZE_VALUE = new ValueInitializerOnNullValue();
	
	private final List<Accessor> accessors;
	
	private NullValueHandler nullValueHandler = THROW_NULLPOINTEREXCEPTION;
	
	public AccessorChain() {
		this(new ArrayList<>(5));
	}
	
	public AccessorChain(Accessor... accessors) {
		this(Arrays.asList(accessors));
	}
	
	public AccessorChain(List<Accessor> accessors) {
		this.accessors = accessors;
	}
	
	public List<Accessor> getAccessors() {
		return accessors;
	}
	
	public void add(Accessor accessor) {
		accessors.add(accessor);
	}
	
	public void add(Accessor... accessors) {
		add(Arrays.asList(accessors));
	}
	
	public void add(Iterable<Accessor> accessors) {
		this.accessors.addAll((Collection<Accessor>) accessors);
	}
	
	public AccessorChain<C, T> setNullValueHandler(NullValueHandler nullValueHandler) {
		this.nullValueHandler = nullValueHandler;
		return this;
	}
	
	@Override
	public T doGet(C c) {
		Object target = c;
		Object previousTarget;
		for (Accessor accessor : accessors) {
			previousTarget = target;
			target = accessor.get(target);
			if (target == null) {
				Object handlerResult = onNullValue(previousTarget, accessor);
				if (handlerResult == null) {
					// we must go out from the loop to avoid a NullPointerException, moreover it has no purpose to continue iteration
					return null;
				} else {
					target = handlerResult;
				}
			}
		}
		return (T) target;
	}
	
	/**
	 * Method called when a null value is returned by an accessor in the chain
	 * @param targetBean bean on which accessor was invoked
	 * @param accessor accessor that returned null when invoked on targetBean
	 * @return the value that should replace null value, can be null too
	 */
	@Nullable
	protected Object onNullValue(Object targetBean, Accessor accessor) {
		return this.nullValueHandler.consume(targetBean, accessor);
	}
	
	/**
	 * Only supported when last accessor is reversible (aka implements {@link ReversibleAccessor}.
	 *
	 * @return a new chain which path is the same as this
	 * @throws UnsupportedOperationException if last accessor is not reversible
	 */
	@Override
	public AccessorChainMutator<C, Object, T> toMutator() {
		Accessor lastAccessor = Iterables.last(getAccessors());
		if (lastAccessor instanceof ReversibleAccessor) {
			ReversibleMutator<Object, T> lastMutator = (ReversibleMutator<Object, T>) ((ReversibleAccessor) lastAccessor).toMutator();
			return new AccessorChainMutator<>(Collections.cutTail(getAccessors()), lastMutator);
		} else {
			throw new UnsupportedOperationException("Last accessor cannot be reverted because it's not " + ReversibleAccessor.class.getName()
					+ ": " + lastAccessor);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof AccessorChain && accessors.equals(((AccessorChain) other).accessors));
	}
	
	@Override
	public int hashCode() {
		return accessors.hashCode();
	}
	
	@Override
	protected String getGetterDescription() {
		return accessors.toString();
	}
	
	/**
	 * Contract for handling null objects during accessor chaining
	 */
	public interface NullValueHandler {
		
		Object consume(Object srcBean, Accessor accessor);
		
	}
	
	/**
	 * Class that will throw a {@link NullPointerException} when a null value is encountered
	 */
	private static class NullPointerExceptionThrowerOnNullValue implements NullValueHandler {
		@Override
		public Object consume(Object srcBean, Accessor accessor) {
			String accessorDescription = accessor.toString();
			String exceptionMessage;
			if (accessor instanceof AccessorByField) {
				exceptionMessage = srcBean + " has null value on field " + ((AccessorByField) accessor).getGetter().getName();
			} else {
				exceptionMessage = "Call of " + accessorDescription + " on " + srcBean + " returned null";
			}
			throw new NullPointerException(exceptionMessage);
		}
	}
	
	/**
	 * Simple class that will always return null for the whole chain when a null value is encountered
	 */
	private static class NullReturnerOnNullValue implements NullValueHandler {
		@Override
		public Object consume(Object srcBean, Accessor accessor) {
			return null;
		}
	}
	
	/**
	 * Class that will initialize value by instanciating its class and set it onto the property.
	 * Instanciated types can be controlled through {@link #giveValueType(Accessor, Class)}.
	 */
	public static class ValueInitializerOnNullValue implements NullValueHandler {
		
		private final BiFunction<Accessor, Class, Class> valueTypeDeterminer;
		
		public ValueInitializerOnNullValue() {
			this(null);
		}
		
		public ValueInitializerOnNullValue(@Nullable BiFunction<Accessor, Class, Class> valueTypeDeterminer) {
			this.valueTypeDeterminer = Objects.preventNull(valueTypeDeterminer, ValueInitializerOnNullValue::giveValueType);
		}
		
		@Override
		public Object consume(Object srcBean, Accessor accessor) {
			if (accessor instanceof ReversibleAccessor) {
				Mutator mutator = ((ReversibleAccessor) accessor).toMutator();
				Class inputType = giveInputType(mutator);
				// NB: will throw an exception if type is not instanciable
				Object value = Reflections.newInstance(valueTypeDeterminer.apply(accessor, inputType));
				mutator.set(srcBean, value);
				return value;
			} else {
				throw new UnsupportedOperationException(
						"accessor cannot be reverted because it's not " + Reflections.toString(ReversibleAccessor.class) + ": " + accessor);
			}
		}
		
		/**
		 * Expected to give concrete class to be instanciated.
		 * @param accessor the current accessor that returned null, given for a fine grained adjustment of returned type
		 * @param valueType expected compatible type, this of accessor
		 * @return a concrete and instanciable type compatible with acccessor input type
		 */
		public static Class giveValueType(Accessor accessor, Class valueType) {
			if (List.class.equals(valueType)) {
				return ArrayList.class;
			} else if (Set.class.equals(valueType)) {
				return HashSet.class;
			} else if (Map.class.equals(valueType)) {
				return HashMap.class;
			} else {
				return valueType;
			}
		}
	}
	
	
}
