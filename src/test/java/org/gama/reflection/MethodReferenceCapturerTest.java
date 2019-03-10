package org.gama.reflection;

import java.lang.reflect.Constructor;
import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.gama.lang.Reflections;
import org.gama.lang.StringAppender;
import org.gama.lang.collection.Arrays;
import org.gama.lang.function.SerializableTriConsumer;
import org.gama.lang.function.SerializableTriFunction;
import org.gama.reflection.MethodReferenceCapturer.LRUCache;
import org.gama.reflection.jailed.PackagePrivateInheritedClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
	public void testFindMethod_methodDefinedInPackagePrivateClass_throwsException() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		UnsupportedOperationException thrownException = assertThrows(UnsupportedOperationException.class,
				() -> testInstance.findMethod((SerializableBiConsumer<PackagePrivateInheritedClass, Integer>) PackagePrivateInheritedClass::doSomethingWith));
		assertEquals("Found method is synthetic which means original one was wrapped by some bytecode (generally to bypass visibility constraint)", thrownException.getMessage());
		
		thrownException = assertThrows(UnsupportedOperationException.class,
				() -> testInstance.findMethod((SerializableBiConsumer<StringBuilder, Integer>) StringBuilder::ensureCapacity));
		assertEquals("Found method is synthetic which means original one was wrapped by some bytecode (generally to bypass visibility constraint)", thrownException.getMessage());
	}
	
	@Test
	public void testFindMethod_methodDefinedInMethod_throwsException() {
		class Toto {
			public void doSomething() {
				
			}
		}
		
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		UnsupportedOperationException thrownException = assertThrows(UnsupportedOperationException.class,
				() -> testInstance.findMethod((SerializableConsumer<Toto>) Toto::doSomething));
		assertEquals("Capturing by reference a method of a non-static inner class is not supported," 
				+ " make o.g.r.MethodReferenceCapturerTest$Toto static or an outer class of o.g.r.MethodReferenceCapturerTest", thrownException.getMessage());
	}
	
	@Test
	public void testFindConstructor_0arg() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals(Reflections.getConstructor(String.class), testInstance.findConstructor((SerializableSupplier<String>) String::new));
	}
	
	@Test
	public void testFindConstructor_1arg() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals(Reflections.getConstructor(String.class, String.class), testInstance.findConstructor((SerializableFunction<String, String>) String::new));
		assertEquals(Reflections.getConstructor(String.class, char[].class), testInstance.findConstructor((SerializableFunction<char[], String>) String::new));
	}
	
	@Test
	public void testFindConstructor_2args() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals(Reflections.getConstructor(HashMap.class, int.class, float.class),
				testInstance.findConstructor((SerializableBiFunction<Integer, Float, HashMap>) HashMap::new));
	}
	
	@Test
	public void testFindConstructor_constructorIsInnerOne_static() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals(Reflections.getConstructor(StaticInnerClass.class), testInstance.findConstructor(StaticInnerClass::new));
	}
	
	@Test
	public void testFindConstructor_constructorIsInnerOne_static_onlyOneConstructorWithOneArgument() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals(Reflections.getConstructor(StaticInnerClassWithOnlyOneConstructorWithOneArgument.class, int.class),
				testInstance.findConstructor(StaticInnerClassWithOnlyOneConstructorWithOneArgument::new));
	}
	
	@Test
	public void testFindConstructor_constructorIsInnerOne_nonStatic_throwsException() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertEquals("Capturing by reference a non-static inner class constructor is not supported" +
						", make o.g.r.MethodReferenceCapturerTest$NonStaticInnerClass static or an outer class of o.g.r.MethodReferenceCapturerTest",
				assertThrows(UnsupportedOperationException.class, () -> testInstance.findConstructor(NonStaticInnerClass::new)).getMessage());
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
	
	@Test
	public void testToMethodReferenceString() throws NoSuchMethodException {
		assertEquals("String::concat", MethodReferences.toMethodReferenceString(String.class.getMethod("concat", String.class)));
		SerializableFunction<String, char[]> toCharArray = String::toCharArray;
		assertEquals("String::toCharArray", MethodReferences.toMethodReferenceString(toCharArray));
		SerializableBiFunction<String, String, String> concat = String::concat;
		assertEquals("String::concat", MethodReferences.toMethodReferenceString(concat));
		SerializableTriFunction<String, Integer, Integer, CharSequence> subSequence = String::subSequence;
		assertEquals("String::subSequence", MethodReferences.toMethodReferenceString(subSequence));
		SerializableBiConsumer<AtomicInteger, Integer> set = AtomicInteger::set;
		assertEquals("AtomicInteger::set", MethodReferences.toMethodReferenceString(set));
		SerializableConsumer<Map> clear = Map::clear;
		assertEquals("Map::clear", MethodReferences.toMethodReferenceString(clear));
		SerializableTriConsumer<StaticInnerClass, String, Integer> triConsumerMethod = StaticInnerClass::triConsumerMethod;
		assertEquals("StaticInnerClass::triConsumerMethod", MethodReferences.toMethodReferenceString(triConsumerMethod));
	}
	
	private class NonStaticInnerClass {
		
		public NonStaticInnerClass() {
		}
	}
	
	private static class StaticInnerClass {
		
		public StaticInnerClass() {
		}
		
		void triConsumerMethod(String param1, Integer param2) {
			
		}
	}
	
	
	private static class StaticInnerClassWithOnlyOneConstructorWithOneArgument {
		
		private StaticInnerClassWithOnlyOneConstructorWithOneArgument(int i) {
		}
	}
}