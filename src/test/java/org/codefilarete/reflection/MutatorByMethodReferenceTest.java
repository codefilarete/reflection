package org.codefilarete.reflection;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Mary
 */
class MutatorByMethodReferenceTest {
	
	@Test
	void set() {
		MutatorByMethodReference<StringBuilder, Integer> testInstance = new MutatorByMethodReference<>(StringBuilder::append);
		StringBuilder target = new StringBuilder();
		testInstance.set(target, 1);
		assertThat(target.toString()).isEqualTo("1");
	}

	@Test
	void equals() {
		// usual case : 2 instances with same method reference should be equal
		MutatorByMethodReference<DummySet, Object> testInstance1 = new MutatorByMethodReference<>(DummySet::contains);
		MutatorByMethodReference<DummySet, Object> testInstance2 = new MutatorByMethodReference<>(DummySet::contains);
		assertThat(testInstance2).isEqualTo(testInstance1);
		
		// still equals to AbstractCollection::contains because DummySet::contains is not implemented and points to AbstractCollection::contains
		MutatorByMethodReference<DummySet, Object> testInstance3 = new MutatorByMethodReference<>(AbstractCollection::contains);
		assertThat(testInstance3).isEqualTo(testInstance1);
		// same test, but with a different generic parameter => instances should be equal
		// (with a different hashCode implementation I had a strange behavior on which generic type influenced serialization !)
		MutatorByMethodReference<AbstractSet, Object> testInstance4 = new MutatorByMethodReference<>(AbstractCollection::contains);
		assertThat(testInstance4).isEqualTo(testInstance1);

		// A totally different method reference shouldn't be equal 
		MutatorByMethodReference<StringBuilder, CharSequence> testInstance5 = new MutatorByMethodReference<>(StringBuilder::append);
		assertThat(testInstance5).isNotEqualTo(testInstance1);
	}
	
	@Test
	void testToString() {
		MutatorByMethodReference<Map, BiConsumer> testInstance = new MutatorByMethodReference<>(Map::forEach);
		assertThat(testInstance.toString()).isEqualTo("j.u.Map::forEach");
	}
	
	static class DummySet<E> extends AbstractSet<E> {
		
		@Override
		public Iterator<E> iterator() {
			return null;
		}
		
		@Override
		public int size() {
			return 0;
		}
	}
	
	@Test
	void getPropertyType() {
		MutatorByMethodReference<DummySet, Object> testInstance1 = new MutatorByMethodReference<>(DummySet::contains);
		assertThat(testInstance1.getPropertyType()).isEqualTo(Object.class);

		MutatorByMethodReference<DummySet, Object> testInstance2 = new MutatorByMethodReference<>(AbstractCollection::contains);
		assertThat(testInstance2.getPropertyType()).isEqualTo(Object.class);

		MutatorByMethodReference<AbstractSet, Object> testInstance3 = new MutatorByMethodReference<>(AbstractCollection::contains);
		assertThat(testInstance3.getPropertyType()).isEqualTo(Object.class);

		MutatorByMethodReference<StringBuilder, CharSequence> testInstance4 = new MutatorByMethodReference<>(StringBuilder::append);
		assertThat(testInstance4.getPropertyType()).isEqualTo(CharSequence.class);
	}
}