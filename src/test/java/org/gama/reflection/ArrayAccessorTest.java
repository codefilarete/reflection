package org.gama.reflection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
public class ArrayAccessorTest {
	
	@Test
	public void testGet() {
		ArrayAccessor<String> testInstance = new ArrayAccessor<>();
		String[] sample = { "a", "b", "c" };
		
		testInstance.setIndex(0);
		assertEquals("a", testInstance.get(sample));
		testInstance.setIndex(1);
		assertEquals("b", testInstance.get(sample));
		testInstance.setIndex(2);
		assertEquals("c", testInstance.get(sample));
	}
	
	@Test
	public void testGet_ArrayIndexOutOfBoundsException() {
		ArrayAccessor<String> testInstance = new ArrayAccessor<>();
		String[] sample = { "a", "b", "c" };
		
		testInstance.setIndex(-1);
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> testInstance.get(sample));
	}
}