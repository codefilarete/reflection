package org.codefilarete.reflection;

import java.lang.reflect.Method;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.codefilarete.reflection.model.Address;
import org.codefilarete.reflection.model.City;
import org.codefilarete.reflection.model.Person;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.tool.exception.Exceptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.codefilarete.reflection.AccessorDefinition.giveDefinition;

/**
 * @author Guillaume Mary
 */
class AccessorDefinitionTest {
	
	static Object[][] giveMemberDefinition() {
		return new Object[][] {
				// accessor
				{ Accessors.accessorByField(Person.class, "name"), Person.class, "name", String.class },
				{ new AccessorByMethod<>(Person.class, "getName"), Person.class, "name", String.class },
				{ new AccessorByMethodReference<>(Person::getName), Person.class, "name", String.class },
				
				// mutator
				{ Accessors.mutatorByField(Person.class, "name"), Person.class, "name", String.class },
				{ new MutatorByMethod<>(Person.class, "setName", String.class), Person.class, "name", String.class },
				{ new MutatorByMethodReference<>(Person::setName), Person.class, "name", String.class },
				
				{ new AccessorChain<>(Arrays.asList(new AccessorByMethodReference<>(Person::getAddress), new AccessorByMethodReference<>(Address::getCity))),
						Person.class, "address.city", City.class },
				
				{ new DefinedAccessorByMethod<>(Reflections.getMethod(Person.class, "setName", String.class), new AccessorDefinition(String.class, "non sense", Integer.class)),
						String.class, "non sense", Integer.class}
		};
	}
	
	@ParameterizedTest
	@MethodSource("giveMemberDefinition")
	void giveMemberDefinition(ValueAccessPoint<?> o, Class expectedDeclaringClass, String expectedName, Class expectedMemberType) {
		AccessorDefinition accessorDefinition = giveDefinition(o);
		assertThat(accessorDefinition.getDeclaringClass()).isEqualTo(expectedDeclaringClass);
		assertThat(accessorDefinition.getName()).isEqualTo(expectedName);
		assertThat(accessorDefinition.getMemberType()).isEqualTo(expectedMemberType);
	}
	
	@Test
	void giveMemberDefinition_method() throws NoSuchMethodException {
		AccessorDefinition accessorDefinition = giveDefinition(String.class.getMethod("getBytes"));
		assertThat(accessorDefinition.getDeclaringClass()).isEqualTo(String.class);
		assertThat(accessorDefinition.getName()).isEqualTo("bytes");
		assertThat(accessorDefinition.getMemberType()).isEqualTo(byte[].class);
	}
	
	@Test
	void giveMemberDefinition_field() throws NoSuchFieldException {
		AccessorDefinition accessorDefinition = giveDefinition(String.class.getDeclaredField("value"));
		assertThat(accessorDefinition.getDeclaringClass()).isEqualTo(String.class);
		assertThat(accessorDefinition.getName()).isEqualTo("value");
		assertThat(accessorDefinition.getMemberType()).isEqualTo(char[].class);
	}
	
	@Test
	void giveMemberDefinition_nullArgument() {
		assertThatThrownBy(() -> giveDefinition((ValueAccessPoint<?>) null))
				.extracting(t -> Exceptions.findExceptionInCauses(t, UnsupportedOperationException.class), InstanceOfAssertFactories.THROWABLE)
				.hasMessage("Accessor type is unsupported to compute its definition : null");
	}
	
	static Object[][] testToString() {
		return new Object[][] {
				{ Accessors.accessorByField(Person.class, "name"), "o.c.r.m.Person.name" },
				{ new AccessorByMethod<>(Person.class, "getName"), "o.c.r.m.Person.getName()" },
				{ new AccessorByMethodReference<>(Person::getName), "Person::getName" },
				
				// mutator
				{ Accessors.mutatorByField(Person.class, "name"), "o.c.r.m.Person.name" },
				{ new MutatorByMethod<>(Person.class, "setName", String.class), "o.c.r.m.Person.setName(j.l.String)" },
				{ new MutatorByMethodReference<>(Person::setName), "Person::setName" },
				
				{ new AccessorChain<>(Arrays.asList(new AccessorByMethodReference<>(Person::getAddress), new AccessorByMethodReference<>(Address::getCity))),
						"Person::getAddress > Address::getCity" },
				
				{ new PropertyAccessor<>(new AccessorByMethodReference<>(Person::getName), new MutatorByMethodReference<>(Person::setName)), "Person::getName" },
				{ new PropertyAccessor<>(new AccessorByMethod<>(Person.class, "getName"), new MutatorByMethod<>(Person.class, "setName", String.class)),
						"o.c.r.m.Person.getName()" },
				{ null, "null" }
		};
	}
	
	@ParameterizedTest
	@MethodSource("testToString")
	void toString(ValueAccessPoint<?> valueAccessPoint, String expectedResult) {
		assertThat(AccessorDefinition.toString(valueAccessPoint)).isEqualTo(expectedResult);
	}
	
	@Test
	void toString_collection() {
		assertThat(AccessorDefinition.toString(Arrays.asList(
				new AccessorByMethodReference<>(Person::getAddress),
				new AccessorByMethodReference<>(Address::getCity)))).isEqualTo("Person::getAddress > Address::getCity");
		assertThat(AccessorDefinition.toString(Arrays.asList(
				new AccessorByMethodReference<>(Person::getAddress),
				new AccessorByMethodReference<>(Address::getCity),
				Accessors.accessorByField(Person.class, "name")))).isEqualTo("Person::getAddress > Address::getCity > o.c.r.m.Person.name");
	}
	
	/**
	 * A class that inherits from {@link AccessorByMethod} but implements {@link AccessorDefinitionDefiner} to ensure
	 * that the latter is taken into account, not the mechanism implemented by {@link AccessorDefinition#giveDefinition(ValueAccessPoint)}
	 */
	private static class DefinedAccessorByMethod<C, T> extends AccessorByMethod<C, T> implements AccessorDefinitionDefiner<C> {
		
		private final AccessorDefinition accessorDefinition;
		
		public DefinedAccessorByMethod(Method getter, AccessorDefinition accessorDefinition) {
			super(getter);
			this.accessorDefinition = accessorDefinition;
		}
		
		@Override
		public AccessorDefinition asAccessorDefinition() {
			return accessorDefinition;
		}
	}
	
}