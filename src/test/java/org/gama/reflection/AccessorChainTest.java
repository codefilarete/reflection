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

import static org.gama.reflection.Accessors.accessorByMethodReference;
import static org.gama.reflection.Accessors.propertyAccessor;
import static org.gama.reflection.Accessors.mutatorByField;
import static org.gama.reflection.Accessors.mutatorByMethod;
import static org.gama.reflection.Accessors.mutatorByMethodReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Guillaume Mary
 */
public class AccessorChainTest {
	
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
		
		private DataSet() {
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
		}
	}
	
	public static List<IAccessor> list(IAccessor ... accessors) {
		return Arrays.asList(accessors);
	}
	
	public static Object[][] testGetData() {
		DataSet dataSet = new DataSet();
		return new Object[][] {
				{ list(dataSet.cityNameAccessor),
						new City("Toto"), "Toto" },
				{ list(dataSet.addressCityAccessor, dataSet.cityNameAccessor),
						new Address(new City("Toto"), null), "Toto" },
				{ list(dataSet.personAddressAccessor, dataSet.addressCityAccessor, dataSet.cityNameAccessor),
						new Person(new Address(new City("Toto"), null)), "Toto" },
				{ list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneListAccessor, dataSet.phoneNumberAccessor),
						new Person(new Address(null, Arrays.asList(new Phone("123"), new Phone("456"), new Phone("789")))), "789" },
				{ list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneListAccessor, dataSet.phoneNumberMethodAccessor),
						new Person(new Address(null, Arrays.asList(new Phone("123"), new Phone("456"), new Phone("789")))), "789" },
				{ list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneListAccessor, dataSet.phoneNumberMethodAccessor, dataSet.charAtAccessor.setParameters(2)),
						new Person(new Address(null, Arrays.asList(new Phone("123"), new Phone("456"), new Phone("789")))), '9' },
				{ list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneListAccessor, dataSet.phoneNumberMethodAccessor, dataSet.toCharArrayAccessor, dataSet.charArrayAccessor),
						new Person(new Address(null, Arrays.asList(new Phone("123"), new Phone("456"), new Phone("789")))), '9' },
				{ list(dataSet.toCharArrayAccessor, dataSet.charArrayAccessor),
						"123", '3' },
		};
	}
	
	@ParameterizedTest
	@MethodSource("testGetData")
	public void testGet(List<IAccessor> accessors, Object object, Object expected) {
		AccessorChain<Object, Object> accessorChain = new AccessorChain<>(accessors);
		assertEquals(expected, accessorChain.get(object));
	}
	
	@Test
	public void testGet_accessorIsAWrongOne_throwsIllegalArgumentException() {
		// field "number" doesn't exist on Collection "phones" => get(..) should throw exception
		DataSet dataSet = new DataSet();
		List<IAccessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneNumberAccessor);
		Object object = new Person(new Address(null, Arrays.asList(new Phone("123"))));
		AccessorChain<Object, Object > testInstance = new AccessorChain<>(accessors);
		RuntimeException thrownException = assertThrows(RuntimeException.class, () -> testInstance.get(object));
		assertEquals("Error while applying [accessor for field o.g.r.m.Person.address, accessor for field o.g.r.m.Address.phones," 
				+ " accessor for field o.g.r.m.Phone.number] on instance of o.g.r.m.Person", thrownException.getMessage());
		assertEquals("Error while applying accessor for field o.g.r.m.Phone.number on instance of j.u.ArrayList", thrownException.getCause().getMessage());
		assertEquals("Field o.g.r.m.Phone.number doesn't exist in j.u.ArrayList", thrownException.getCause().getCause().getMessage());
	}
	
	@Test
	public void testGet_nullValueOnPath_defaultNullHandler_throwsNullPointerException() {
		DataSet dataSet = new DataSet();
		List<IAccessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneNumberAccessor);
		Object object = new Person(new Address(null, null));
		AccessorChain<Object, Object > testInstance = new AccessorChain<>(accessors);
		RuntimeException thrownException = assertThrows(RuntimeException.class, () -> testInstance.get(object));
		assertEquals("Error while applying [accessor for field o.g.r.m.Person.address, accessor for field o.g.r.m.Address.phones," 
				+ " accessor for field o.g.r.m.Phone.number] on instance of o.g.r.m.Person", thrownException.getMessage());
		assertTrue(thrownException.getCause() instanceof NullPointerException);
		assertEquals("Cannot invoke [accessor for field o.g.r.m.Person.address, accessor for field o.g.r.m.Address.phones," 
				+ " accessor for field o.g.r.m.Phone.number] on null instance", thrownException.getCause().getMessage());
	}
	
	@Test
	public void testGet_nullValueOnPath_nullHandler() {
		DataSet dataSet = new DataSet();
		List<IAccessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneNumberAccessor);
		Object object = new Person(new Address(null, null));
		AccessorChain<Object, Object > testInstance = new AccessorChain<>(accessors);
		testInstance.setNullValueHandler(AccessorChain.RETURN_NULL);
		assertNull(testInstance.get(object));
	}
}