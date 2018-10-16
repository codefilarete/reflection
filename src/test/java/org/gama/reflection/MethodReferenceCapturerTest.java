package org.gama.reflection;

import java.lang.reflect.Constructor;
import java.text.Collator;
import java.util.List;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.Reflections;
import org.gama.lang.StringAppender;
import org.gama.lang.collection.Arrays;
import org.gama.reflection.MethodReferenceCapturer.LRUCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
public class MethodReferenceCapturerTest {
	
	@Test
	public void testFindMethod() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals(Reflections.getMethod(Object.class, "toString"), testInstance.findMethod(Object::toString));
		assertEquals(Reflections.getMethod(Integer.class, "shortValue"), testInstance.findMethod(Integer::shortValue));
		assertEquals(Reflections.getMethod(Collator.class, "setStrength", int.class), testInstance.findMethod(Collator::setStrength));
		assertEquals(Reflections.getMethod(String.class, "toCharArray"), testInstance.findMethod(String::toCharArray));
		assertEquals(Reflections.getMethod(List.class, "toArray", Object[].class), testInstance.findMethod((SerializableBiConsumer<List, Object[]>) List::toArray));
		assertEquals(Reflections.getMethod(String.class, "codePointCount", int.class, int.class), testInstance.findMethod(
				(SerializableTriConsumer<String, Integer, Integer>) String::codePointCount));
		assertEquals(Reflections.getMethod(StringAppender.class, "ccat", Object[].class, Object.class), testInstance.findMethod(
				(SerializableTriConsumer<StringAppender, Object[], Object>) StringAppender::ccat));
	}
	
	@Test
	public void testFindConstructor() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals(Reflections.getConstructor(String.class, String.class), testInstance.findConstructor((SerializableFunction<String, String>) String::new));
		assertEquals(Reflections.getConstructor(String.class, char[].class), testInstance.findConstructor((SerializableFunction<char[], String>) String::new));
	}
	
	@Test
	public void testGiveArgumentTypes() throws ClassNotFoundException {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertArrayEquals(new Class[] { int.class }, testInstance.giveArgumentTypes("(I)V"));
		assertArrayEquals(new Class[] { boolean.class }, testInstance.giveArgumentTypes("(Z)V"));
		assertArrayEquals(new Class[] { int[].class }, testInstance.giveArgumentTypes("([I)V"));
		assertArrayEquals(new Class[] { int[].class, boolean[].class }, testInstance.giveArgumentTypes("([I[Z)V"));
		assertArrayEquals(new Class[] { int.class, int.class }, testInstance.giveArgumentTypes("(II)V"));
		assertArrayEquals(new Class[] { Object[].class }, testInstance.giveArgumentTypes("([Ljava.lang.Object;)V"));
		assertArrayEquals(new Class[] { int.class, Object[].class }, testInstance.giveArgumentTypes("(I[Ljava.lang.Object;)V"));
		assertArrayEquals(new Class[] { Object[].class, int.class }, testInstance.giveArgumentTypes("([Ljava.lang.Object;I)V"));
		assertArrayEquals(new Class[] { Object[].class, int[].class }, testInstance.giveArgumentTypes("([Ljava.lang.Object;[I)V"));
		assertArrayEquals(new Class[] { Object.class }, testInstance.giveArgumentTypes("(Ljava.lang.Object;)V"));
		assertArrayEquals(new Class[] { Object[].class, Object.class }, testInstance.giveArgumentTypes("([Ljava.lang.Object;Ljava.lang.Object;)V"));
		assertArrayEquals(new Class[] { Object.class, Object.class }, testInstance.giveArgumentTypes("(Ljava.lang.Object;Ljava.lang.Object;)V"));
	}
	
	@Test
	public void testLRUCache() {
		LRUCache testInstance = new LRUCache(3);
		Constructor<String> dummyExecutable = Reflections.getDefaultConstructor(String.class);
		testInstance.put("b", dummyExecutable);
		testInstance.put("a", dummyExecutable);
		testInstance.put("c", dummyExecutable);
		assertEquals(Arrays.asSet("b", "a", "c"), testInstance.keySet());
		// adding an overflowing entry makes the very first one to be removed (LRU principle)
		testInstance.put("d", dummyExecutable);
		assertEquals(Arrays.asSet("a", "c", "d"), testInstance.keySet());
		// adding an already existing one as no influence on the map
		testInstance.put("d", dummyExecutable);
		assertEquals(Arrays.asSet("a", "c", "d"), testInstance.keySet());
	}
}