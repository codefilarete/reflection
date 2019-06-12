package org.gama.reflection;

import org.gama.lang.collection.Arrays;
import org.gama.reflection.model.Address;
import org.gama.reflection.model.City;
import org.gama.reflection.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.gama.reflection.Accessors.accessorByField;
import static org.gama.reflection.Accessors.mutatorByField;
import static org.gama.reflection.MemberDefinition.giveMemberDefinition;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
class MemberDefinitionTest {
	
	static Object[][] testGiveMemberDefinition() {
		return new Object[][] {
				// accessor
				{ accessorByField(Person.class, "name"), Person.class, "name", String.class },
				{ new AccessorByMethod<>(Person.class, "getName"), Person.class, "name", String.class },
				{ new AccessorByMethodReference<>(Person::getName), Person.class, "name", String.class },
				
				// mutator
				{ mutatorByField(Person.class, "name"), Person.class, "name", String.class },
				{ new MutatorByMethod<>(Person.class, "setName", String.class), Person.class, "name", String.class },
				{ new MutatorByMethodReference<>(Person::setName), Person.class, "name", String.class },
				
				{ new AccessorChain<>(Arrays.asList(new AccessorByMethodReference<>(Person::getAddress), new AccessorByMethodReference<>(Address::getCity))),
						Person.class, "address.city", City.class },
		};
	}
	
	@ParameterizedTest
	@MethodSource("testGiveMemberDefinition")
	void testGiveMemberDefinition(ValueAccessPoint o, Class expectedDeclaringClass, String expectedName, Class expectedMemberType) {
		MemberDefinition memberDefinition = giveMemberDefinition(o);
		assertEquals(expectedDeclaringClass, memberDefinition.getDeclaringClass());
		assertEquals(expectedName, memberDefinition.getName());
		assertEquals(expectedMemberType, memberDefinition.getMemberType());
	}
	
	static Object[][] testToString() {
		return new Object[][] {
				{ accessorByField(Person.class, "name"), "o.g.r.m.Person.name" },
				{ new AccessorByMethod<>(Person.class, "getName"), "o.g.r.m.Person.getName()" },
				{ new AccessorByMethodReference<>(Person::getName), "Person::getName" },
				
				// mutator
				{ mutatorByField(Person.class, "name"), "o.g.r.m.Person.name" },
				{ new MutatorByMethod<>(Person.class, "setName", String.class), "o.g.r.m.Person.setName(j.l.String)" },
				{ new MutatorByMethodReference<>(Person::setName), "Person::setName" },
				
				{ new AccessorChain<>(Arrays.asList(new AccessorByMethodReference<>(Person::getAddress), new AccessorByMethodReference<>(Address::getCity))),
						"Person::getAddress > Address::getCity" },
				
				{ new PropertyAccessor<>(new AccessorByMethodReference<>(Person::getName), new MutatorByMethodReference<>(Person::setName)), "Person::getName" },
				{ new PropertyAccessor<>(new AccessorByMethod<>(Person.class, "getName"), new MutatorByMethod<>(Person.class, "setName", String.class)),
						"o.g.r.m.Person.getName()" },
				{ null, "null" }
		};
	}
	
	@ParameterizedTest
	@MethodSource("testToString")
	void toString(ValueAccessPoint valueAccessPoint, String expectedResult) {
		assertEquals(expectedResult, MemberDefinition.toString(valueAccessPoint));
	}
	
	@Test
	void toString_collection() {
		assertEquals("Person::getAddress > Address::getCity", MemberDefinition.toString(Arrays.asList(
				new AccessorByMethodReference<>(Person::getAddress),
				new AccessorByMethodReference<>(Address::getCity))));
		assertEquals("Person::getAddress > Address::getCity > o.g.r.m.Person.name", MemberDefinition.toString(Arrays.asList(
				new AccessorByMethodReference<>(Person::getAddress),
				new AccessorByMethodReference<>(Address::getCity),
				accessorByField(Person.class, "name"))));
	}
	
}