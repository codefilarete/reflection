package org.gama.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.gama.lang.Reflections.MemberNotFoundException;
import org.gama.lang.StringAppender;
import org.gama.lang.collection.Arrays;
import org.gama.reflection.model.City;
import org.junit.jupiter.api.Test;

import static org.gama.reflection.Accessors.accessorByField;
import static org.gama.reflection.Accessors.accessorByMethod;
import static org.gama.reflection.Accessors.accessorByMethodReference;
import static org.gama.reflection.Accessors.mutatorByField;
import static org.gama.reflection.Accessors.mutatorByMethod;
import static org.gama.reflection.Accessors.mutatorByMethodReference;
import static org.gama.reflection.Accessors.propertyAccessor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Mary
 */
class AccessorsTest {
	
	@Test
	void giveInputType() {
		assertEquals(int.class, Accessors.giveInputType(propertyAccessor(City.class, "citizenCount")));
		assertEquals(boolean.class, Accessors.giveInputType(propertyAccessor(City.class, "capital")));
		assertEquals(int.class, Accessors.giveInputType(mutatorByField(City.class, "citizenCount")));
		assertEquals(boolean.class, Accessors.giveInputType(mutatorByField(City.class, "capital")));
		assertEquals(int.class, Accessors.giveInputType(mutatorByMethod(City.class, "citizenCount", int.class)));
		assertEquals(boolean.class, Accessors.giveInputType(mutatorByMethod(City.class, "capital", boolean.class)));
		assertEquals(int.class, Accessors.giveInputType(mutatorByMethodReference(City::setCitizenCount)));
		assertEquals(boolean.class, Accessors.giveInputType(mutatorByMethodReference(City::setCapital)));
		assertEquals(CharSequence.class, Accessors.giveInputType(new AccessorChainMutator<>(Arrays.asList(Object::toString), mutatorByMethodReference(String::contains))));
		assertEquals(String.class, Accessors.giveInputType(new PropertyAccessor(accessorByMethodReference(City::getName), mutatorByMethodReference(City::setName))));
	}
	
	@Test
	void giveReturnType() {
		assertEquals(int.class, Accessors.giveReturnType(propertyAccessor(City.class, "citizenCount")));
		assertEquals(boolean.class, Accessors.giveReturnType(propertyAccessor(City.class, "capital")));
		assertEquals(int.class, Accessors.giveReturnType(accessorByField(City.class, "citizenCount")));
		assertEquals(boolean.class, Accessors.giveReturnType(accessorByField(City.class, "capital")));
		assertEquals(String.class, Accessors.giveReturnType(accessorByMethod(City.class, "name")));
		assertEquals(boolean.class, Accessors.giveReturnType(accessorByMethod(City.class, "capital")));
		assertEquals(String.class, Accessors.giveReturnType(accessorByMethodReference(City::getName)));
		assertEquals(boolean.class, Accessors.giveReturnType(accessorByMethodReference(City::isCapital)));
		assertEquals(String.class, Accessors.giveReturnType(new AccessorChain<>(accessorByMethodReference(City::isCapital), accessorByMethodReference(Object::toString))));
		assertEquals(String.class, Accessors.giveReturnType(new PropertyAccessor(accessorByMethodReference(City::getName), mutatorByMethodReference(City::setName))));
	}
	
	@Test
	void mutator() throws NoSuchFieldException {
		Field appender = StringAppender.class.getDeclaredField("appender");
		assertEquals(appender, Accessors.mutator(StringAppender.class, "appender").getSetter());
		assertEquals(appender, Accessors.mutator(StringAppender.class, "appender", StringBuilder.class).getSetter());
		assertEquals(appender, Accessors.mutator(StringAppender.class, "appender", CharSequence.class).getSetter());
		
		MemberNotFoundException thrownException = assertThrows(MemberNotFoundException.class,
				() -> Accessors.mutator(StringAppender.class, "appender", String.class).getSetter());
		assertEquals("Member type doesn't match expected one for field o.g.l.StringAppender.appender:" 
				+ " expected j.l.String but is j.l.StringBuilder", thrownException.getMessage());
	}
	
	@Test
	void mutator_noSetter_fieldIsCompatible() {
		assertEquals(Reflections.findField(Toto.class, "propertyForBoxing"),
				Accessors.mutator(Toto.class, "propertyForBoxing", Long.class).getSetter());
		assertEquals(Reflections.findField(Toto.class, "propertyForTypeCompatibility"),
				Accessors.mutator(Toto.class, "propertyForTypeCompatibility", CharSequence.class).getSetter());
	}
	
	@Test
	void mutator_withMethodReferenceSetter() {
		assertEquals(MutatorByMethodReference.class, Accessors.mutator(Toto::setProperty).getMutator().getClass());
		assertEquals(Reflections.getMethod(Toto.class, "getProperty"), ((ValueAccessPointByMethod) Accessors.mutator(Toto::setProperty).getAccessor()).getMethod());
	}
	
	@Test
	void accessor() {
		Method appender = Reflections.getMethod(StringAppender.class, "getAppender");
		assertEquals(appender, Accessors.accessor(StringAppender.class, "appender", StringBuilder.class).getGetter());
		assertEquals(appender, Accessors.accessor(StringAppender.class, "appender", CharSequence.class).getGetter());
		
		MemberNotFoundException thrownException = assertThrows(MemberNotFoundException.class,
				() -> Accessors.mutator(StringAppender.class, "appender", String.class).getSetter());
		assertEquals("Member type doesn't match expected one for field o.g.l.StringAppender.appender:" 
				+ " expected j.l.String but is j.l.StringBuilder", thrownException.getMessage());
	}
	
	@Test
	void accessor_noGetter_fieldIsCompatible() {
		assertEquals(Reflections.findField(Toto.class, "propertyForBoxing"),
				Accessors.accessor(Toto.class, "propertyForBoxing", Long.class).getGetter());
		assertEquals(Reflections.findField(Toto.class, "propertyForTypeCompatibility"),
				Accessors.accessor(Toto.class, "propertyForTypeCompatibility", CharSequence.class).getGetter());
	}
		
	@Test
	void accessor_withMethodReferenceSetter() {
		assertEquals(AccessorByMethodReference.class, Accessors.accessor(Toto::getProperty).getAccessor().getClass());
		assertEquals(Reflections.getMethod(Toto.class, "setProperty", Long.class), ((ValueAccessPointByMethod) Accessors.accessor(Toto::getProperty).getMutator()).getMethod());
	}
	
	@Test
	void mutatorByMethod_noSetter() throws NoSuchMethodException {
		assertEquals(Toto.class.getMethod("setProperty", Long.class), mutatorByMethod(Toto.class, "property").getMethod());
	}
	
	@Test
	void mutatorByMethod_noMatchingField() {
		MemberNotFoundException throwException = assertThrows(MemberNotFoundException.class, () -> mutatorByMethod(Toto.class, "noMatchingField"));
		assertEquals("Method setNoMatchingField() on o.g.r.AccessorsTest$Toto was not found", throwException.getMessage());
	}
	
	protected static class Toto {
		
		private Long property;
		
		private long propertyForBoxing;
		
		private String propertyForTypeCompatibility;
		
		public Long getProperty() {
			return property;
		}
		
		public void setProperty(Long property) {
			this.property = property;
		}
		
		public Long getNoMatchingField() {
			return null;
		}
	}
}