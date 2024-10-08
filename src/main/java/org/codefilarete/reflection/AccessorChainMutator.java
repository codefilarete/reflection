package org.codefilarete.reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.StringAppender;
import org.codefilarete.tool.ThreadLocals;
import org.codefilarete.tool.VisibleForTesting;
import org.codefilarete.tool.collection.Iterables;
import org.codefilarete.tool.function.ThrowingRunnable;

/**
 * @param <C> source bean type
 * @param <X> last mutator bean type
 * @param <T> value type to be set
 * @author Guillaume Mary
 */
public class AccessorChainMutator<C, X, T> extends AccessorChain<C, X> implements ReversibleMutator<C, T> {
	
	/**
	 * Keeps track of the mutator that returned null during the {@link #get(Object)} phase, if any.
	 * Will be used to give better exception message when {@link NullPointerException} will be thrown by {@link #set(Object, Object)}.
	 * Using a ThreadLocal is quite an overkill design, but can't find a less intrusive & thread-safe way to do it. 
	 */
	@SuppressWarnings("java:S5164" /* remove() will be invoked by AutoRemoveThreadLocal while using ThreadLocals.doWithThreadLocal(..) */)
	private static final ThreadLocal<Accessor> CURRENT_NULL_RETURNING_MUTATOR = new ThreadLocal<>();
	
	private final Mutator<X, T> mutator;
	
	public AccessorChainMutator(List<? extends Accessor<?, ?>> accessors, Mutator<X, T> mutator) {
		super(accessors);
		this.mutator = mutator;
	}
	
	public AccessorChainMutator(AccessorChain<C, X> accessors, Mutator<X, T> mutator) {
		super(accessors.getAccessors());
		this.mutator = mutator;
	}
	
	public Mutator<X, T> getMutator() {
		return mutator;
	}
	
	@Override
	public void set(C c, T t) {
		final Object[] finalTarget = new Object[1];
		ThreadLocals.doWithThreadLocal(CURRENT_NULL_RETURNING_MUTATOR, () -> null, (ThrowingRunnable<NullPointerException>) () -> {
			X target = get(c);	// Warn : this line changes state of CURRENT_NULL_RETURNING_MUTATOR by invoking onNullValue()
			if (target == null) {
				throwNullPointerException(c);
			}
			finalTarget[0] = target;
		});
		mutator.set((X) finalTarget[0], t);
	}
	
	/**
	 * Overridden to keep track of the culprit mutator that returned null
	 * 
	 * @param targetBean bean on which accessor was invoked
	 * @param accessor accessor that returned null when invoked on targetBean
	 * @return super.onNullValue(targetBean, accessor)
	 */
	@Override
	protected Object onNullValue(Object targetBean, Accessor accessor) {
		CURRENT_NULL_RETURNING_MUTATOR.set(accessor);
		return super.onNullValue(targetBean, accessor);
	}
	
	private void throwNullPointerException(Object srcBean) {
		String accessorDescription = new AccessorPathBuilder().ccat(getAccessors(), ".").toString();
		Accessor nullReturningMutator = CURRENT_NULL_RETURNING_MUTATOR.get();
		List<Accessor<?, ?>> pathToNullPointerException = Iterables.head(getAccessors(), nullReturningMutator);
		pathToNullPointerException.add(nullReturningMutator);
		String nullProviderDescription = new AccessorPathBuilder().ccat(pathToNullPointerException, ".").toString();
		throw new NullPointerException("Call of " + accessorDescription + " on " + srcBean + " returned null, because "
				+ nullProviderDescription + " returned null");
	}
	
	/**
	 * Only supported when last mutator is reversible (aka implements {@link ReversibleMutator}.
	 * 
	 * @return a new chain which path is the same as this
	 * @throws UnsupportedOperationException if last mutator is not reversible
	 */
	@Override
	public AccessorChain<C, T> toAccessor() {
		if (mutator instanceof ReversibleMutator) {
			List<Accessor<?, ?>> newAccessors = new ArrayList<>(getAccessors());
			newAccessors.add(((ReversibleMutator) mutator).toAccessor());
			return new AccessorChain<>(newAccessors);
		} else {
			throw new UnsupportedOperationException(
					"Last mutator cannot be reverted because it doesn't implement " + Reflections.toString(ReversibleAccessor.class) + ": " + mutator);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && this.mutator.equals(((AccessorChainMutator) other).mutator);
	}
	
	@Override
	public int hashCode() {
		return 31 * super.hashCode() + this.mutator.hashCode();
	}
	
	/**
	 * Overridden to take mutator into account
	 * @return getters and final setter aggregated
	 */
	@Override
	public String getDescription() {
		// NB: arrow mark is totally arbitrary and is only here to distinguish mutator from accessor part
		return super.getDescription() + " <- " + this.mutator.toString();
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
	
	/**
	 * Aimed at giving a simple and readable description of a collection of accessor
	 */
	@VisibleForTesting
	static class AccessorPathBuilder extends StringAppender {
		@Override
		public StringAppender cat(Object o) {
			if (o instanceof AccessorByMember) {
				super.cat(((AccessorByMember) o).getGetter().getName());
				if (((AccessorByMember) o).getGetter() instanceof Method) {
					super.cat("(");
					if (o instanceof ListAccessor) {
						super.cat(((ListAccessor) o).getIndex());
					} else {
						if (((Method) ((AccessorByMember) o).getGetter()).getParameterCount() > 0) {
							// we don't need a perfect description for our case (exception message) so we shortcut method parameters
							super.cat("..");
						}
					}
					super.cat(")");
				}
				return this;
			} else if (o instanceof AccessorByMethodReference) {
				super.cat(((AccessorByMethodReference) o).getMethodName() + "()");
				return this;
			} else if (o instanceof ArrayAccessor) {
				return super.cat("[" + ((ArrayAccessor) o).getIndex() + "]");
			} else {
				return super.cat(o);
			}
		}
	}
}
