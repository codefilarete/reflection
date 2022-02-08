package org.gama.reflection;

import java.util.List;

import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.tool.exception.Exceptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
		assertThat(Arrays.asList("x", "b", "c")).isEqualTo(sample);
		testInstance.setIndex(1);
		testInstance.set(sample, "y");
		assertThat(Arrays.asList("x", "y", "c")).isEqualTo(sample);
		testInstance.setIndex(2);
		testInstance.set(sample, "z");
		assertThat(Arrays.asList("x", "y", "z")).isEqualTo(sample);
	}
	
	@Test
	public void testSet_ArrayIndexOutOfBoundsException() {
		ListMutator<List<String>, String> testInstance = new ListMutator<>();
		List<String> sample = Arrays.asList("a", "b", "c");
		
		testInstance.setIndex(-1);
		assertThatThrownBy(() -> testInstance.set(sample, "x"))
				.extracting(t -> Exceptions.findExceptionInCauses(t, ArrayIndexOutOfBoundsException.class))
				.isNotNull();
	}
}