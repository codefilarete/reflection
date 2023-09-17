package org.codefilarete.reflection;

import org.codefilarete.reflection.model.City;
import org.codefilarete.reflection.model.IPerson;
import org.codefilarete.reflection.model.Person;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Mary
 */
class ValueAccessPointComparatorTest {
	
	static Object[][] compare() {
		MutatorByMethodReference<Person, String> ctMutatorByMethodReference = new MutatorByMethodReference<>(Person::setLastName);
		AccessorByMethodReference<Person, String> ctAccessorByMethodReference = new AccessorByMethodReference<>(Person::getLastName);
		PropertyAccessor<Person, String> propertyAccessor = new PropertyAccessor<>(ctAccessorByMethodReference, ctMutatorByMethodReference);
		return new Object[][] {
				// ACCESSOR vs ACCESSOR
				// field vs method
				{ Accessors.accessorByField(Person.class, "name"), Accessors.accessorByField(Person.class, "name"), true },
				{ Accessors.accessorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getName"), true },
				{ Accessors.accessorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getName), true },
				
				// field vs method with same owning type 
				{ Accessors.accessorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getLastName"), false },
				{ Accessors.accessorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getLastName), false },
				
				// members with same name but different owner
				{ Accessors.accessorByField(Person.class, "name"), Accessors.accessorByField(City.class, "name"), false },
				{ Accessors.accessorByField(Person.class, "name"), new AccessorByMethod<>(City.class, "getName"), false },
				{ Accessors.accessorByField(Person.class, "name"), new AccessorByMethodReference<>(City::getName), false },
				
				// MUTATOR vs MUTATOR
				// field vs method
				{ Accessors.mutatorByField(Person.class, "name"), Accessors.mutatorByField(Person.class, "name"), true },
				{ Accessors.mutatorByField(Person.class, "name"), new MutatorByMethod<>(Person.class, "setName", String.class), true },
				{ Accessors.mutatorByField(Person.class, "name"), new MutatorByMethodReference<>(Person::setName), true },
				
				// field vs method with same owning type 
				{ Accessors.mutatorByField(Person.class, "name"), new MutatorByMethod<>(Person.class, "setLastName", String.class), false },
				{ Accessors.mutatorByField(Person.class, "name"), new MutatorByMethodReference<>(Person::setLastName), false },
				
				// members with same name but different owner
				{ Accessors.mutatorByField(Person.class, "name"), Accessors.mutatorByField(City.class, "name"), false },
				{ Accessors.mutatorByField(Person.class, "name"), new MutatorByMethod<>(City.class, "setName", String.class), false },
				{ Accessors.mutatorByField(Person.class, "name"), new MutatorByMethodReference<>(City::setName), false },
				
				// MUTATOR vs ACCESSOR
				{ Accessors.mutatorByField(Person.class, "name"), Accessors.accessorByField(Person.class, "name"), true },
				{ Accessors.mutatorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getName"), true },
				{ Accessors.mutatorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getName), true },
				
				{ Accessors.mutatorByField(Person.class, "name"), new AccessorByMethod<>(Person.class, "getLastName"), false },
				{ Accessors.mutatorByField(Person.class, "name"), new AccessorByMethodReference<>(Person::getLastName), false },
				
				{ Accessors.accessorByMethod(Person.class, "lastName"), new AccessorByMethodReference<>(Person::getLastName), true},
				{ Accessors.mutatorByMethod(Person.class, "lastName"), new AccessorByMethodReference<>(Person::getLastName), true},
				{ Accessors.accessorByMethod(Person.class, "lastName"), new MutatorByMethodReference<>(Person::setLastName), true},
				{ Accessors.mutatorByMethod(Person.class, "lastName"), new MutatorByMethodReference<>(Person::setLastName), true},
				{ Accessors.accessorByMethod(Person.class, "lastName"), propertyAccessor, true},
				
				{ new AccessorByMethodReference<>(IPerson::getName), new AccessorByMethodReference<>(IPerson::getName), true},
			
		};
	}
	
	@ParameterizedTest
	@MethodSource("compare")
	<X> void compare(ValueAccessPoint<X> accessor1, ValueAccessPoint<X> accessor2, boolean expectedEquality) {
		
		ValueAccessPointSet<X> testInstance = new ValueAccessPointSet<>();
		testInstance.add(accessor2);
		assertThat(testInstance.contains(accessor1)).isEqualTo(expectedEquality);
	}
}