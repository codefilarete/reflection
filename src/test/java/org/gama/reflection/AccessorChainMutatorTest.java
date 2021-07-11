package org.gama.reflection;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.gama.lang.Reflections;
import org.gama.lang.Reflections.MemberNotFoundException;
import org.gama.lang.collection.Arrays;
import org.gama.reflection.AccessorChainMutator.AccessorPathBuilder;
import org.gama.reflection.model.Address;
import org.gama.reflection.model.City;
import org.gama.reflection.model.Person;
import org.gama.reflection.model.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Guillaume Mary
 */
class AccessorChainMutatorTest {

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

		private DataSet() {
			cityNameAccessor = Accessors.accessorByField(City.class, "name");
			addressCityAccessor = Accessors.accessorByField(Address.class, "city");
			personAddressAccessor = Accessors.accessorByField(Person.class, "address");
			addressPhonesAccessor = Accessors.accessorByField(Address.class, "phones");
			phoneListAccessor = new ListAccessor<>(2);
			phoneNumberAccessor = Accessors.accessorByField(Phone.class, "number");
			phoneNumberMethodAccessor = Accessors.accessorByMethod(Phone.class, "number");
			charAtAccessor = new AccessorByMethod<>(Reflections.findMethod(String.class, "charAt", int.class));
			toCharArrayAccessor = new AccessorByMethod<>(Reflections.findMethod(String.class, "toCharArray"));
			charArrayAccessor = new ArrayAccessor<>(2);

			cityNameMutator = Accessors.mutatorByField(City.class, "name");
			addressCityMutator = Accessors.mutatorByField(Address.class, "city");
			personAddressMutator = Accessors.mutatorByField(Person.class, "address");
			addressPhonesMutator = Accessors.mutatorByField(Address.class, "phones");
			phoneListMutator = new ListMutator<>(2);
			phoneNumberMutator = Accessors.mutatorByField(Phone.class, "number");
			phoneNumberMethodMutator = Accessors.mutatorByMethod(Phone.class, "number");
			charAtMutator = new MutatorByMethod<>(Reflections.findMethod(String.class, "charAt", int.class));
			toCharArrayMutator = new MutatorByMethod<>(Reflections.findMethod(String.class, "toCharArray"));
			charArrayMutator = new ArrayMutator<>(2);
		}
	}

	static Object[][] testGetMutatorData() {
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

	static Object[][] testGetMutator_exception_data() {
		DataSet dataSet = new DataSet();
		return new Object[][]{
				{ dataSet.charAtAccessor },    // chartAt() has no mutator equivalent
				{ dataSet.toCharArrayAccessor },    // toCharArray() has no mutator equivalent
		};
	}

	@ParameterizedTest
	@MethodSource("testGetMutatorData")
	void testGetMutator(IReversibleAccessor accessor, IMutator expected) {
		assertThat(accessor.toMutator()).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("testGetMutator_exception_data")
	void testGetMutator_exception(IReversibleAccessor accessor) {
		assertThatExceptionOfType(MemberNotFoundException.class).isThrownBy(accessor::toMutator);
	}

	static List<IAccessor> list(IAccessor ... accessors) {
		return Arrays.asList(accessors);
	}

	static Object[][] setData() {
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
	@MethodSource("setData")
	void set(List<IAccessor> accessors, Object object, Object expected) {
		AccessorChain<Object, Object> accessorChain = new AccessorChain<>(accessors);
		AccessorChainMutator testInstance = accessorChain.toMutator();
		testInstance.set(object, expected);
		assertThat(accessorChain.get(object)).isEqualTo(expected);
	}

	@Test
	void set_nullValueOnPath_throwsNullPointerException() {
		DataSet dataSet = new DataSet();
		List<IAccessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor);
		Object object = new Person(null);
		AccessorChainMutator testInstance = new AccessorChain(accessors).toMutator();
		assertThatThrownBy(() -> testInstance.set(object, new Address(new City("Toto"), null)))
		        .isInstanceOf(RuntimeException.class)
				.hasMessage("Error while applying [accessor for field o.g.r.m.Person.address] <- mutator for field o.g.r.m.Address.phones on instance of o.g.r.m.Person")
                .hasCauseInstanceOf(NullPointerException.class)
				.extracting(Throwable::getCause, InstanceOfAssertFactories.THROWABLE)
                .hasMessage("Cannot invoke [accessor for field o.g.r.m.Person.address] on null instance");
	}

	@Test
	void set_nullValueOnPath_withInitializer_objectsAreInstanciated() {
		DataSet dataSet = new DataSet();
		IMutator<List<Phone>, Phone> phoneAdder = List::add;
		List<IAccessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor);
		// We create a Person without address, it will be instanciated by AccessorChainMutator
		Person targetPerson = new Person(null);
		AccessorChainMutator<Person, List<Phone>, Phone> testInstance = new AccessorChainMutator<>(accessors, phoneAdder);
		testInstance.setNullValueHandler(AccessorChain.INITIALIZE_VALUE);
		Phone phone = new Phone("123");
		testInstance.set(targetPerson, phone);
		assertThat(targetPerson.getAddress().getPhones()).isEqualTo(Arrays.asList(phone));
	}

	@Test
	void set_nullValueOnPath_nullHandler() {
		DataSet dataSet = new DataSet();
		List<IAccessor> accessors = list(dataSet.personAddressAccessor, dataSet.addressPhonesAccessor, dataSet.phoneListAccessor);
		Person person = new Person(new Address(null, null));
		AccessorChainMutator<Person, Phone, String> testInstance = new AccessorChainMutator<>(accessors, dataSet.phoneNumberMethodMutator);
		testInstance.setNullValueHandler(AccessorChain.RETURN_NULL);
		assertThatThrownBy(() -> testInstance.set(person, "123"))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Call of address.phones.get(2) on " + person + " returned null, because address.phones returned null");
	}

	static Object[][] testPathDescription() {
		DataSet dataSet = new DataSet();
		return new Object[][] {
				{ list(
						dataSet.personAddressAccessor,
						dataSet.addressPhonesAccessor,
						dataSet.phoneListAccessor,
						dataSet.phoneNumberMethodAccessor,
						dataSet.charArrayAccessor),
						"address.phones.get(2).getNumber().[2]" },
				{ list(
						dataSet.personAddressAccessor,
						dataSet.addressPhonesAccessor,
						dataSet.phoneListAccessor,
						new AccessorByMethodReference<>(Phone::getNumber),
						dataSet.charArrayAccessor),
						"address.phones.get(2).getNumber().[2]" },
				{ list(
						dataSet.personAddressAccessor,
						dataSet.addressPhonesAccessor,
						dataSet.phoneListAccessor,
						new AccessorByMethodReference<>(Phone::getNumber),
						new AccessorByMethod(Reflections.getMethod(String.class, "substring", int.class, int.class))),
						"address.phones.get(2).getNumber().substring(..)" },
		};
	}

	@ParameterizedTest
	@MethodSource("testPathDescription")
	void testPathDescription(List<IAccessor> accessors, String expectedResult) {
		AccessorPathBuilder testInstance = new AccessorPathBuilder();
		assertThat(testInstance.ccat(accessors, ".").toString()).isEqualTo(expectedResult);
	}
}