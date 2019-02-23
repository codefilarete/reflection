package org.gama.reflection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.gama.lang.collection.Arrays;
import org.gama.lang.collection.Iterables;
import org.gama.lang.collection.Maps;
import org.gama.lang.function.Hanger.Holder;
import org.gama.lang.trace.ModifiableInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
class MethodReferenceDispatcherTest {
	
	@Test
	public void testRedirect_SerializableTriFunction() throws SQLException {
		CharSequence testInstance;
		
		// SerializableTriFunction : a getter with 2 args
		testInstance = new MethodReferenceDispatcher()
				.redirect(CharSequence::subSequence, (i, j) -> "Hello " + i + j + " !")
				.fallbackOn("Coucou world !")
				.build(CharSequence.class);
		assertEquals("Hello 42666 !", testInstance.subSequence(42, 666));
		// testing fallback (on chars() method)
		StringBuilder appendedChars = new StringBuilder();
		testInstance.chars().forEach(c -> appendedChars.append((char) c));
		assertEquals("Coucou world !", appendedChars.toString());
		assertEquals("Dispatcher to Coucou world !", testInstance.toString());
	}
	
	@Test
	public void testRedirect_BiFunction() {
		CharSequence testInstance;
		
		// SerializableBiFunction : a getter with 1 arg
		testInstance = new MethodReferenceDispatcher()
				.redirect(CharSequence::charAt, i -> 'H')
				.fallbackOn("Coucou world !")
				.build(CharSequence.class);
		assertEquals('H', testInstance.charAt(1));
		
		// SerializableFunction : a getter
		testInstance = new MethodReferenceDispatcher()
				.redirect(CharSequence::chars, () -> IntStream.range(-1, 3))
				.fallbackOn("Coucou world !")
				.build(CharSequence.class);
		assertEquals(Arrays.asList(-1, 0, 1, 2), Iterables.copy(testInstance.chars().iterator()));
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
				.fallbackOn("Coucou world !")
				.build(PreparedStatement.class);
		
		testInstance.setString(0, "coucou");
		testInstance.setLong(1, 42);
		testInstance.setInt(2, 666);
		assertEquals(Maps.asHashMap(0, (Object) "coucou").add(1, 42L).add(2, 666), valuesCaptor);
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