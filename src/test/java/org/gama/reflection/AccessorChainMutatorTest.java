package org.gama.reflection;

import java.util.List;

import org.gama.lang.Reflections;
import org.gama.lang.collection.Arrays;
import org.gama.reflection.model.Address;
import org.gama.reflection.model.City;
import org.gama.reflection.model.Person;
import org.gama.reflection.model.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
public class AccessorChainMutatorTest {
	
	private static class DataSet {
		private final AccessorByField<City, String> cityNameAccessor;
		private final AccessorByField<Address, City> addressCityAccessor;
		private final AccessorByField<Person, Address> personAddressAccessor;
		private final AccessorByField<Address, List> addressPhonesAccessor;
		private final AccessorByMethod<? extends List, Phone> phoneListAccessor;
		private final AccessorByField<Phone, String> phoneNumberAccessor;
		private final AccessorByMethod<Phone, String> phoneNumberMethodAccessor;
		private final AccessorByMethod<String, Character> charAtAccessor;
		private final AccessorByMethod<String, Character[]> toCharArrayAccessor;
		private final ArrayAccessor<String> charArrayAccessor;
		
		private final MutatorByField<City, String> cityNameMutator;
		private final MutatorByField<Address, City> addressCityMutator;
		private final MutatorByField<Person, Address> personAddressMutator;
		private final MutatorByField<Address, List> addressPhonesMutator;
		private final MutatorByMethod<? extends List, Phone> phoneListMutator;
		private final MutatorByField<Phone, String> phoneNumberMutator;
		private final MutatorByMethod<Phone, String> phoneNumberMethodMutator;
		private final MutatorByMethod<String, Character> charAtMutator;
		private final MutatorByMethod<String, Character[]> toCharArrayMutator;
		private final ArrayMutator<String> charArrayMutator;
		
		private DataSet() throws NoSuchFieldException {
			cityNameAccessor = Accessors.accessorByField(City.class, "name");
			addressCityAccessor = Accessors.accessorByField(Address.class, "city");
			personAddressAccessor = Accessors.accessorByField(Person.class, "address");
			addressPhonesAccessor = Accessors.accessorByField(Address.class, "phones");
			phoneListAccessor = new ListAccessor<>(2);
			phoneNumberAccessor = Accessors.accessorByField(Phone.class, "number");
			phoneNumberMethodAccessor = Accessors.accessorByMethod(Phone.class, "number");
			charAtAccessor = new AccessorByMethod<>(Reflections.findMethod(String.class, "charAt", Integer.TYPE));
			toCharArrayAccessor = new AccessorByMethod<>(Reflections.findMethod(String.class, "toCharArray"));
			charArrayAccessor = new ArrayAccessor<>(2);
			
			cityNameMutator = Accessors.mutatorByField(City.class, "name");
			addressCityMutator = Accessors.mutatorByField(Address.class, "city");
			personAddressMutator = Accessors.mutatorByField(Person.class, "address");
			addressPhonesMutator = Accessors.mutatorByField(Address.class, "phones");
			phoneListMutator = new ListMutator<>(2);
			phoneNumberMutator = Accessors.mutatorByField(Phone.class, "number");
			phoneNumberMethodMutator = Accessors.mutatorByMethod(Phone.class, "number");
			charAtMutator = new MutatorByMethod<>(Reflections.findMethod(String.class, "charAt", Integer.TYPE));
			toCharArrayMutator = new MutatorByMethod<>(Reflections.findMethod(String.class, "toCharArray"));
			charArrayMutator = new ArrayMutator<>(2);
		}
	}
	
	public static Object[][] testGetMutatorData() throws NoSuchFieldException {
		DataSet dataSet = new DataSet();
		return new Object[][]{
				{ dataSet.cityNameAccessor, dataSet.cityNameMutator },
				{ dataSet.addressCityAccessor, dataSet.addressCityMutator },
				{ dataSet.personAddressAccessor, dataSet.personAddressMutator },
				{ dataSet.addressPhonesAccessor, dataSet.addressPhonesMutator },
				{ dataSet.phoneListAccessor, dataSet.phoneListMutator },
				{ dataSet.phoneNumberAccessor, dataSet.phoneNumberMutator },
				{ dataSet.phoneNumberMethodAccessor, dataSet.phoneNumberMutator },
				{ dataSet.charArrayAccessor, dataSet.charArrayMutator }
			
		};
	}
	
	public static Object[][] testGetMutator_exception_data() throws NoSuchFieldException {
		DataSet dataSet = new DataSet();
		return new Object[][]{
				{ dataSet.charAtAccessor },    // chartAt() has no mutator equivalent
				{ dataSet.toCharArrayAccessor },    // toCharArray() has no mutator equivalent
		};
	}
	
	@ParameterizedTest
	@MethodSource("testGetMutatorData")
	public void testGetMutator(IReversibleAccessor accessor, IMutator expected) {
		assertEquals(expected, accessor.toMutator());
	}
	
	@ParameterizedTest
	@MethodSource("testGetMutator_exception_data")
	public void testGetMutator_exception(IReversibleAccessor accessor) {
		assertThrows(IllegalArgumentException.class, accessor::toMutator);
	}
	
	public static List<IAccessor> list(IAccessor ... accessors) {
		return Arrays.asList(accessors);
	}
	
	public static Object[][] testSetData() throws NoSuchFieldException {
		DataSet dataSet = new DataSet();
		return new Object[][] {
				{ list(dataSet.cityNameAccessor),
						new City("Toto"), "Tata" },
				{ list(dataSet.addressCityAccessor, dataSet.cityNameAccessor),
						new Address(new City("Toto"), null), "Tata" },
				{ list(dataSet.personAddressAccessor, dataSet.addressCityAccessor, dataSet.cityNameAccessor),
						new Person(new Address(new City("Toto"), null)), "Tata" },
				{ list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneListAccessor, dataSet.phoneNumberAccessor),
						new Person(new Address(null, Arrays.asList(new Phone("123"), new Phone("456"), new Phone("789")))), "000" },
				{ list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneListAccessor, dataSet.phoneNumberMethodAccessor),
						new Person(new Address(null, Arrays.asList(new Phone("123"), new Phone("456"), new Phone("789")))), "000" },
				{ list(dataSet.charArrayAccessor),
						new char[] { '1', '2', '3' }, '0' },
		};
	}
	
	@ParameterizedTest
	@MethodSource("testSetData")
	public void testSet(List<IAccessor> accessors, Object object, Object expected) {
		AccessorChain<Object, Object> accessorChain = new AccessorChain<>(accessors);
		AccessorChainMutator testInstance = accessorChain.toMutator();
		testInstance.set(object, expected);
		assertEquals(expected, accessorChain.get(object));
	}
	
	@Test
	public void testSet_NullPointerException() throws NoSuchFieldException {
		DataSet dataSet = new DataSet();
		List<IAccessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor);
		Object object = new Person(null);
		AccessorChainMutator testInstance = new AccessorChain(accessors).toMutator();
		assertThrows(NullPointerException.class, () -> testInstance.set(object, new Address(new City("Toto"), null)));
	}
	
}