package org.gama.reflection;

import org.gama.lang.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
public class AccessorByMethodTest {
	
	@Test
	public void testForProperty() {
		AccessorByMethod testInstance = Accessors.accessorByMethod(Toto.class, "a");
		assertEquals(testInstance.getGetter(), Reflections.findMethod(Toto.class, "getA"));
	}
	
	@Test
	public void testGet() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getA"));
		Toto toto = new Toto();
		toto.a = 42;
		assertEquals((Object) 42, testInstance.get(toto));
	}
	
	@Test
	public void testToMutator() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getA"));
		assertEquals(Reflections.getMethod(Toto.class, "setA", int.class), testInstance.toMutator().getSetter());
	}
	
	@Test
	public void testToMutator_reverseSetterDoesntExist_throwsException() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getFakeProperty"));
		assertEquals("Can't find a mutator for int o.g.r.AccessorByMethodTest$Toto.getFakeProperty()",
				assertThrows(NonReversibleAccessor.class, testInstance::toMutator).getMessage());
	}
	
	@Test
	public void testToString() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getA"));
		assertEquals("int o.g.r.AccessorByMethodTest$Toto.getA()", testInstance.toString());
	}
	
	private static class Toto {
		private int a;
		
		public int getA() {
			return a;
		}
		
		public void setA(int a) {
			this.a = a;
		}
		
		public int getFakeProperty() {
			return 0;
		}
	}
	
}