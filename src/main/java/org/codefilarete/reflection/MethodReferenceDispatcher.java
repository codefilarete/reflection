package org.codefilarete.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.codefilarete.tool.function.SerializableThrowingBiConsumer;
import org.codefilarete.tool.function.SerializableThrowingConsumer;
import org.codefilarete.tool.function.SerializableThrowingFunction;
import org.codefilarete.tool.function.SerializableThrowingTriConsumer;
import org.codefilarete.tool.function.SerializableTriConsumer;
import org.codefilarete.tool.function.SerializableTriFunction;
import org.codefilarete.tool.function.ThrowingBiConsumer;
import org.codefilarete.tool.function.ThrowingConsumer;
import org.codefilarete.tool.function.TriFunction;
import org.codefilarete.tool.reflect.MethodDispatcher;

import static org.codefilarete.tool.Reflections.PRIMITIVE_DEFAULT_VALUES;
import static org.codefilarete.tool.Reflections.newProxy;
import static org.codefilarete.reflection.MethodReferences.buildSerializedLambda;

/**
 * A specialized version of {@link MethodDispatcher} for single method to be redirected.
 * Can't be added directly to {@link MethodDispatcher} because its requires {@link MethodReferenceCapturer} which is not available from
 * {@link MethodDispatcher} module
 * 
 * @author Guillaume Mary
 */
public class MethodReferenceDispatcher extends MethodDispatcher {
	
	private static final MethodReferenceCapturer METHOD_REFERENCE_CAPTURER = new MethodReferenceCapturer();
	
	/**
	 * Redirects a {@link Function} invocation (on the proxy built by {@link MethodReferenceDispatcher#build(Class)}) onto the given {@link Callable}
	 * 
	 * @param methodToCapture the no-args {@link Function} to be intercepted
	 * @param codeToInvoke the code to be called instead of the {@link Function} 
	 * @param <X> declaring class of the intercepted method
	 * @param <R> result type of the given function
	 * @return this
	 */
	public <X, R> MethodReferenceDispatcher redirect(SerializableFunction<X, R> methodToCapture, Supplier<R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (ArgsDigester) args -> codeToInvoke.get());
		return this;
	}
	
	/**
	 * Made for particular use case of Fluent API writing : result of Function invocation will be the proxy itself (X) so one can chain it with some
	 * other proxy methods, {@link BiConsumer} is used to apply some code on this {@link TriFunction} call.
	 * Quite the same as {@link #redirect(SerializableTriFunction, BiFunction)} but avoids to implement a null-returning {@link TriFunction}
	 *
	 * @param methodToCapture a Method Reference (3-args getter) representing the method to be intercepted
	 * @param codeToInvoke consumer invoked on {@code methodToCapture} call
	 * @param <X> declaring class of the given function
	 * @param <R> result type of the given function
	 * @return this
	 */
	public <X, R> MethodReferenceDispatcher redirect(SerializableFunction<X, R> methodToCapture, Runnable codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (ArgsConsumer) args -> codeToInvoke.run());
		return this;
	}
	
	/**
	 * Redirects a {@link BiFunction} invocation (on the proxy built by {@link MethodReferenceDispatcher#build(Class)}) onto the given {@link Function}
	 *
	 * @param methodToCapture the 2-args {@link BiFunction} to be intercepted
	 * @param codeToInvoke the code to be called instead of the {@link BiFunction} 
	 * @param <X> declaring class of the intercepted method
	 * @param <R> result type of the given function
	 * @return this
	 */
	public <X, A, R> MethodReferenceDispatcher redirect(SerializableBiFunction<X, A, R> methodToCapture, Function<A, R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (ArgsDigester) args -> codeToInvoke.apply((A) args[0]));
		return this;
	}
	
	/**
	 * Made for particular use case of Fluent API writing : result of Function invocation will be the proxy itself (X) so one can chain it with some
	 * other proxy methods, {@link BiConsumer} is used to apply some code on this {@link TriFunction} call.
	 * Quite the same as {@link #redirect(SerializableTriFunction, BiFunction)} but avoids to implement a null-returning {@link TriFunction}
	 *
	 * @param methodToCapture a Method Reference (3-args getter) representing the method to be intercepted
	 * @param codeToInvoke consumer invoked on {@code methodToCapture} call
	 * @param <X> declaring class of the given function
	 * @param <A> input type of the given function
	 * @param <R> result type of the given function
	 * @return this
	 */
	public <X, A, R> MethodReferenceDispatcher redirect(SerializableBiFunction<X, A, R> methodToCapture, Consumer<A> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (ArgsConsumer) args -> ((Consumer) codeToInvoke).accept(args[0]));
		return this;
	}
	
	/**
	 * Redirects a {@link TriFunction} invocation (on the proxy built by {@link MethodReferenceDispatcher#build(Class)}) onto the given {@link Function}
	 *
	 * @param methodToCapture the 3-args {@link TriFunction} to be intercepted
	 * @param codeToInvoke the code to be called instead of the {@link TriFunction}
	 * @param <X> declaring class of the intercepted method
	 * @param <R> result type of the given function
	 * @return this
	 */
	public <X, A, B, R> MethodReferenceDispatcher redirect(SerializableTriFunction<X, A, B, R> methodToCapture, BiFunction<A, B, R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (ArgsDigester) args -> codeToInvoke.apply((A) args[0], (B) args[1]));
		return this;
	}
	
	/**
	 * Made for particular use case of Fluent API writing : result of Function invocation will be the proxy itself (X) so one can chain it with some
	 * other proxy methods, {@link BiConsumer} is used to apply some code on this {@link TriFunction} call.
	 * Quite the same as {@link #redirect(SerializableTriFunction, BiFunction)} but avoids to implement a null-returning {@link TriFunction}
	 * 
	 * @param methodToCapture a Method Reference (3-args getter) representing the method to be intercepted
	 * @param codeToInvoke consumer invoked on {@code methodToCapture} call
	 * @param <X> declaring class of the given function
	 * @param <A> first input type of the given function
	 * @param <B> second input type of the given function
	 * @param <R> result type of the given function
	 * @return this
	 */
	public <X, A, B, R> MethodReferenceDispatcher redirect(SerializableTriFunction<X, A, B, R> methodToCapture, BiConsumer<A, B> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (ArgsConsumer) args -> ((BiConsumer) codeToInvoke).accept(args[0], args[1]));
		return this;
	}
	
	public <X> MethodReferenceDispatcher redirect(SerializableConsumer<X> methodToCapture, Runnable codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture),
				(proxy, m, args) -> { codeToInvoke.run(); return null; }, true);
		return this;
	}
	
	public <X, A> MethodReferenceDispatcher redirect(SerializableBiConsumer<X, A> methodToCapture, Consumer<A> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture),
				(proxy, m, args) -> { codeToInvoke.accept((A) args[0]); return null; }, true);
		return this;
	}
	
	public <X, A, B> MethodReferenceDispatcher redirect(SerializableTriConsumer<X, A, B> methodToCapture, BiConsumer<A, B> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (proxy, method1, args) -> { codeToInvoke.accept((A) args[0], (B) args[1]); return null; }, true);
		return this;
	}
	
	/**
	 * Same as {@link #redirect(SerializableFunction, Supplier)}, but dedicated to intercepted methods that have a {@code throws} clause.
	 * Naming it "redirect" would lead to some casting of the argument because compiler doesn't distinct method references that has a throws clause
	 * from those that don't have one, so, to prevent boilerplate casting, method must be named differently : {@code redirectThrower}.
	 *
	 * @param methodToCapture the no-args method to be intercepted
	 * @param codeToInvoke the code to be called instead of the method
	 * @param <X> declaring class of the intercepted method
	 * @param <R> result type of the method
	 * @param <E> exception type that may be thrown by captured method
	 * @return this
	 */
	public <X, R, E extends Throwable> MethodReferenceDispatcher redirectThrower(SerializableThrowingFunction<X, R, E> methodToCapture, Supplier<R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(MethodReferences.buildSerializedLambda(methodToCapture)), (ArgsDigester) args -> codeToInvoke.get());
		return this;
	}
	
	public <X, E extends Throwable> MethodReferenceDispatcher redirectThrower(SerializableThrowingConsumer<X, E> methodToCapture, Runnable codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(MethodReferences.buildSerializedLambda(methodToCapture)), (ArgsConsumer) args -> codeToInvoke.run());
		return this;
	}
	
	/**
	 * Same as {@link #redirect(SerializableBiConsumer, Consumer)}, but dedicated to intercepted methods that have a {@code throws} clause.
	 * Naming it "redirect" would lead to some casting of the argument because compiler doesn't distinct method references that has a throws clause
	 * from those that don't have one, so, to prevent boilerplate casting, method must be named differently : {@code redirectThrower}.
	 *
	 * @param methodToCapture the no-args method to be intercepted
	 * @param codeToInvoke the code to be called instead of the method
	 * @param <X> declaring class of the intercepted method
	 * @param <A> input type of the method
	 * @param <E> exception type that may be thrown by captured method
	 * @return this
	 */
	public <X, A, E extends Throwable> MethodReferenceDispatcher redirectThrower(SerializableThrowingBiConsumer<X, A, E> methodToCapture, ThrowingConsumer<A, E> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(MethodReferences.buildSerializedLambda(methodToCapture)), (ArgsConsumer) args -> codeToInvoke.accept((A) args[0]));
		return this;
	}
	
	/**
	 * Same as {@link #redirect(SerializableTriConsumer, BiConsumer)}, but dedicated to intercepted methods that have a {@code throws} clause.
	 * Naming it "redirect" would lead to some casting of the argument because compiler doesn't distinct method references that has a throws clause
	 * from those that don't have one, so, to prevent boilerplate casting, method must be named differently : {@code redirectThrower}.
	 *
	 * @param methodToCapture the no-args method to be intercepted
	 * @param codeToInvoke the code to be called instead of the method
	 * @param <X> declaring class of the intercepted method
	 * @param <A> first input type of the method
	 * @param <B> second input type of the method
	 * @param <E> exception type that may be thrown by captured method
	 * @return this
	 */
	public <X, A, B, E extends Throwable> MethodReferenceDispatcher redirectThrower(SerializableThrowingTriConsumer<X, A, B, E> methodToCapture, ThrowingBiConsumer<A, B, E> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(MethodReferences.buildSerializedLambda(methodToCapture)), (ArgsConsumer) args -> codeToInvoke.accept((A) args[0], (B) args[1]));
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 * Overridden to cast return type to current instance one, to allow better chaining
	 */
	public <X> MethodReferenceDispatcher redirect(Class<X> interfazz, X extensionSurrogate) {
		return (MethodReferenceDispatcher) super.redirect(interfazz, extensionSurrogate);
	}
	
	/**
	 * {@inheritDoc}
	 * Overridden to cast return type to current instance one, to allow better chaining
	 */
	public <X> MethodReferenceDispatcher redirect(Class<X> interfazz, X extensionSurrogate, boolean returnProxy) {
		return (MethodReferenceDispatcher) super.redirect(interfazz, extensionSurrogate, returnProxy);
	}
	
	/**
	 * {@inheritDoc}
	 * Overridden to cast return type to current instance one, to allow better chaining
	 */
	public <X> MethodReferenceDispatcher redirect(Class<X> interfazz, X extensionSurrogate, Object returningMethodsTarget) {
		return (MethodReferenceDispatcher) super.redirect(interfazz, extensionSurrogate, returningMethodsTarget);
	}
	
	/* Shortcut methods  */
	
	private void addInterceptor(Method method, ArgsDigester argsDigester) {
		addInterceptor(method, (p, m, args) -> argsDigester.digest(args), false);
	}
	
	private void addInterceptor(Method method, ArgsConsumer argsConsumer) {
		addInterceptor(method, (p, m, args) -> {
			argsConsumer.consume(args);
			return PRIMITIVE_DEFAULT_VALUES.getOrDefault(method.getReturnType(), null);
		}, true);
	}
	
	private void addInterceptor(Method method, InvocationHandler invocationHandler, boolean returnProxy) {
		interceptors.put(giveSignature(method), new Interceptor(method, newProxy(method.getDeclaringClass(), invocationHandler), returnProxy));
	}
	
	@FunctionalInterface
	private interface ArgsConsumer {
		
		@SuppressWarnings("squid:S00112")	// voluntary non dedicated exception class
		void consume(Object... args) throws Throwable;
	}
	
	@FunctionalInterface
	private interface ArgsDigester<R> {
		
		@SuppressWarnings("squid:S00112")	// voluntary non dedicated exception class
		R digest(Object... args) throws Throwable;
	}
}
