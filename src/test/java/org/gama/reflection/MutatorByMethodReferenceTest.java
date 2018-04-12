package org.gama.reflection;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Guillaume Mary
 */
public class MutatorByMethodReferenceTest {
	
	@Test
	public void testSet() {
		MutatorByMethodReference<StringBuilder, Integer> testInstance = new MutatorByMethodReference<>(StringBuilder::append);
		StringBuilder target = new StringBuilder();
		testInstance.set(target, 1);
		assertEquals("1", target.toString());
	}

	@Test
	public void testEquals() {
		// usual case : 2 instances with same method reference should be equal
		MutatorByMethodReference<DummySet, Object> testInstance1 = new MutatorByMethodReference<>(DummySet::contains);
		MutatorByMethodReference<DummySet, Object> testInstance2 = new MutatorByMethodReference<>(DummySet::contains);
		assertEquals(testInstance1, testInstance2);
		
		// still equals to AbstractCollection::contains because DummySet::contains is not implemented and points to AbstractCollection::contains
		MutatorByMethodReference<DummySet, Object> testInstance3 = new MutatorByMethodReference<>(AbstractCollection::contains);
		assertEquals(testInstance1, testInstance3);
		// same test, but with a different generic parameter => instances should be equal
		// (with a different hashCode implementation I had a strange behavior on which generic type influenced serilization !)
		MutatorByMethodReference<AbstractSet, Object> testInstance4 = new MutatorByMethodReference<>(AbstractCollection::contains);
		assertEquals(testInstance1, testInstance4);

		// A totally different method reference shouldn't be equal 
		MutatorByMethodReference<StringBuilder, CharSequence> testInstance5 = new MutatorByMethodReference<>(StringBuilder::append);
		assertNotEquals(testInstance1, testInstance5);
	}
	
	
	public static class DummySet<E> extends AbstractSet<E> {
		
		@Override
		public Iterator<E> iterator() {
			return null;
		}
		
		@Override
		public int size() {
			return 0;
		}
	}
}