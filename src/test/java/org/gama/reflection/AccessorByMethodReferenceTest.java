package org.gama.reflection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Guillaume Mary
 */
public class AccessorByMethodReferenceTest {
	
	@Test
	public void testGet() {
		AccessorByMethodReference<Integer, String> testInstance = new AccessorByMethodReference<>(Number::toString);
		assertEquals("1", testInstance.get(1));
	}
	
	@Test
	public void testEquals() {
		// usual case : 2 instances with same method reference should be equal
		AccessorByMethodReference<Integer, String> testInstance1 = new AccessorByMethodReference<>(Number::toString);
		AccessorByMethodReference<Integer, String> testInstance2 = new AccessorByMethodReference<>(Number::toString);
		assertEquals(testInstance1, testInstance2);
		
		// still equals to Object::toString because Number::toString is not implemented and points to Object::toString
		AccessorByMethodReference<Integer, String> testInstance3 = new AccessorByMethodReference<>(Object::toString);
		assertEquals(testInstance1, testInstance3);
		
		// A totally different method reference shouldn't be equal 
		AccessorByMethodReference<Integer, String> testInstance4 = new AccessorByMethodReference<>(String::valueOf);
		assertNotEquals(testInstance1, testInstance4);
		AccessorByMethodReference<Integer, String> testInstance5 = new AccessorByMethodReference<>(AccessorByMethodReferenceTest::myToString);
		assertNotEquals(testInstance1, testInstance5);
	}
	
	@Test
	public void testToString() {
		AccessorByMethodReference<String, char[]> testInstance = new AccessorByMethodReference<>(String::toCharArray);
		assertEquals("method reference for java/lang/String.toCharArray()", testInstance.getGetterDescription());
	}
	
	private static String myToString(Integer i) {
		return String.valueOf(i);
	}
}