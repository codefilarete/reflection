package org.codefilarete.reflection;

import org.codefilarete.tool.Reflections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Guillaume Mary
 */
public class AccessorByMethodTest {
	
	@Test
	public void testForProperty() {
		AccessorByMethod testInstance = Accessors.accessorByMethod(Toto.class, "a");
		assertThat(Reflections.findMethod(Toto.class, "getA")).isEqualTo(testInstance.getGetter());
	}
	
	@Test
	public void testGet() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getA"));
		Toto toto = new Toto();
		toto.a = 42;
		assertThat(testInstance.get(toto)).isEqualTo((Object) 42);
	}
	
	@Test
	public void testToMutator() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getA"));
		assertThat(testInstance.toMutator().getSetter()).isEqualTo(Reflections.getMethod(Toto.class, "setA", int.class));
	}
	
	@Test
	public void testToMutator_reverseSetterDoesntExist_throwsException() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getFakeProperty"));
		assertThatThrownBy(testInstance::toMutator)
				.isInstanceOf(NonReversibleAccessor.class)
				.hasMessage("Can't find a mutator for o.c.r.AccessorByMethodTest$Toto.getFakeProperty()");
	}
	
	@Test
	public void testToString() {
		AccessorByMethod<Toto, Integer> testInstance = new AccessorByMethod<>(Reflections.findMethod(Toto.class, "getA"));
		assertThat(testInstance.toString()).isEqualTo("o.c.r.AccessorByMethodTest$Toto.getA()");
	}
	
	private static class Toto {
		private int a;
		
		public int getA() {
			return a;
		}
		
		public void setA(int a) {
			this.a = a;
		}
		
		public int getFakeProperty() {
			return 0;
		}
	}
	
}