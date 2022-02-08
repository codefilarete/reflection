package org.gama.reflection;

import org.codefilarete.tool.Reflections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Mary
 */
public class MutatorByFieldTest {
	
	@Test
	public void testSet() {
		MutatorByField<Toto, Integer> testInstance = new MutatorByField<>(Reflections.findField(Toto.class, "a"));
		Toto toto = new Toto();
		testInstance.set(toto, 42);
		assertThat(toto.a).isEqualTo(42);
	}
	
	private static class Toto {
		private int a;
	}
	
}