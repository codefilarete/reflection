package org.codefilarete.reflection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Guillaume Mary
 */
public class ArrayAccessorTest {
	
	@Test
	public void testGet() {
		ArrayAccessor<String> testInstance = new ArrayAccessor<>();
		String[] sample = { "a", "b", "c" };
		
		testInstance.setIndex(0);
		assertThat(testInstance.get(sample)).isEqualTo("a");
		testInstance.setIndex(1);
		assertThat(testInstance.get(sample)).isEqualTo("b");
		testInstance.setIndex(2);
		assertThat(testInstance.get(sample)).isEqualTo("c");
	}
	
	@Test
	public void testGet_ArrayIndexOutOfBoundsException() {
		ArrayAccessor<String> testInstance = new ArrayAccessor<>();
		String[] sample = { "a", "b", "c" };
		
		testInstance.setIndex(-1);
		assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class).isThrownBy(() -> testInstance.get(sample));
	}
}