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
public class ListAccessorTest {
	
	@Test
	public void testGet() {
		ListAccessor<List<String>, String> testInstance = new ListAccessor<>();
		List<String> sample = Arrays.asList("a", "b", "c");
		
		testInstance.setIndex(0);
		assertThat(testInstance.get(sample)).isEqualTo("a");
		testInstance.setIndex(1);
		assertThat(testInstance.get(sample)).isEqualTo("b");
		testInstance.setIndex(2);
		assertThat(testInstance.get(sample)).isEqualTo("c");
	}
	
	@Test
	public void testGet_ArrayIndexOutOfBoundsException() {
		ListAccessor<List<String>, String> testInstance = new ListAccessor<>();
		List<String> sample = Arrays.asList("a", "b", "c");
		
		testInstance.setIndex(-1);
		assertThatThrownBy(() -> testInstance.get(sample))
				.extracting(t -> Exceptions.findExceptionInCauses(t, ArrayIndexOutOfBoundsException.class))
				.isNotNull();
	}
}