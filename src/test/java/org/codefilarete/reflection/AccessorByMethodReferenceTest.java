package org.codefilarete.reflection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Mary
 */
public class AccessorByMethodReferenceTest {
	
	@Test
	public void testGet() {
		AccessorByMethodReference<Integer, String> testInstance = new AccessorByMethodReference<>(Number::toString);
		assertThat(testInstance.get(1)).isEqualTo("1");
	}
	
	@Test
	public void testEquals() {
		// usual case : 2 instances with same method reference should be equal
		AccessorByMethodReference<Integer, String> testInstance1 = new AccessorByMethodReference<>(Number::toString);
		AccessorByMethodReference<Integer, String> testInstance2 = new AccessorByMethodReference<>(Number::toString);
		assertThat(testInstance2).isEqualTo(testInstance1);
		
		// still equals to Object::toString because Number::toString is not implemented and points to Object::toString
		AccessorByMethodReference<Integer, String> testInstance3 = new AccessorByMethodReference<>(Object::toString);
		assertThat(testInstance3).isEqualTo(testInstance1);
		
		// A totally different method reference shouldn't be equal 
		AccessorByMethodReference<Integer, String> testInstance4 = new AccessorByMethodReference<>(String::valueOf);
		assertThat(testInstance4).isNotEqualTo(testInstance1);
		AccessorByMethodReference<Integer, String> testInstance5 = new AccessorByMethodReference<>(AccessorByMethodReferenceTest::myToString);
		assertThat(testInstance5).isNotEqualTo(testInstance1);
	}
	
	@Test
	public void testToString() {
		AccessorByMethodReference<String, char[]> testInstance = new AccessorByMethodReference<>(String::toCharArray);
		assertThat(testInstance.toString()).isEqualTo("j.l.String::toCharArray");
	}
	
	private static String myToString(Integer i) {
		return String.valueOf(i);
	}
}