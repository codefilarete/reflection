package org.codefilarete.reflection;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.bean.Objects;
import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.tool.collection.Iterables;
import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * Chain of {@link Accessor}s that behaves as a {@link Accessor}
 * Behavior of null-encountered-values during {@link #get(Object)} is controlled through a {@link NullValueHandler}, by default {@link NullPointerException}
 * will be thrown, see {@link #setNullValueHandler(NullValueHandler)} to change it. This class proposes some other default behavior such as
 * {@link #RETURN_NULL} or {@link #INITIALIZE_VALUE}
 * 
 * @author Guillaume Mary
 */
public class AccessorChain<C, T> extends AbstractAccessor<C, T> implements ReversibleAccessor<C, T> {
	
	public static <IN, OUT> AccessorChain<IN, OUT> chain(SerializableFunction<IN, OUT> function1) {
		return new AccessorChain<>(new AccessorByMethodReference<>(function1));
	}
	
	public static <IN, A, OUT> AccessorChain<IN, OUT> chain(SerializableFunction<IN, A> function1, SerializableFunction<A, OUT> function2) {
		return new AccessorChain<>(new AccessorByMethodReference<>(function1), new AccessorByMethodReference<>(function2));
	}
	
	/**
	 * Creates a chain that:
	 * - returns null when any getters return null
	 * - initializes values (instantiate bean) on path when its mutator is used
	 * (voluntary dissimetric behavior)
	 *
	 * @param getter1 getter of the first property
	 * @param getter2 getter of the second property
	 * @see #RETURN_NULL
	 * @see ValueInitializerOnNullValue#newInstance(Accessor, Class)
	 * @see #chainNullSafe(List, BiFunction)
	 */
	public static <IN, A, OUT> AccessorChain<IN, OUT> chainNullSafe(SerializableFunction<IN, A> getter1, SerializableFunction<A, OUT> getter2) {
		// Note that we use Accessors.accessor because it builds a ReversibleAccessor (required further to eventually set value) whereas AccessorByMethodReference doesn't
		return new AccessorChain<IN, OUT>(Accessors.accessor(getter1), Accessors.accessor(getter2)) {
			
			private final AccessorChainMutator<IN, Object, OUT> mutator = (AccessorChainMutator<IN, Object, OUT>) super.toMutator()
					.setNullValueHandler(INITIALIZE_VALUE);
			
			@Override
			public AccessorChainMutator toMutator() {
				return mutator;
			}
		}.setNullValueHandler(AccessorChain.RETURN_NULL);
	}
	
	/**
	 * Creates a chain that initializes values (instantiate bean) on its path if accessor returns null.
	 *
	 * @param getter getter of the first property
	 * @param setter setter of the second property
	 * @see #INITIALIZE_VALUE
	 * @see #chainNullSafe(List, BiFunction)
	 */
	public static <IN, A, OUT> AccessorChainMutator<IN, A, OUT> chainNullSafe(SerializableFunction<IN, A> getter, BiConsumer<A, OUT> setter) {
		// Note that we use Accessors.accessor because it builds a ReversibleAccessor (required further to eventually set value) whereas AccessorByMethodReference doesn't
		AccessorChainMutator<IN, A, OUT> result = new AccessorChainMutator<>(Arrays.asList(Accessors.accessor(getter)), setter::accept);
		result.setNullValueHandler(AccessorChain.INITIALIZE_VALUE);
		return result;
	}
	
	/**
	 * Creates a chain that:
	 * - returns null when any accessor on the path returns null
	 * - initializes values (instantiate bean) on the path when its mutator is used
	 * (voluntary dissimetric behavior)
	 *
	 * @param accessors list of {@link Accessor} to be used by chain
	 * @see #RETURN_NULL
	 * @see ValueInitializerOnNullValue#newInstance(Accessor, Class)
	 * @see #chainNullSafe(List, BiFunction)
	 */
	public static <IN, OUT> AccessorChain<IN, OUT> chainNullSafe(List<? extends Accessor<?, ?>> accessors) {
		return chainNullSafe(accessors, null);
	}
	
	/**
	 * Creates a chain that:
	 * - returns null when any accessor on path returns null
	 * - initializes values (instantiate bean) on path when its mutator is used
	 * (voluntary dissimetric behavior)
	 * 
	 * @param accessors list of {@link Accessor} to be used by chain
	 * @param valueTypeDeterminer must be given if a bean type is badly determined by default mechanism
	 * 		  (returning Object on generic for instance, or wrong Collection concrete type), null accepted (means default mechanism)
	 * @see #RETURN_NULL
	 * @see ValueInitializerOnNullValue#newInstance(Accessor, Class)
	 */
	public static <IN, OUT> AccessorChain<IN, OUT> chainNullSafe(List<? extends Accessor<?, ?>> accessors, @Nullable BiFunction<Accessor, Class, Object> valueTypeDeterminer) {
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
	
	/** Will instantiate needed value (and set it) if a link in an accessor chain returns null */
	public static final NullValueHandler INITIALIZE_VALUE = new ValueInitializerOnNullValue();
	
	private final List<Accessor<?, ?>> accessors;
	
	private NullValueHandler nullValueHandler = THROW_NULLPOINTEREXCEPTION;
	
	public AccessorChain() {
		this(new ArrayList<>(5));
	}
	
	public AccessorChain(Accessor<?, ?>... accessors) {
		this(Arrays.asList(accessors));
	}
	
	public AccessorChain(List<? extends Accessor<?, ?>> accessors) {
		this.accessors = (List<Accessor<?, ?>>) accessors;
	}
	
	public List<Accessor<?, ?>> getAccessors() {
		return accessors;
	}
	
	public void add(Accessor<?, ?> accessor) {
		accessors.add(accessor);
	}
	
	public void add(Accessor<?, ?>... accessors) {
		add(Arrays.asList(accessors));
	}
	
	public void add(Iterable<? extends Accessor<?, ?>> accessors) {
		if (accessors instanceof Collection) {
			this.accessors.addAll((Collection<? extends Accessor<?, ?>>) accessors);
		} else {
			accessors.forEach(this::add);
		}
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
			AccessorChainMutator<C, Object, T> result = new AccessorChainMutator<>(Iterables.cutTail(getAccessors()), lastMutator);
			result.setNullValueHandler(this.nullValueHandler);
			return result;
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
	 * Class that will initialize value by instantiating its class and set it onto the property.
	 * instantiated types can be controlled through {@link #newInstance(Accessor, Class)}.
	 */
	public static class ValueInitializerOnNullValue implements NullValueHandler {
		
		private final BiFunction<Accessor, Class, Object> valueTypeDeterminer;
		
		public ValueInitializerOnNullValue() {
			this(null);
		}
		
		public ValueInitializerOnNullValue(@Nullable BiFunction<Accessor, Class, Object> valueTypeDeterminer) {
			this.valueTypeDeterminer = Objects.preventNull(valueTypeDeterminer, ValueInitializerOnNullValue::newInstance);
		}
		
		@Override
		public Object consume(Object srcBean, Accessor accessor) {
			if (accessor instanceof ReversibleAccessor) {
				Mutator mutator = ((ReversibleAccessor) accessor).toMutator();
				Class inputType = Accessors.giveInputType(mutator);
				// NB: will throw an exception if type is not instantiable
				Object value = valueTypeDeterminer.apply(accessor, inputType);
				mutator.set(srcBean, value);
				return value;
			} else {
				throw new UnsupportedOperationException(
						"accessor cannot be reverted because it's not " + Reflections.toString(ReversibleAccessor.class) + ": " + accessor);
			}
		}
		
		/**
		 * Expected to return an instance matching <code>valueType</code> class.
		 * @param accessor the current accessor that returned null, given for a fine-grained adjustment of returned type
		 * @param valueType expected compatible type, this of accessor
		 * @return a concrete and instantiable type compatible with accessor input type
		 */
		public static <T> T newInstance(Accessor<?, T> accessor, Class<T> valueType) {
			if (List.class.equals(valueType)) {
				return (T) new ArrayList();
			} else if (SortedSet.class.equals(valueType)) {
				return (T) new TreeSet();
			} else if (Set.class.equals(valueType)) {
				return (T) new HashSet();
			} else if (SortedMap.class.equals(valueType)) {
				return (T) new TreeMap();
			} else if (Map.class.equals(valueType)) {
				return (T) new HashMap();
			} else if (BlockingDeque.class.equals(valueType)) {
				return (T) new LinkedBlockingDeque();
			} else if (TransferQueue.class.equals(valueType)) {
				return (T) new LinkedTransferQueue();
			} else if (BlockingQueue.class.equals(valueType)) {
				return (T) new ArrayBlockingQueue(16);
			} else if (Queue.class.equals(valueType)) {
				return (T) new ArrayDeque();
			} else {
				return Reflections.newInstance(valueType);
			}
		}
	}
}