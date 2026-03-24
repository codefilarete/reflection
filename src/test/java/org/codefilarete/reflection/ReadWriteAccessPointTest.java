package org.codefilarete.reflection;

import org.codefilarete.reflection.model.City;
import org.codefilarete.reflection.model.Phone;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Reflections.MemberNotFoundException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Guillaume Mary
 */
public class ReadWriteAccessPointTest {
	
	@Test
	public void testOf_fieldInput() {
		Field numberField = Reflections.findField(Phone.class, "number");
		DefaultReadWritePropertyAccessPoint<Object, Object> numberAccessor = Accessors.accessor(numberField);
		assertThat(numberAccessor.getReader()).isEqualTo(new AccessorByField<>(numberField));
		assertThat(numberAccessor.getWriter()).isEqualTo(new MutatorByField<>(numberField));
	}
	
	@Test
	public void testOf_methodInput() {
		Field numberField = Reflections.findField(Phone.class, "number");
		Method numberGetter = Reflections.findMethod(Phone.class, "getNumber");
		DefaultReadWritePropertyAccessPoint<Object, Object> numberAccessor = Accessors.accessor(numberGetter);
		assertThat(numberAccessor.getReader()).isEqualTo(new AccessorByMethod<>(numberGetter));
		// As there's no setter for "number" field, the mutator is an field one, not a method one
		assertThat(numberAccessor.getWriter()).isEqualTo(new MutatorByField<>(numberField));
		
		
		Method nameGetter = Reflections.findMethod(City.class, "getName");
		Method nameSetter = Reflections.findMethod(City.class, "setName", String.class);
		DefaultReadWritePropertyAccessPoint<Object, Object> nameAccessor = Accessors.accessor(nameGetter);
		assertThat(nameAccessor.getReader()).isEqualTo(new AccessorByMethod<>(nameGetter));
		// As a setter exists for "name" field, the mutator is a method one, not a field one
		assertThat(nameAccessor.getWriter()).isEqualTo(new MutatorByMethod<>(nameSetter));
	}
	
	@Test
	public void testOf_nonConventionalMethodInput_exceptionThrown() {
		Method nameGetter = Reflections.findMethod(City.class, "name");
		
		assertThatExceptionOfType(MemberNotFoundException.class).as("Field wrapper j.l.String o.c.r.m.City.name() doesn't feet encapsulation naming convention").isThrownBy(() -> Accessors.accessor(nameGetter));
	}
	
}