package org.codefilarete.reflection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Guillaume Mary
 */
public class ArrayMutatorTest {
	
	@Test
	public void set() {
		ArrayMutator<String> testInstance = new ArrayMutator<>();
		String[] sample = { "a", "b", "c" };
		
		testInstance.setIndex(0);
		testInstance.set(sample, "x");
		assertThat(new String[]{"x", "b", "c"}).isEqualTo(sample);
		testInstance.setIndex(1);
		testInstance.set(sample, "y");
		assertThat(new String[]{"x", "y", "c"}).isEqualTo(sample);
		testInstance.setIndex(2);
		testInstance.set(sample, "z");
		assertThat(new String[]{"x", "y", "z"}).isEqualTo(sample);
	}
	
	@Test
	public void set_ArrayIndexOutOfBoundsException() {
		ArrayMutator<String> testInstance = new ArrayMutator<>();
		String[] sample = { "a", "b", "c" };
		
		testInstance.setIndex(-1);
		assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class).isThrownBy(() -> testInstance.set(sample, "x"));
	}
}