package org.gama.reflection;

import java.lang.reflect.Field;

import org.gama.lang.Reflections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Guillaume Mary
 */
public class ExceptionConverterTest {
	
	@Test
	public void testConvertException_wrongConversion() throws Throwable {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertNotNull(field_a);
		field_a.setAccessible(true);
		
		MutatorByField<Toto, Object> accessorByField = new MutatorByField<>(field_a);
		
		Toto target = new Toto();
		Throwable thrownThrowable = null;
		try {
			accessorByField.set(target, 0L);
		} catch (IllegalArgumentException e) {
			thrownThrowable = e;
		}
		
		assertTrue(thrownThrowable.getMessage().contains("can't be used with"));
	}
	
	@Test
	public void testConvertException_missingField() throws Throwable {
		Field field_a = Reflections.findField(Toto.class, "a");
		assertNotNull(field_a);
		field_a.setAccessible(true);
		
		MutatorByField<Tata, Object> mutatorByField = new MutatorByField<>(field_a);
		
		Tata target = new Tata();
		Throwable thrownThrowable = null;
		try {
			mutatorByField.set(target, 0L);
		} catch (IllegalArgumentException e) {
			thrownThrowable = e;
		}
		
		assertTrue(thrownThrowable.getMessage().contains("doesn't have field"));
	}
	
	@Test
	public void testConvertException_primitiveField() throws Throwable {
		Field field_b = Reflections.findField(Toto.class, "b");
		assertNotNull(field_b);
		field_b.setAccessible(true);
		
		MutatorByField<Toto, Object> mutatorByField = new MutatorByField<>(field_b);
		
		Toto target = new Toto();
		Throwable thrownThrowable = null;
		try {
			mutatorByField.set(target, null);
		} catch (IllegalArgumentException e) {
			thrownThrowable = e;
		}
		
		assertTrue(thrownThrowable.getMessage().contains("can't be used with null"));
	}
	
	private static class Toto {
		
		private Integer a;
		
		private int b;
	}
	
	private static class Tata {
		
	}
}