package org.gama.reflection;

import org.gama.lang.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
public class MutatorByMethodTest {
	
	@Test
	public void testForProperty() {
		MutatorByMethod testInstance = Accessors.mutatorByMethod(Toto.class, "a");
		assertEquals(testInstance.getSetter(), Reflections.findMethod(Toto.class, "setA", int.class));
	}
	
	@Test
	public void testSet() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		Toto toto = new Toto();
		testInstance.set(toto, 42);
		assertEquals(42, toto.a);
	}
	
	@Test
	public void testSet_withWrongArgument() {
		MutatorByMethod<Toto, Object> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		Toto toto = new Toto();
		RuntimeException thrownException = assertThrows(RuntimeException.class, () -> testInstance.set(toto, "42"));
		assertEquals("Error while applying o.g.r.MutatorByMethodTest$Toto.setA(int) on instance of o.g.r.MutatorByMethodTest$Toto with value 42", thrownException.getMessage());
		assertEquals("o.g.r.MutatorByMethodTest$Toto.setA(int) expects int as argument, but j.l.String was given", thrownException.getCause().getMessage());
	}
	
	@Test
	public void testToMutator() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		assertEquals(Reflections.getMethod(Toto.class, "getA"), testInstance.toAccessor().getGetter());
	}
	
	@Test
	public void testToMutator_reverseSetterDoesntExist_throwsException() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setFakeProperty", int.class));
		assertEquals("Can't find a mutator for o.g.r.MutatorByMethodTest$Toto.setFakeProperty(int)",
				assertThrows(NonReversibleAccessor.class, testInstance::toAccessor).getMessage());
	}
	
	@Test
	public void testToString() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		assertEquals("o.g.r.MutatorByMethodTest$Toto.setA(int)", testInstance.toString());
	}
	
	private static class Toto {
		private int a;
		
		public int getA() {
			return a;
		}
		
		public void setA(int a) {
			this.a = a;
		}
		
		public void setFakeProperty(int a) {
			this.a = a;
		}
	}
	
}