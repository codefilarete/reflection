package org.codefilarete.reflection;

import org.codefilarete.tool.Reflections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.THROWABLE;

/**
 * @author Guillaume Mary
 */
public class MutatorByMethodTest {
	
	@Test
	public void testForProperty() {
		MutatorByMethod testInstance = Accessors.mutatorByMethod(Toto.class, "a");
		assertThat(Reflections.findMethod(Toto.class, "setA", int.class)).isEqualTo(testInstance.getSetter());
	}
	
	@Test
	public void testSet() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		Toto toto = new Toto();
		testInstance.set(toto, 42);
		assertThat(toto.a).isEqualTo(42);
	}
	
	@Test
	public void testSet_withWrongArgument() {
		MutatorByMethod<Toto, Object> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		Toto toto = new Toto();
		assertThatThrownBy(() -> testInstance.set(toto, "42"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying o.c.r.MutatorByMethodTest$Toto.setA(int) on instance of o.c.r.MutatorByMethodTest$Toto with value 42")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("o.c.r.MutatorByMethodTest$Toto.setA(int) expects int as argument, but j.l.String was given");
	}
	
	@Test
	public void testToMutator() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		assertThat(testInstance.toAccessor().getGetter()).isEqualTo(Reflections.getMethod(Toto.class, "getA"));
	}
	
	@Test
	public void testToMutator_reverseSetterDoesntExist_throwsException() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setFakeProperty", int.class));
		assertThatThrownBy(testInstance::toAccessor)
				.isInstanceOf(NonReversibleAccessor.class)
				.hasMessage("Can't find a mutator for o.c.r.MutatorByMethodTest$Toto.setFakeProperty(int)");
	}
	
	@Test
	public void testToString() {
		MutatorByMethod<Toto, Integer> testInstance = new MutatorByMethod<>(Reflections.findMethod(Toto.class, "setA", int.class));
		assertThat(testInstance.toString()).isEqualTo("o.c.r.MutatorByMethodTest$Toto.setA(int)");
	}
	
	private static class Toto {
		private int a;
		
		public int getA() {
			return a;
		}
		
		public void setA(int a) {
			this.a = a;
		}
		
		public void setFakeProperty(int a) {
			this.a = a;
		}
	}
	
}