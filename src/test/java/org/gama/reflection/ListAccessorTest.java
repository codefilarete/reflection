package org.gama.reflection;

import java.util.List;

import org.gama.lang.collection.Arrays;
import org.gama.lang.test.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
public class ListAccessorTest {
	
	@Test
	public void testGet() {
		ListAccessor<List<String>, String> testInstance = new ListAccessor<>();
		List<String> sample = Arrays.asList("a", "b", "c");
		
		testInstance.setIndex(0);
		assertEquals("a", testInstance.get(sample));
		testInstance.setIndex(1);
		assertEquals("b", testInstance.get(sample));
		testInstance.setIndex(2);
		assertEquals("c", testInstance.get(sample));
	}
	
	@Test
	public void testGet_ArrayIndexOutOfBoundsException() {
		ListAccessor<List<String>, String> testInstance = new ListAccessor<>();
		List<String> sample = Arrays.asList("a", "b", "c");
		
		testInstance.setIndex(-1);
		Assertions.assertThrows(() -> testInstance.get(sample), Assertions.hasExceptionInCauses(ArrayIndexOutOfBoundsException.class));
	}
}