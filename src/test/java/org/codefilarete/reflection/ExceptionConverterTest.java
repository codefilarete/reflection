package org.codefilarete.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.codefilarete.tool.Reflections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.THROWABLE;

/**
 * @author Guillaume Mary
 */
class ExceptionConverterTest {
	
	@Test
	void convertException_wrongConversion() {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertThat(field_a).isNotNull();
		Reflections.ensureAccessible(field_a);
		
		MutatorByField<Toto, Object> accessorByField = new MutatorByField<>(field_a);
		
		Toto target = new Toto();
		assertThatThrownBy(() -> accessorByField.set(target, 0L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying mutator for field o.c.r.ExceptionConverterTest$Toto.a on instance of o.c.r.ExceptionConverterTest$Toto with value 0")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("Field o.c.r.ExceptionConverterTest$Toto.a of type j.l.Integer is not compatible with j.l.Long");
	}
	
	@Test
	void convertException_missingField() {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertThat(field_a).isNotNull();
		Reflections.ensureAccessible(field_a);
		
		MutatorByField<Tata, Object> mutatorByField = new MutatorByField<>(field_a);
		
		Tata target = new Tata();
		assertThatThrownBy(() -> mutatorByField.set(target, 0L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying mutator for field o.c.r.ExceptionConverterTest$Toto.a on instance of o.c.r.ExceptionConverterTest$Tata with value 0")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("Field o.c.r.ExceptionConverterTest$Toto.a doesn't exist in o.c.r.ExceptionConverterTest$Tata");
	}
	
	@Test
	void convertException_fieldExistButTypesAreNotCompatible() {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertThat(field_a).isNotNull();
		Reflections.ensureAccessible(field_a);
		
		MutatorByField<Object, Object> mutatorByField = new MutatorByField<>(field_a);
		
		Titi target = new Titi();
		assertThatThrownBy(() -> mutatorByField.set(target, 0L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying mutator for field o.c.r.ExceptionConverterTest$Toto.a on instance of o.c.r.ExceptionConverterTest$Titi with value 0")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("Field o.c.r.ExceptionConverterTest$Toto.a cannot be applied on instance of type o.c.r.ExceptionConverterTest$Titi");
	}
	
	@Test
	void convertException_primitiveField() {
		Field field_b = Reflections.findField(Toto.class, "b");
		assertThat(field_b).isNotNull();
		Reflections.ensureAccessible(field_b);
		
		MutatorByField<Toto, Object> mutatorByField = new MutatorByField<>(field_b);
		
		Toto target = new Toto();
		
		assertThatThrownBy(() -> mutatorByField.set(target, null))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying mutator for field o.c.r.ExceptionConverterTest$Toto.b on instance of o.c.r.ExceptionConverterTest$Toto with value null")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("Field o.c.r.ExceptionConverterTest$Toto.b of type int is not compatible with null");
	}
	
	@Test
	void convertException_typeMismatch() {
		Method methodSetA = Reflections.getMethod(Toto.class, "setA", Integer.class);
		Reflections.ensureAccessible(methodSetA);
		
		MutatorByMethod<Toto, Object> accessorByMethod = new MutatorByMethod<>(methodSetA);
		
		Toto target = new Toto();
		assertThatThrownBy(() -> accessorByMethod.set(target, "42"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying o.c.r.ExceptionConverterTest$Toto.setA(j.l.Integer) on instance of o.c.r.ExceptionConverterTest$Toto with value 42")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("o.c.r.ExceptionConverterTest$Toto.setA(j.l.Integer) expects j.l.Integer as argument, but j.l.String was given");
	}
	
	private static class Toto {
		
		private Integer a;
		
		private int b;
		
		public void setA(Integer a) {
			this.a = a;
		}
	}
	
	private static class Tata {
		
	}
	
	private static class Titi {
		
		private Integer a;
	}
}