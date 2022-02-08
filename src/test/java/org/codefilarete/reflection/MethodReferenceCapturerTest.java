package org.codefilarete.reflection;

import java.lang.reflect.Constructor;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.presentation.Representation;
import org.codefilarete.reflection.MethodReferenceCapturer.LRUCache;
import org.codefilarete.reflection.MethodReferenceCapturer.MethodDefinition;
import org.codefilarete.reflection.jailed.PackagePrivateInheritedClass;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBiFunction;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.StringAppender;
import org.codefilarete.tool.Strings;
import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.tool.function.Predicates;
import org.codefilarete.tool.function.SerializableTriConsumer;
import org.codefilarete.tool.function.SerializableTriFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.codefilarete.tool.function.Functions.chain;

/**
 * @author Guillaume Mary
 */
class MethodReferenceCapturerTest {
	
	@Test
	void findMethod() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findMethod(Object::toString)).isEqualTo(Reflections.getMethod(Object.class, "toString"));
		assertThat(testInstance.findMethod(Integer::shortValue)).isEqualTo(Reflections.getMethod(Integer.class, "shortValue"));
		assertThat(testInstance.findExecutable(Runnable::run)).isEqualTo(Reflections.getMethod(Runnable.class, "run"));
		assertThat(testInstance.findMethod(Collator::setStrength)).isEqualTo(Reflections.getMethod(Collator.class, "setStrength", int.class));
		assertThat(testInstance.findMethod(String::toCharArray)).isEqualTo(Reflections.getMethod(String.class, "toCharArray"));
		assertThat(testInstance.findMethod((SerializableBiFunction<List, Object[], Object[]>) List::toArray)).isEqualTo(Reflections.getMethod(List.class, "toArray", Object[].class));
		assertThat(testInstance.findMethod((SerializableBiConsumer<List, Object[]>) List::toArray)).isEqualTo(Reflections.getMethod(List.class, 
				"toArray", Object[].class));
		assertThat(testInstance.findMethod(
				(SerializableTriConsumer<String, Integer, Integer>) String::codePointCount)).isEqualTo(Reflections.getMethod(String.class, 
				"codePointCount", int.class, int.class));
		assertThat(testInstance.findMethod(
				(SerializableTriConsumer<StringAppender, Object[], Object>) StringAppender::ccat)).isEqualTo(Reflections.getMethod(StringAppender.class, "ccat", Object[].class, Object.class));
	}
	
	@Test
	void findExecutable() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findExecutable(Object::toString)).isEqualTo(Reflections.getMethod(Object.class, "toString"));
		assertThat(testInstance.findExecutable(Integer::shortValue)).isEqualTo(Reflections.getMethod(Integer.class, "shortValue"));
		assertThat(testInstance.findExecutable(Runnable::run)).isEqualTo(Reflections.getMethod(Runnable.class, "run"));
		assertThat(testInstance.findExecutable(ThreadLocal<Object>::get)).isEqualTo(Reflections.getMethod(ThreadLocal.class, "get"));
		assertThat(testInstance.findExecutable(Collator::setStrength)).isEqualTo(Reflections.getMethod(Collator.class, "setStrength", int.class));
		assertThat(testInstance.findExecutable(String::toCharArray)).isEqualTo(Reflections.getMethod(String.class, "toCharArray"));
		assertThat(testInstance.findExecutable((SerializableBiFunction<List, Object[], Object[]>) List::toArray)).isEqualTo(Reflections.getMethod(List.class, "toArray", Object[].class));
		assertThat(testInstance.findExecutable((SerializableBiConsumer<List, Object[]>) List::toArray)).isEqualTo(Reflections.getMethod(List.class, 
				"toArray", Object[].class));
		assertThat(testInstance.findExecutable(
				(SerializableTriConsumer<String, Integer, Integer>) String::codePointCount)).isEqualTo(Reflections.getMethod(String.class, 
				"codePointCount", int.class, int.class));
		assertThat(testInstance.findExecutable(
				(SerializableTriConsumer<StringAppender, Object[], Object>) StringAppender::ccat)).isEqualTo(Reflections.getMethod(StringAppender.class, "ccat", Object[].class, Object.class));
	}
	
	@Test
	void findMethod_methodDefinedInPackagePrivateClass_throwsException() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThatThrownBy(() -> testInstance.findMethod((SerializableBiConsumer<PackagePrivateInheritedClass, Integer>) PackagePrivateInheritedClass::doSomethingWith))
				.isInstanceOf(UnsupportedOperationException.class)
				.extracting(Throwable::getMessage, InstanceOfAssertFactories.STRING)
				.startsWith("Found method is synthetic which means original one was wrapped by some bytecode "
						+ "(generally to bypass visibility constraint)");
		
		assertThatThrownBy(() -> testInstance.findMethod((SerializableBiConsumer<StringBuilder, Integer>) StringBuilder::ensureCapacity))
				.isInstanceOf(UnsupportedOperationException.class)
				.extracting(Throwable::getMessage, InstanceOfAssertFactories.STRING)
				.startsWith("Found method is synthetic which means original one was wrapped by some bytecode "
						+ "(generally to bypass visibility constraint)");
	}
	
	@Test
	void findMethod_methodDefinedInMethod_throwsException() throws NoSuchMethodException {
		class Tata {
			public void doSomething() {
				
			}
		}
		
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findMethod((SerializableConsumer<Tata>) Tata::doSomething)).isEqualTo(Tata.class.getMethod("doSomething"));
	}
	
	@Test
	void findMethod_methodDefinedInMethod_throwsException2() throws NoSuchMethodException {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findMethod((SerializableConsumer<Tata>) Tata::doSomething)).isEqualTo(Tata.class.getMethod("doSomething"));
	}

	private class Tata {
		public void doSomething() {
			
		}
	}
	
	@Test
	void findConstructor_0arg() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findConstructor((SerializableSupplier<String>) String::new)).isEqualTo(Reflections.getConstructor(String.class));
	}
	
	@Test
	void findConstructor_1arg() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findConstructor((SerializableFunction<String, String>) String::new)).isEqualTo(Reflections.getConstructor(String.class, String.class));
		assertThat(testInstance.findConstructor((SerializableFunction<char[], String>) String::new)).isEqualTo(Reflections.getConstructor(String.class, char[].class));
	}
	
	@Test
	void findConstructor_2args() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findConstructor((SerializableBiFunction<Integer, Float, HashMap>) HashMap::new)).isEqualTo(Reflections.getConstructor(HashMap.class, int.class, float.class));
	}
	
	@Test
	void findConstructor_3args() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findConstructor((SerializableTriFunction<String, String, String, Locale>) Locale::new)).isEqualTo(Reflections.getConstructor(Locale.class, String.class, String.class, String.class));
	}
	
	@Test
	void findConstructor_constructorIsInnerOne_static() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findConstructor(StaticInnerClass::new)).isEqualTo(Reflections.getConstructor(StaticInnerClass.class));
	}
	
	@Test
	void findConstructor_constructorIsInnerOne_static_onlyOneConstructorWithOneArgument() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThat(testInstance.findConstructor(StaticInnerClassWithOnlyOneConstructorWithOneArgument::new)).isEqualTo(Reflections.getConstructor(StaticInnerClassWithOnlyOneConstructorWithOneArgument.class, int.class));
	}
	
	@Test
	void findConstructor_constructorIsInnerOne_nonStatic_throwsException() {
		MethodReferenceCapturer testInstance = new MethodReferenceCapturer();
		assertThatThrownBy(() -> testInstance.findConstructor(NonStaticInnerClass::new))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessage("Capturing by reference a non-static inner class constructor is not supported"
						+ ", make o.g.r.MethodReferenceCapturerTest$NonStaticInnerClass static or an outer class of o.g.r.MethodReferenceCapturerTest");
	}
	
	public static Object[][] giveArgumentTypes_methodDefinition() {
		return new Object[][] {
				{ MethodReferenceCapturer.giveArgumentTypes(MethodReferences.buildSerializedLambda(Object::toString)),
						MethodDefinition.methodDefinition(Object.class, "toString", new Class[0], String.class) },
				{ MethodReferenceCapturer.giveArgumentTypes(MethodReferences.buildSerializedLambda(Integer::shortValue)),
						MethodDefinition.methodDefinition(Integer.class, "shortValue", new Class[0], short.class) },
				{ MethodReferenceCapturer.giveArgumentTypes(MethodReferences.buildSerializedLambda(Collator::setStrength)),
						MethodDefinition.methodDefinition(Collator.class, "setStrength", new Class[] { int.class }, void.class) },
				{ MethodReferenceCapturer.giveArgumentTypes(MethodReferences.buildSerializedLambda(String::toCharArray)),
						MethodDefinition.methodDefinition(String.class, "toCharArray", new Class[0], char[].class) },
				{ MethodReferenceCapturer.giveArgumentTypes(MethodReferences.buildSerializedLambda((SerializableBiConsumer<List, Object[]>) List::toArray)),
						MethodDefinition.methodDefinition(List.class, "toArray", new Class[] { Object[].class }, Object[].class) },
				{ MethodReferenceCapturer.giveArgumentTypes(MethodReferences.buildSerializedLambda((SerializableTriConsumer<String, Integer, Integer>) String::codePointCount)),
						MethodDefinition.methodDefinition(String.class, "codePointCount", new Class[] { int.class, int.class }, int.class) },
				{ MethodReferenceCapturer.giveArgumentTypes(MethodReferences.buildSerializedLambda((SerializableTriConsumer<StringAppender, Object[], Object>) StringAppender::ccat)),
						MethodDefinition.methodDefinition(StringAppender.class, "ccat", new Class[] { Object[].class, Object.class }, StringAppender.class) },
		};
	}
	
	
	@ParameterizedTest
	@MethodSource
	void giveArgumentTypes_methodDefinition(MethodDefinition actual, MethodDefinition expected) {
		// Because MethodDefinition doesn't override equals() (because there's no need for it in MethodReferenceCapturer)
		// we use a dedicated compartor an representation for AssertJ
		Function[] propertiesToPrint = {
				(Function<MethodDefinition, Class>) MethodDefinition::getDeclaringClass,
				(Function<MethodDefinition, String>) MethodDefinition::getName,
				// arrays are hardly comparable so we transform arguments into a List
				chain(MethodDefinition::getArgumentTypes, Arrays::asList),
				(Function<MethodDefinition, Class>) MethodDefinition::getReturnType
		};
		BiPredicate<MethodDefinition, MethodDefinition> predicate = Predicates.and(propertiesToPrint);
		Comparator<MethodDefinition> comparator = (o1, o2) -> predicate.test(o1, o2) ? 0 : -1;
		Representation representation = new Representation() {
			@Override
			public String toStringOf(Object object) {
				return Strings.footPrint(object, propertiesToPrint);
			}
			
			@Override
			public String unambiguousToStringOf(Object object) {
				return object.getClass() + "@" + Integer.toHexString(object.hashCode());
			}
		};
		Assertions.assertThat(actual)
				.usingComparator(comparator).withRepresentation(representation)
				.isEqualTo(expected);
	}
	
	@Test
	void testLRUCache() {
		LRUCache testInstance = new LRUCache(3);
		Constructor<String> dummyExecutable = Reflections.getDefaultConstructor(String.class);
		testInstance.put("b", dummyExecutable);
		testInstance.put("a", dummyExecutable);
		testInstance.put("c", dummyExecutable);
		assertThat(testInstance.keySet()).isEqualTo(Arrays.asSet("b", "a", "c"));
		// adding an overflowing entry makes the very first one to be removed (LRU principle)
		testInstance.put("d", dummyExecutable);
		assertThat(testInstance.keySet()).isEqualTo(Arrays.asSet("a", "c", "d"));
		// adding an already existing one as no influence on the map
		testInstance.put("d", dummyExecutable);
		assertThat(testInstance.keySet()).isEqualTo(Arrays.asSet("a", "c", "d"));
	}
	
	@Test
	void testToMethodReferenceString() throws NoSuchMethodException {
		assertThat(MethodReferences.toMethodReferenceString(String.class.getMethod("concat", String.class))).isEqualTo("String::concat");
		SerializableFunction<String, char[]> toCharArray = String::toCharArray;
		assertThat(MethodReferences.toMethodReferenceString(toCharArray)).isEqualTo("String::toCharArray");
		SerializableBiFunction<String, String, String> concat = String::concat;
		assertThat(MethodReferences.toMethodReferenceString(concat)).isEqualTo("String::concat");
		SerializableTriFunction<String, Integer, Integer, CharSequence> subSequence = String::subSequence;
		assertThat(MethodReferences.toMethodReferenceString(subSequence)).isEqualTo("String::subSequence");
		SerializableBiConsumer<AtomicInteger, Integer> set = AtomicInteger::set;
		assertThat(MethodReferences.toMethodReferenceString(set)).isEqualTo("AtomicInteger::set");
		SerializableConsumer<Map> clear = Map::clear;
		assertThat(MethodReferences.toMethodReferenceString(clear)).isEqualTo("Map::clear");
		SerializableTriConsumer<StaticInnerClass, String, Integer> triConsumerMethod = StaticInnerClass::triConsumerMethod;
		assertThat(MethodReferences.toMethodReferenceString(triConsumerMethod)).isEqualTo("StaticInnerClass::triConsumerMethod");
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