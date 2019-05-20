package org.gama.reflection;

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

import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.collection.Arrays;
import org.gama.lang.collection.Iterables;
import org.gama.lang.collection.Maps;
import org.gama.lang.function.Hanger.Holder;
import org.gama.lang.trace.ModifiableInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Guillaume Mary
 */
class MethodReferenceDispatcherTest {
	
	@Test
	public void testRedirect_TriFunction() {
		CharSequence testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		testInstance = new MethodReferenceDispatcher()
				.redirect(CharSequence::subSequence, (i, j) -> "Hello " + i + j + " !")
				.fallbackOn("Hello world !")
				.build(CharSequence.class);
		assertEquals("Hello 42666 !", testInstance.subSequence(42, 666));
		// testing fallback (on chars() method)
		StringBuilder appendedChars = new StringBuilder();
		testInstance.chars().forEach(c -> appendedChars.append((char) c));
		assertEquals("Hello world !", appendedChars.toString());
		assertEquals("Dispatcher to Hello world !", testInstance.toString());
	}
	
	@Test
	public void testRedirect_TriFunction_BiConsumer() {
		CharSequence testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		ModifiableInt modifiableInt = new ModifiableInt();
		testInstance = new MethodReferenceDispatcher()
				.redirect(CharSequence::subSequence, (i, j) -> {
					modifiableInt.increment(i);
					modifiableInt.increment(j);
				})
				.fallbackOn("Hello world !")
				.build(CharSequence.class);
		CharSequence actual = testInstance.subSequence(42, 666);
		assertEquals(708, modifiableInt.getValue());
		assertSame(testInstance, actual);
		// testing fallback (on chars() method)
		StringBuilder appendedChars = new StringBuilder();
		testInstance.chars().forEach(c -> appendedChars.append((char) c));
		assertEquals("Hello world !", appendedChars.toString());
		assertEquals("Dispatcher to Hello world !", testInstance.toString());
	}
	
	@Test
	public void testRedirect_Function() {
		Stream testInstance;
		
		// SerializableBiFunction : a getter with 1 arg
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableFunction<Stream, Optional>) Stream::findFirst, () -> Optional.of(42))
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		assertEquals(Optional.of(42), testInstance.findFirst());
		
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertEquals(Arrays.asList(1, 2), Iterables.copy(stream.iterator()));
	}
	
	@Test
	public void testRedirect_Function_Runnable() {
		Stream testInstance;
		
		// SerializableBiFunction : a getter with 1 arg
		ModifiableInt modifiableInt = new ModifiableInt();
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableFunction<Stream, Stream>) Stream::distinct, (Runnable) modifiableInt::increment)
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		
		Stream actual = testInstance.distinct();
		assertEquals(1, modifiableInt.getValue());
		assertSame(testInstance, actual);
		
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertEquals(Arrays.asList(1, 2), Iterables.copy(stream.iterator()));
	}
	
	@Test
	public void testRedirect_BiFunction() {
		Stream testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableBiFunction<Stream, Long, Stream>) Stream::limit, (Function<Long, Stream>) Stream::of)
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		Stream actual = testInstance.limit(42);
		assertArrayEquals(Stream.of(42L).toArray(), actual.toArray());
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertEquals(Arrays.asList(1, 2), Iterables.copy(stream.iterator()));
	}
	
	@Test
	public void testRedirect_BiFunction_Consumer() {
		Stream testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		ModifiableInt modifiableInt = new ModifiableInt();
		testInstance = new MethodReferenceDispatcher()
				.redirect((SerializableBiFunction<Stream, Long, Stream>) Stream::limit, (Consumer<Long>) l -> modifiableInt.increment(l.intValue()))
				.fallbackOn(Stream.of(1, null, 2))
				.build(Stream.class);
		Stream actual = testInstance.limit(42);
		assertEquals(42, modifiableInt.getValue());
		assertSame(testInstance, actual);
		// other methods are not intercepted
		Stream stream = testInstance.filter(Objects::nonNull);
		assertEquals(Arrays.asList(1, 2), Iterables.copy(stream.iterator()));
	}
	
	@Test
	public void testRedirect_Consumer() {
		ExtendedRunnable testInstance;
		
		// SerializableConsumer : a runner
		ModifiableInt modifiableInt = new ModifiableInt();
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
		assertEquals(1, modifiableInt.getValue());
		testInstance.doRun();
		assertEquals(667, modifiableInt.getValue());
	}
	
	@Test
	public void testRedirect_BiConsumer() {
		// SerializableBiConsummer : a setter
		DummySetter testInstance;
		
		Holder<Integer> valueHolder = new Holder<>();
		testInstance = new MethodReferenceDispatcher()
				.redirect(DummySetter::setValue, valueHolder::set)
				.build(DummySetter.class);
		
		testInstance.setValue(42);
		assertEquals(42, (int) valueHolder.get());
		
		// SerializableTriSonsummer : a setter
		Map<Integer, String> valuesHolder = new HashMap<>();
		testInstance = new MethodReferenceDispatcher()
				.redirect(DummySetter::setValues, valuesHolder::put)
				.build(DummySetter.class);
		
		testInstance.setValues(42, "666");
		assertEquals(Maps.asHashMap(42, "666"), valuesHolder);
	}
	
	@Test
	public void testRedirect_ThrowingConsumer() throws SQLException {
		Connection testInstance;
		
		// ThrowingBiConsumer
		Holder<Integer> xx = new Holder<>();
		testInstance = new MethodReferenceDispatcher()
				.redirectThrower(Connection::commit, () -> xx.set(42))
				.build(Connection.class);
		testInstance.commit();
		assertEquals(42, (int) xx.get());
	}
	
	@Test
	public void testRedirect_ThrowingBiConsumer() throws SQLException {
		PreparedStatement testInstance;
		
		// ThrowingBiConsumer
		Holder<Integer> xx = new Holder<>();
		testInstance = new MethodReferenceDispatcher()
				.redirectThrower(PreparedStatement::setFetchSize, xx::set)
				.build(PreparedStatement.class);
		testInstance.setFetchSize(42);
		assertEquals(42, (int) xx.get());
	}
	
	@Test
	public void testRedirect_ThrowingTriConsumer() throws SQLException {
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
		assertEquals(Maps.asHashMap(0, (Object) "Hello").add(1, 42L).add(2, 666), valuesCaptor);
	}
	
	@Test
	public void testRedirect_ThrowingFunction() throws SQLException {
		PreparedStatement testInstance;
		
		// ThrowingFunction
		testInstance = new MethodReferenceDispatcher()
				.redirectThrower(PreparedStatement::executeBatch, () -> new int[] {42, 666 })
				.fallbackOn("Coucou world !")
				.build(PreparedStatement.class);
		assertArrayEquals(new int[] { 42, 666 }, testInstance.executeBatch());
	}
	
	public interface ExtendedRunnable extends Runnable {
		void doRun();
	}
	
	public interface DummySetter {
		
		void setValue(int i);
		
		void setValues(int i, String s);
	}
}