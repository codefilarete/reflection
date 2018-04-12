package org.gama.reflection;

import org.gama.lang.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
public class AccessorByFieldTest {
	
	@Test
	public void testGet() {
		AccessorByField<Toto, Integer> testInstance = new AccessorByField<>(Reflections.findField(Toto.class, "a"));
		Toto toto = new Toto();
		toto.a = 42;
		assertEquals(42, (int) testInstance.get(toto));
	}
	
	private static class Toto {
		private int a;
	}
	
}