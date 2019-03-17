package org.gama.reflection;

import org.gama.reflection.model.City;
import org.gama.reflection.model.Person;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.gama.reflection.Accessors.accessorByField;
import static org.gama.reflection.Accessors.accessorByMethod;
import static org.gama.reflection.Accessors.mutatorByField;
import static org.gama.reflection.Accessors.mutatorByMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
class ValueAccessPointComparatorTest {
	
	static Object[][] testValueAccessPointComparator() {
		MutatorByMethodReference<Person, String> ctMutatorByMethodReference = new MutatorByMethodReference<>(Person::setLastName);
		AccessorByMethodReference<Person, String> ctAccessorByMethodReference = new AccessorByMethodReference<>(Person::getLastName);
		PropertyAccessor<Person, String> propertyAccessor = new PropertyAccessor<>(ctAccessorByMethodReference, ctMutatorByMethodReference);
		return new Object[][] {
				// ACCESSOR vs ACCESSOR
				// field vs method
				{ accessorByField(Person.class, "name"), accessorByField(Person.class, "name"), true },
				{ accessorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getName"), true },
				{ accessorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getName), true },
				
				// field vs method with same owning type 
				{ accessorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getLastName"), false },
				{ accessorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getLastName), false },
				
				// members with same name but different owner
				{ accessorByField(Person.class, "name"), accessorByField(City.class, "name"), false },
				{ accessorByField(Person.class, "name"), new AccessorByMethod<>(City.class, "getName"), false },
				{ accessorByField(Person.class, "name"), new AccessorByMethodReference<>(City::getName), false },
				
				// MUTATOR vs MUTATOR
				// field vs method
				{ mutatorByField(Person.class, "name"), mutatorByField(Person.class, "name"), true },
				{ mutatorByField(Person.class, "name"), new MutatorByMethod<>(Person.class, "setName", String.class), true },
				{ mutatorByField(Person.class, "name"), new MutatorByMethodReference<>(Person::setName), true },
				
				// field vs method with same owning type 
				{ mutatorByField(Person.class, "name"), new MutatorByMethod<>(Person.class, "setLastName", String.class), false },
				{ mutatorByField(Person.class, "name"), new MutatorByMethodReference<>(Person::setLastName), false },
				
				// members with same name but different owner
				{ mutatorByField(Person.class, "name"), mutatorByField(City.class, "name"), false },
				{ mutatorByField(Person.class, "name"), new MutatorByMethod<>(City.class, "setName", String.class), false },
				{ mutatorByField(Person.class, "name"), new MutatorByMethodReference<>(City::setName), false },
				
				// MUTATOR vs ACCESSOR
				{ mutatorByField(Person.class, "name"), accessorByField(Person.class, "name"), true },
				{ mutatorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getName"), true },
				{ mutatorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getName), true },
				
				{ mutatorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getLastName"), false },
				{ mutatorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getLastName), false },
				
				{ accessorByMethod(Person.class, "lastName"), new AccessorByMethodReference<>(Person::getLastName), true},
				{ mutatorByMethod(Person.class, "lastName"), new AccessorByMethodReference<>(Person::getLastName), true},
				{ accessorByMethod(Person.class, "lastName"), new MutatorByMethodReference<>(Person::setLastName), true},
				{ mutatorByMethod(Person.class, "lastName"), new MutatorByMethodReference<>(Person::setLastName), true},
				{ accessorByMethod(Person.class, "lastName"), propertyAccessor, true},
			
		};
	}
	
	@ParameterizedTest
	@MethodSource("testValueAccessPointComparator")
	void testValueAccessPointComparator(ValueAccessPoint accessor1, ValueAccessPoint accessor2, boolean expectedEquality) {
		
		ValueAccessPointSet testInstance = new ValueAccessPointSet();
		testInstance.add(accessor2);
		assertEquals(expectedEquality, testInstance.contains(accessor1));
	}
}