package org.gama.reflection;

import org.gama.lang.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	
	private static class Toto {
		private int a;
		
		public int getA() {
			return a;
		}
		
		public void setA(int a) {
			this.a = a;
		}
	}
	
}