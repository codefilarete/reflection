package org.gama.reflection;

import java.util.List;

import org.gama.lang.collection.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
public class ListMutatorTest {
	
	@Test
	public void testSet() {
		ListMutator<List<String>, String> testInstance = new ListMutator<>();
		List<String> sample = Arrays.asList("a", "b", "c");
		
		testInstance.setIndex(0);
		testInstance.set(sample, "x");
		assertEquals(sample, Arrays.asList("x", "b", "c"));
		testInstance.setIndex(1);
		testInstance.set(sample, "y");
		assertEquals(sample, Arrays.asList("x", "y", "c"));
		testInstance.setIndex(2);
		testInstance.set(sample, "z");
		assertEquals(sample, Arrays.asList("x", "y", "z"));
	}
	
	@Test
	public void testSet_ArrayIndexOutOfBoundsException() {
		ListMutator<List<String>, String> testInstance = new ListMutator<>();
		List<String> sample = Arrays.asList("a", "b", "c");
		
		testInstance.setIndex(-1);
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> testInstance.set(sample, "x"));
	}
}