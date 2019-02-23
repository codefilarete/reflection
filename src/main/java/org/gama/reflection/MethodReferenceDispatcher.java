package org.gama.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.function.SerializableThrowingBiConsumer;
import org.gama.lang.function.SerializableThrowingFunction;
import org.gama.lang.function.SerializableThrowingTriConsumer;
import org.gama.lang.function.SerializableTriConsumer;
import org.gama.lang.function.SerializableTriFunction;
import org.gama.lang.function.ThrowingBiConsumer;
import org.gama.lang.function.ThrowingConsumer;
import org.gama.lang.reflect.MethodDispatcher;

/**
 * A specialized version of {@link MethodDispatcher} for single method to be redirected.
 * Can't be added directly to {@link MethodDispatcher} because its requires {@link MethodReferenceCapturer} which is not available from
 * {@link MethodDispatcher} module
 * 
 * @author Guillaume Mary
 */
public class MethodReferenceDispatcher extends MethodDispatcher {
	
	private static final MethodReferenceCapturer METHOD_REFERENCE_CAPTURER = new MethodReferenceCapturer();
	
	private void addInterceptor(Method method, InvocationHandler invocationHandler, boolean returnProxy) {
		interceptors.put(giveSignature(method), new Interceptor(method, Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { method.getDeclaringClass() },
				invocationHandler), returnProxy));
	}
	
	/**
	 * Redirects a method invokation (on the proxy built by {@link MethodReferenceDispatcher#build(Class)}) onto the given {@link Callable}
	 * 
	 * @param methodToCapture the no-args method to be intercepted
	 * @param codeToInvoke the code to be called instead of the the method 
	 * @param <X> declaring class of the intercepted method
	 * @param <R> result type of the method
	 * @return this
	 */
	public <X, R> MethodReferenceDispatcher redirect(SerializableFunction<X, R> methodToCapture, Callable<R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (proxy, m, args) -> codeToInvoke.call(), false);
		return this;
	}
	
	public <X, A, R> MethodReferenceDispatcher redirect(SerializableBiFunction<X, A, R> methodToCapture, Function<A, R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (proxy, m, args) -> codeToInvoke.apply((A) args[0]), false);
		return this;
	}

	public <X, A, B, R> MethodReferenceDispatcher redirect(SerializableTriFunction<X, A, B, R> methodToCapture, SerializableBiFunction<A, B, R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (proxy, m, args) -> codeToInvoke.apply((A) args[0], (B) args[1]), false);
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
	
	public <X, A, B> MethodReferenceDispatcher redirect(SerializableTriConsumer<X, A, B> methodToCapture, SerializableBiConsumer<A, B> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(methodToCapture), (proxy, method1, args) -> { codeToInvoke.accept((A) args[0], (B) args[1]); return null; }, true);
		return this;
	}
	
	/**
	 * Same as {@link #redirect(SerializableFunction, Callable)},but dedicated to intercepted methods that has a {@code throws} clause : currently naming it "redirect"
	 * leads to some casting of the argument because compiler doesn't dictinct method references that has a throws clause from those that don't have one, so, to prevent
	 * boilerplate casting, method must be named differenlty : {@code redirectThrower}.
	 *
	 * @param methodToCapture the no-args method to be intercepted
	 * @param codeToInvoke the code to be called instead of the the method 
	 * @param <X> declaring class of the intercepted method
	 * @param <R> result type of the method
	 * @param <E> exception type tha may be thrown by captured method
	 * @return this
	 */
	public <X, R, E extends Throwable> MethodReferenceDispatcher redirectThrower(SerializableThrowingFunction<X, R, E> methodToCapture, Callable<R> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(MethodReferences.buildSerializedLambda(methodToCapture)),
				(proxy, m, args) -> codeToInvoke.call(), false);
		return this;
	}
	
	public <X, A, B, E extends Throwable> MethodReferenceDispatcher redirectThrower(SerializableThrowingBiConsumer<X, A, E> methodToCapture, ThrowingConsumer<A, E> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(MethodReferences.buildSerializedLambda(methodToCapture)),
				(proxy, m, args) -> { codeToInvoke.accept((A) args[0]); return null; }, true);
		return this;
	}
	
	public <X, A, B, E extends Throwable> MethodReferenceDispatcher redirectThrower(SerializableThrowingTriConsumer<X, A, B, E> methodToCapture, ThrowingBiConsumer<A, B, E> codeToInvoke) {
		addInterceptor(METHOD_REFERENCE_CAPTURER.findMethod(MethodReferences.buildSerializedLambda(methodToCapture)),
				(proxy, m, args) -> { codeToInvoke.accept((A) args[0], (B) args[1]); return null; }, true);
		return this;
	}
}
