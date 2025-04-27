package org.codefilarete.reflection.model;

import java.util.List;

/**
 * @author Guillaume Mary
 */
public class Address {
	
	private City city;
	
	private List<Phone> phones;
	
	public Address() {
		// we don't initialize this.phones because it is used during MutatorChain testing and a policy of initialization of null values
	}
	
	public Address(City city, List<Phone> phones) {
		this.city = city;
		this.phones = phones;
	}
	
	public List<Phone> getPhones() {
		return phones;
	}
	
	public City getCity() {
		return city;
	}
	
	public void setCity(City city) {
		this.city = city;
	}
}
