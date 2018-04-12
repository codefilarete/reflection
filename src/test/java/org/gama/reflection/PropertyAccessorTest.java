package org.gama.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.gama.reflection.model.City;
import org.gama.reflection.model.Phone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
public class PropertyAccessorTest {
	
	@Test
	public void testOf_fieldInput() {
		Field numberField = Reflections.findField(Phone.class, "number");
		PropertyAccessor<Phone, String> numberAccessor = Accessors.of(numberField);
		assertEquals(new AccessorByField<>(numberField), numberAccessor.getAccessor());
		assertEquals(new MutatorByField<>(numberField), numberAccessor.getMutator());
	}
	
	@Test
	public void testOf_methodInput() {
		Field numberField = Reflections.findField(Phone.class, "number");
		Method numberGetter = Reflections.findMethod(Phone.class, "getNumber");
		PropertyAccessor<Phone, String> numberAccessor = Accessors.of(numberGetter);
		assertEquals(new AccessorByMethod<>(numberGetter), numberAccessor.getAccessor());
		// As there's no setter for "number" field, the mutator is an field one, not a method one
		assertEquals(new MutatorByField<>(numberField), numberAccessor.getMutator());
		
		
		Method nameGetter = Reflections.findMethod(City.class, "getName");
		Method nameSetter = Reflections.findMethod(City.class, "setName", String.class);
		PropertyAccessor<City, String> nameAccessor = Accessors.of(nameGetter);
		assertEquals(new AccessorByMethod<>(nameGetter), nameAccessor.getAccessor());
		// As a setter exists for "name" field, the mutator is a method one, not a field one
		assertEquals(new MutatorByMethod<>(nameSetter), nameAccessor.getMutator());
	}
	
	@Test
	public void testOf_nonConventionalMethodInput_exceptionThrown() {
		Method nameGetter = Reflections.findMethod(City.class, "name");
		
		assertThrows(IllegalArgumentException.class, () -> Accessors.of(nameGetter),
				"Field wrapper j.l.String o.g.r.m.City.name() doesn't feet encapsulation naming convention");
	}
	
}