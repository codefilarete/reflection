package org.gama.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
public class ExceptionConverterTest {
	
	@Test
	public void testConvertException_wrongConversion() {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertNotNull(field_a);
		Reflections.ensureAccessible(field_a);
		
		MutatorByField<Toto, Object> accessorByField = new MutatorByField<>(field_a);
		
		Toto target = new Toto();
		IllegalArgumentException thrownThrowable = assertThrows(IllegalArgumentException.class, () -> accessorByField.set(target, 0L));
		assertEquals("Field o.g.r.ExceptionConverterTest$Toto.a of type j.l.Integer is not compatible with j.l.Long", thrownThrowable.getMessage());
	}
	
	@Test
	public void testConvertException_missingField() {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertNotNull(field_a);
		Reflections.ensureAccessible(field_a);
		
		MutatorByField<Tata, Object> mutatorByField = new MutatorByField<>(field_a);
		
		Tata target = new Tata();
		IllegalArgumentException thrownThrowable = assertThrows(IllegalArgumentException.class, () -> mutatorByField.set(target, 0L));
		assertEquals("Field o.g.r.ExceptionConverterTest$Toto.a doesn't exist in o.g.r.ExceptionConverterTest$Tata", thrownThrowable.getMessage());
	}
	
	@Test
	public void testConvertException_primitiveField() {
		Field field_b = Reflections.findField(Toto.class, "b");
		assertNotNull(field_b);
		Reflections.ensureAccessible(field_b);
		
		MutatorByField<Toto, Object> mutatorByField = new MutatorByField<>(field_b);
		
		Toto target = new Toto();
		IllegalArgumentException thrownThrowable = assertThrows(IllegalArgumentException.class, () -> mutatorByField.set(target, null));
		assertEquals("Field o.g.r.ExceptionConverterTest$Toto.b of type int is not compatible with null", thrownThrowable.getMessage());
	}
	
	@Test
	public void testConvertException_typeMismatch() {
		Method methodSetA = Reflections.getMethod(Toto.class, "setA", Integer.class);
		Reflections.ensureAccessible(methodSetA);
		
		MutatorByMethod<Toto, Object> accessorByMethod = new MutatorByMethod<>(methodSetA);
		
		Toto target = new Toto();
		IllegalArgumentException thrownThrowable = assertThrows(IllegalArgumentException.class, () -> accessorByMethod.set(target, "42"));
		assertEquals("o.g.r.ExceptionConverterTest$Toto.setA(j.l.Integer) expects j.l.Integer as argument, but j.l.String was given", thrownThrowable.getMessage());
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