package org.gama.reflection;

import org.gama.lang.collection.Arrays;
import org.gama.reflection.model.Address;
import org.gama.reflection.model.City;
import org.gama.reflection.model.Person;
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
	
}