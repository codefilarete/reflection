package org.gama.reflection;

import org.gama.lang.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
public class MutatorByFieldTest {
	
	@Test
	public void testSet() {
		MutatorByField<Toto, Integer> testInstance = new MutatorByField<>(Reflections.findField(Toto.class, "a"));
		Toto toto = new Toto();
		testInstance.set(toto, 42);
		assertEquals(42, toto.a);
	}
	
	private static class Toto {
		private int a;
	}
	
}