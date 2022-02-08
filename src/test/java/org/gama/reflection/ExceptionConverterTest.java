package org.gama.reflection;

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
public class ExceptionConverterTest {
	
	@Test
	public void testConvertException_wrongConversion() {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertThat(field_a).isNotNull();
		Reflections.ensureAccessible(field_a);
		
		MutatorByField<Toto, Object> accessorByField = new MutatorByField<>(field_a);
		
		Toto target = new Toto();
		assertThatThrownBy(() -> accessorByField.set(target, 0L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying mutator for field o.g.r.ExceptionConverterTest$Toto.a on instance of o.g.r.ExceptionConverterTest$Toto with value 0")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("Field o.g.r.ExceptionConverterTest$Toto.a of type j.l.Integer is not compatible with j.l.Long");
	}
	
	@Test
	public void testConvertException_missingField() {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertThat(field_a).isNotNull();
		Reflections.ensureAccessible(field_a);
		
		MutatorByField<Tata, Object> mutatorByField = new MutatorByField<>(field_a);
		
		Tata target = new Tata();
		assertThatThrownBy(() -> mutatorByField.set(target, 0L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying mutator for field o.g.r.ExceptionConverterTest$Toto.a on instance of o.g.r.ExceptionConverterTest$Tata with value 0")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("Field o.g.r.ExceptionConverterTest$Toto.a doesn't exist in o.g.r.ExceptionConverterTest$Tata");
	}
	
	@Test
	public void testConvertException_primitiveField() {
		Field field_b = Reflections.findField(Toto.class, "b");
		assertThat(field_b).isNotNull();
		Reflections.ensureAccessible(field_b);
		
		MutatorByField<Toto, Object> mutatorByField = new MutatorByField<>(field_b);
		
		Toto target = new Toto();
		
		assertThatThrownBy(() -> mutatorByField.set(target, null))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying mutator for field o.g.r.ExceptionConverterTest$Toto.b on instance of o.g.r.ExceptionConverterTest$Toto with value null")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("Field o.g.r.ExceptionConverterTest$Toto.b of type int is not compatible with null");
	}
	
	@Test
	public void testConvertException_typeMismatch() {
		Method methodSetA = Reflections.getMethod(Toto.class, "setA", Integer.class);
		Reflections.ensureAccessible(methodSetA);
		
		MutatorByMethod<Toto, Object> accessorByMethod = new MutatorByMethod<>(methodSetA);
		
		Toto target = new Toto();
		assertThatThrownBy(() -> accessorByMethod.set(target, "42"))
		.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying o.g.r.ExceptionConverterTest$Toto.setA(j.l.Integer) on instance of o.g.r.ExceptionConverterTest$Toto with value 42")
				.extracting(Throwable::getCause, THROWABLE)
				.hasMessage("o.g.r.ExceptionConverterTest$Toto.setA(j.l.Integer) expects j.l.Integer as argument, but j.l.String was given");
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
}