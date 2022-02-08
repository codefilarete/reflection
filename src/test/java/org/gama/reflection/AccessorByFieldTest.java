package org.gama.reflection;

import org.codefilarete.tool.Reflections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Mary
 */
public class AccessorByFieldTest {
	
	@Test
	public void testGet() {
		AccessorByField<Toto, Integer> testInstance = new AccessorByField<>(Reflections.findField(Toto.class, "a"));
		Toto toto = new Toto();
		toto.a = 42;
		assertThat((int) testInstance.get(toto)).isEqualTo(42);
	}
	
	private static class Toto {
		private int a;
	}
	
}