package org.gama.reflection;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.gama.lang.Reflections;
import org.gama.lang.collection.Arrays;
import org.gama.reflection.AccessorChain.ValueInitializerOnNullValue;
import org.gama.reflection.model.Address;
import org.gama.reflection.model.City;
import org.gama.reflection.model.Person;
import org.gama.reflection.model.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Guillaume Mary
 */
class AccessorChainTest {
	
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
	
	static List<Accessor> list(Accessor... accessors) {
		return Arrays.asList(accessors);
	}
	
	static Object[][] get_data() {
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
	@MethodSource("get_data")
	void get(List<Accessor> accessors, Object object, Object expected) {
		AccessorChain<Object, Object> accessorChain = new AccessorChain<>(accessors);
		assertThat(accessorChain.get(object)).isEqualTo(expected);
	}
	
	@Test
	void get_accessorIsAWrongOne_throwsIllegalArgumentException() {
		// field "number" doesn't exist on Collection "phones" => get(..) should throw exception
		DataSet dataSet = new DataSet();
		List<Accessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneNumberAccessor);
		Object object = new Person(new Address(null, Arrays.asList(new Phone("123"))));
		AccessorChain<Object, Object > testInstance = new AccessorChain<>(accessors);
		assertThatThrownBy(() -> testInstance.get(object))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying [accessor for field o.g.r.m.Person.address, accessor for field o.g.r.m.Address.phones,"
						+ " accessor for field o.g.r.m.Phone.number] on instance of o.g.r.m.Person")
				.extracting(Throwable::getCause, InstanceOfAssertFactories.THROWABLE)
				.hasMessage("Error while applying accessor for field o.g.r.m.Phone.number on instance of j.u.ArrayList")
				.extracting(Throwable::getCause, InstanceOfAssertFactories.THROWABLE)
				.hasMessage("Field o.g.r.m.Phone.number doesn't exist in j.u.ArrayList");
	}
	
	@Test
	void get_nullValueOnPath_defaultNullHandler_throwsNullPointerException() {
		DataSet dataSet = new DataSet();
		List<Accessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneNumberAccessor);
		Object object = new Person(new Address(null, null));
		AccessorChain<Object, Object > testInstance = new AccessorChain<>(accessors);
		assertThatThrownBy(() -> testInstance.get(object))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying [accessor for field o.g.r.m.Person.address, accessor for field o.g.r.m.Address.phones,"
						+ " accessor for field o.g.r.m.Phone.number] on instance of o.g.r.m.Person")
				.extracting(Throwable::getCause, InstanceOfAssertFactories.THROWABLE)
				.hasMessage("Cannot invoke [accessor for field o.g.r.m.Person.address, accessor for field o.g.r.m.Address.phones,"
						+ " accessor for field o.g.r.m.Phone.number] on null instance");
	}
	
	@Test
	void get_nullValueOnPath_nullHandler() {
		DataSet dataSet = new DataSet();
		List<Accessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneNumberAccessor);
		Object object = new Person(new Address(null, null));
		AccessorChain<Object, Object > testInstance = new AccessorChain<>(accessors);
		testInstance.setNullValueHandler(AccessorChain.RETURN_NULL);
		assertThat(testInstance.get(object)).isNull();
	}
	
	@Test
	void forModel_getWithSomeNullOnPath_returnsNull() {
		DataSet dataSet = new DataSet();
		AccessorChain<Object, Object> testInstance = AccessorChain.forModel(list(dataSet.personAddressAccessor,
				dataSet.addressCityAccessor, dataSet.cityNameAccessor), null);
		assertThat(testInstance.get(new Person(null))).isNull();
		assertThat(testInstance.get(new Person(new Address(null, null)))).isNull();
	}
	
	@Test
	void forModel_setWithSomeNullOnPath_instanciateBeansOnPath() {
		DataSet dataSet = new DataSet();
		AccessorChain<Object, Object> testInstance = AccessorChain.forModel(list(dataSet.personAddressAccessor,
				dataSet.addressCityAccessor, dataSet.cityNameAccessor), null);
		Person pawn = new Person(null);
		testInstance.toMutator().set(pawn, "toto");
		assertThat(pawn.getAddress().getCity().getName()).isEqualTo("toto");
	}
	
	@Test
	void forModel_setUsesValueTypeDeterminer() {
		DataSet dataSet = new DataSet();
		AccessorChain<Object, Object> testInstance = AccessorChain.forModel(list(dataSet.personAddressAccessor,
				dataSet.addressPhonesAccessor, new ListAccessor<>(0)), (accessor, valueType) -> {
			if (accessor == dataSet.addressPhonesAccessor) {
				return MyList.class;	// we return a special List that prevent IndexOutOfBoundsException
			} else {
				return ValueInitializerOnNullValue.giveValueType(accessor, valueType);
			}
		});
		Person pawn = new Person(null);
		Phone newPhone = new Phone();
		testInstance.toMutator().set(pawn, newPhone);
		assertThat(pawn.getAddress().getPhones()).isInstanceOf(MyList.class);
		assertThat(pawn.getAddress().getPhones().get(0)).isEqualTo(newPhone);
	}
	
	private static class MyList<E> extends ArrayList<E> {
		
		MyList() {
			super();
			add(null); // we add one element to allow ListAccessor(0) to set a value in this list, else a IndexOutOfBoundsException is thrown
		}
	}
}