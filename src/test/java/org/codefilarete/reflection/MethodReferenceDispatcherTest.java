package org.codefilarete.reflection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.tool.collection.Iterables;
import org.codefilarete.tool.collection.Maps;
import org.codefilarete.tool.function.Hanger.Holder;
import org.codefilarete.tool.reflect.MethodDispatcher.WrongTypeReturnedException;
import org.codefilarete.tool.trace.MutableInt;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * @author Guillaume Mary
 */
class MethodReferenceDispatcherTest {
	
	@Test
	void redirect_TriFunction() {
		CharSequence testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		testInstance = new MethodReferenceDispatcher()
				.redirect(CharSequence::subSequence, (i, j) -> "Hello " + i + j + " !")
				.fallbackOn("Hello world !")
				.build(CharSequence.class);
		assertThat(testInstance.subSequence(42, 666)).isEqualTo("Hello 42666 !");
		// testing fallback (on chars() method)
		StringBuilder appendedChars = new StringBuilder();
		testInstance.chars().forEach(c -> appendedChars.append((char) c));
		assertThat(appendedChars.toString()).isEqualTo("Hello world !");
		assertThat(testInstance.toString()).isEqualTo("Dispatcher to Hello world !");
	}
	
	/**
	 * Edge case : due to generics or too generic typing (Object), the code to invoke might not return the expected type (the one of captured method).
	 * Whereas it's a misusage of the API, this case is hard to understand and debug, we added the handling of it in the code to avoid dev spend
	 * too much time to understand what's going on. Hence, we test it (again, even it's an edge case it has some (time) value to be addressed)
	 */
	@Test
	void redirect_TriFunction_FunctionReturnsWrongType_throwsRuntimeException() {
		Map<Integer, String> valuesHolder = new HashMap<>();
		int key = 42;
		// we put a value for the key that is going to be set, to make the call return a value (Map.put(..) returns the old value)
		valuesHolder.put(key, "any string");
		DummySetter testInstance = new MethodReferenceDispatcher()
				// Note that initial problem comes from here : argument-methods are incompatible on their returned type, but for any (compiler ?)
				// reasons, the returned type is not checked. This will lead to a ClassCastException at runtime.
				.redirect(DummySetter::value, valuesHolder::put)
				.build(DummySetter.class);
		
		// because we previously put a value for the key in the Map, the method will return a String, whereas it should have returned a DummySetter
		// as expected by DummySetter.value(int, String). Without the case handling, user would get a java.lang.ClassCastException very difficult
		// to understand
		assertThatCode(() -> testInstance.value(key, "whatever"))
				.isInstanceOf(WrongTypeReturnedException.class)
				.hasMessage("Code handling o.c.r.MethodReferenceDispatcherTest$DummySetter.value(int, j.l.String)" +
						" is expected to return a o.c.r.MethodReferenceDispatcherTest$DummySetter but returned a j.l.String");
	}
	
	@Test
	void redirect_TriFunction_BiConsumer() {
		CharSequence testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		MutableInt modifiableInt = new MutableInt();
		testInstance = new MethodReferenceDispatcher()
				.redirect(CharSequence::subSequence, (i, j) -> {
					modifiableInt.increment(i);
					modifiableInt.increment(j);
				})
				.fallbackOn("Hello world !")
				.build(CharSequence.class);
		CharSequence actual = testInstance.subSequence(42, 666);
		assertThat(modifiableInt.getValue()).isEqualTo(708);
		assertThat(actual).isSameAs(testInstance);
		// testing fallback (on chars() method)
		StringBuilder appendedChars = new StringBuilder();
		testInstance.chars().forEach(c -> appendedChars.append((char) c));
		assertThat(appendedChars.toString()).isEqualTo("Hello world !");
		assertThat(testInstance.toString()).isEqualTo("Dispatcher to Hello world !");
	}
	
	@Test
	void redirect_Function() {
		Stream testInstance;
		
		// SerializableBiFunction : a getter with 1 arg
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableFunction<Stream, Optional>) Stream::findFirst, () -> Optional.of(42))
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		assertThat(testInstance.findFirst()).isEqualTo(Optional.of(42));
		
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertThat(Iterables.copy(stream.iterator())).isEqualTo(Arrays.asList(1, 2));
	}
	
	@Test
	void redirect_Function_Runnable() {
		Stream testInstance;
		
		// SerializableBiFunction : a getter with 1 arg
		MutableInt modifiableInt = new MutableInt();
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableFunction<Stream, Stream>) Stream::distinct, (Runnable) modifiableInt::increment)
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		
		Stream actual = testInstance.distinct();
		assertThat(modifiableInt.getValue()).isEqualTo(1);
		assertThat(actual).isSameAs(testInstance);
		
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertThat(Iterables.copy(stream.iterator())).isEqualTo(Arrays.asList(1, 2));
	}
	
	@Test
	void redirect_BiFunction() {
		Stream testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableBiFunction<Stream, Long, Stream>) Stream::limit, (Function<Long, Stream>) Stream::of)
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		Stream actual = testInstance.limit(42);
		assertThat(actual.toArray()).isEqualTo(Stream.of(42L).toArray());
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertThat(Iterables.copy(stream.iterator())).isEqualTo(Arrays.asList(1, 2));
	}
	
	@Test
	void redirect_BiFunction_Consumer() {
		Stream testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		MutableInt modifiableInt = new MutableInt();
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableBiFunction<Stream, Long, Stream>) Stream::limit, (Consumer<Long>) l -> modifiableInt.increment(l.intValue()))
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		Stream actual = testInstance.limit(42);
		assertThat(modifiableInt.getValue()).isEqualTo(42);
		assertThat(actual).isSameAs(testInstance);
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertThat(Iterables.copy(stream.iterator())).isEqualTo(Arrays.asList(1, 2));
	}
	
	@Test
	void redirect_Consumer() {
		ExtendedRunnable testInstance;
		
		// SerializableConsumer : a runner
		MutableInt modifiableInt = new MutableInt();
		testInstance = new MethodReferenceDispatcher()
				.redirect(ExtendedRunnable::run, modifiableInt::increment)
				.fallbackOn(new ExtendedRunnable() {
					@Override
					public void doRun() {
						modifiableInt.increment(666);
					}
					
					@Override
					public void run() {
						modifiableInt.increment(42);
					}
				})
				.build(ExtendedRunnable.class);
		testInstance.run();
		assertThat(modifiableInt.getValue()).isEqualTo(1);
		testInstance.doRun();
		assertThat(modifiableInt.getValue()).isEqualTo(667);
	}
	
	@Test
	void redirect_BiConsumer() {
		// SerializableBiConsummer : a setter
		DummySetter testInstance;
		
		Holder<Integer> valueHolder = new Holder<>();
		testInstance = new MethodReferenceDispatcher()
				.redirect(DummySetter::setValue, valueHolder::set)
				.build(DummySetter.class);
		
		testInstance.setValue(42);
		assertThat((int) valueHolder.get()).isEqualTo(42);
		
		// SerializableTriSonsummer : a setter
		Map<Integer, String> valuesHolder = new HashMap<>();
		testInstance = new MethodReferenceDispatcher()
				.redirect(DummySetter::setValues, valuesHolder::put)
				.build(DummySetter.class);
		
		testInstance.setValues(42, "666");
		assertThat(valuesHolder).isEqualTo(Maps.asHashMap(42, "666"));
	}
	
	@Test
	void redirect_ThrowingConsumer() throws SQLException {
		Connection testInstance;
		
		// ThrowingBiConsumer
		Holder<Integer> xx = new Holder<>();
		testInstance = new MethodReferenceDispatcher()
				.redirectThrower(Connection::commit, () -> xx.set(42))
				.build(Connection.class);
		testInstance.commit();
		assertThat((int) xx.get()).isEqualTo(42);
	}
	
	@Test
	void redirect_ThrowingBiConsumer() throws SQLException {
		PreparedStatement testInstance;
		
		// ThrowingBiConsumer
		Holder<Integer> xx = new Holder<>();
		testInstance = new MethodReferenceDispatcher()
				.redirectThrower(PreparedStatement::setFetchSize, xx::set)
				.build(PreparedStatement.class);
		testInstance.setFetchSize(42);
		assertThat((int) xx.get()).isEqualTo(42);
	}
	
	@Test
	void redirect_ThrowingTriConsumer() throws SQLException {
		PreparedStatement testInstance;
		
		// ThrowingTriConsumer
		Map<Integer, Object> valuesCaptor = new HashMap<>();
		testInstance = new MethodReferenceDispatcher()
				.redirectThrower(PreparedStatement::setString, valuesCaptor::put)
				.redirectThrower(PreparedStatement::setLong, valuesCaptor::put)
				.redirectThrower(PreparedStatement::setInt, valuesCaptor::put)
				// this fallback has no purpose in a real world of such a PreparedStatement, it's just a safeguard for bad written test
				// because if a non captured method is called, an exception will be rised with this text. Currently, the fallback could be removed.
				.fallbackOn("Hello world !")
				.build(PreparedStatement.class);
		
		testInstance.setString(0, "Hello");
		testInstance.setLong(1, 42);
		testInstance.setInt(2, 666);
		assertThat(valuesCaptor).isEqualTo(Maps.asHashMap(0, (Object) "Hello").add(1, 42L).add(2, 666));
	}
	
	@Test
	void redirect_ThrowingFunction() throws SQLException {
		PreparedStatement testInstance;
		
		// ThrowingFunction
		testInstance = new MethodReferenceDispatcher()
				.redirectThrower(PreparedStatement::executeBatch, () -> new int[] {42, 666 })
				.fallbackOn("Coucou world !")
				.build(PreparedStatement.class);
		assertThat(testInstance.executeBatch()).isEqualTo(new int[]{42, 666});
	}
	
	public interface ExtendedRunnable extends Runnable {
		void doRun();
	}
	
	public interface DummySetter {
		
		void setValue(int i);
		
		void setValues(int i, String s);
		
		DummySetter value(int i, String s);
	}
}