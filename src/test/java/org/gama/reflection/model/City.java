package org.gama.reflection.model;

/**
 * @author Guillaume Mary
 */
public class City {
	
	private String name;
	
	private int citizenCount;
	
	private boolean capital;
	
	public City() {
	}
	
	public City(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String name() {
		return this.name;
	}
	
	public void setCitizenCount(int citizenCount) {
		this.citizenCount = citizenCount;
	}
	
	public boolean isCapital() {
		return capital;
	}
	
	public void setCapital(boolean capital) {
		this.capital = capital;
	}
}
