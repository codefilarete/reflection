package org.codefilarete.reflection;

import org.assertj.core.api.Assertions;
import org.codefilarete.reflection.model.City;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Reflections.MemberNotFoundException;
import org.codefilarete.tool.collection.Arrays;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Guillaume Mary
 */
class AccessorsTest {
	
	@Test
	void giveInputType() {
		assertThat(Accessors.giveInputType(Accessors.propertyAccessor(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(Accessors.propertyAccessor(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(Accessors.mutatorByField(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(Accessors.mutatorByField(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(Accessors.mutatorByMethod(City.class, "citizenCount", int.class))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(Accessors.mutatorByMethod(City.class, "capital", boolean.class))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(Accessors.mutatorByMethodReference(City::setCitizenCount))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(Accessors.mutatorByMethodReference(City::setCapital))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(new AccessorChainMutator<>(Arrays.asList(Object::toString), Accessors.mutatorByMethodReference(String::contains)))).isEqualTo(CharSequence.class);
		assertThat(Accessors.giveInputType(new DefaultReadWriteAccessPoint<>(Accessors.accessorByMethodReference(City::getName), Accessors.mutatorByMethodReference(City::setName)))).isEqualTo(String.class);
	}
	
	@Test
	void giveReturnType() {
		assertThat(Accessors.giveReturnType(Accessors.propertyAccessor(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveReturnType(Accessors.propertyAccessor(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(Accessors.accessorByField(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveReturnType(Accessors.accessorByField(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(Accessors.accessorByMethod(City.class, "name"))).isEqualTo(String.class);
		assertThat(Accessors.giveReturnType(Accessors.accessorByMethod(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(Accessors.accessorByMethodReference(City::getName))).isEqualTo(String.class);
		assertThat(Accessors.giveReturnType(Accessors.accessorByMethodReference(City::isCapital))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(new AccessorChain<>(Accessors.accessorByMethodReference(City::isCapital), Accessors.accessorByMethodReference(Object::toString)))).isEqualTo(String.class);
		assertThat(Accessors.giveReturnType(new DefaultReadWriteAccessPoint<>(Accessors.accessorByMethodReference(City::getName), Accessors.mutatorByMethodReference(City::setName)))).isEqualTo(String.class);
	}
	
	@Test
	void mutator() throws NoSuchFieldException {
		Field propertyNoSetter = Toto.class.getDeclaredField("propertyNoSetter");
		assertThat(Accessors.mutator(Toto.class, "propertyNoSetter").getSetter()).isEqualTo(propertyNoSetter);
		// testing compatibility look up
		assertThat(Accessors.mutator(Toto.class, "propertyNoSetter", StringBuilder.class).getSetter()).isEqualTo(propertyNoSetter);
		assertThat(Accessors.mutator(Toto.class, "propertyNoSetter", CharSequence.class).getSetter()).isEqualTo(propertyNoSetter);
		
		assertThatThrownBy(() -> Accessors.mutator(Toto.class, "propertyNoSetter", String.class).getSetter())
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("Member type doesn't match expected one for field o.c.r.AccessorsTest$Toto.propertyNoSetter:"
						+ " expected j.l.String but is j.l.StringBuilder");
	}
	
	@Test
	void mutator_noSetter_fieldIsCompatible() {
		assertThat(Accessors.mutator(Toto.class, "propertyForBoxing", Long.class).getSetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForBoxing"));
		assertThat(Accessors.mutator(Toto.class, "propertyForTypeCompatibility", CharSequence.class).getSetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForTypeCompatibility"));
	}
	
	@Test
	void readWriteAccessPoint_accessorAsMethodReference() {
		ReadWritePropertyAccessPoint<Toto, StringBuilder> testInstance = Accessors.readWriteAccessPoint(Toto::getProperty);
		assertThat(testInstance.getReader().getClass()).isEqualTo(AccessorByMethodReference.class);
		assertThat(((ValueAccessPointByMethod<Toto>) testInstance.getWriter()).getMethod()).isEqualTo(Reflections.getMethod(Toto.class, "setProperty", StringBuilder.class));
	}
	
	@Test
	void readWriteAccessPoint_mutatorAsMethodReference() {
		ReadWritePropertyAccessPoint<Toto, StringBuilder> testInstance = Accessors.readWriteAccessPoint(Toto::setProperty);
		assertThat(testInstance.getWriter().getClass()).isEqualTo(MutatorByMethodReference.class);
		assertThat(((ValueAccessPointByMethod<Toto>) testInstance.getReader()).getMethod()).isEqualTo(Reflections.getMethod(Toto.class, "getProperty"));
		
		ReadWritePropertyAccessPoint<Toto, String> testInstance2 = Accessors.readWriteAccessPoint(Toto::setPropertyWithoutField);
		assertThat(testInstance2.getWriter().getClass()).isEqualTo(MutatorByMethodReference.class);
		assertThat(((ValueAccessPointByMethod<Toto>) testInstance2.getReader()).getMethod()).isEqualTo(Reflections.getMethod(Toto.class, "getPropertyWithoutField"));
		
	}
	
	@Nested
	class WithoutField {
		
		@Test
		void accessor() {
			assertThat(Accessors.readWriteAccessPoint(Toto::getPropertyWithoutField).getReader().getClass()).isEqualTo(AccessorByMethodReference.class);
		}
		
		@Test
		void mutator() {
			assertThat(Accessors.readWriteAccessPoint(Toto::setPropertyWithoutField).getWriter().getClass()).isEqualTo(MutatorByMethodReference.class);
		}
	}
	
	@Test
	void accessor() {
		Method getProperty = Reflections.getMethod(Toto.class, "getProperty");
		// testing compatibility look up
		assertThat(Accessors.accessor(Toto.class, "property", StringBuilder.class).getGetter()).isEqualTo(getProperty);
		assertThat(Accessors.accessor(Toto.class, "property", CharSequence.class).getGetter()).isEqualTo(getProperty);

		assertThatThrownBy(() -> Accessors.mutator(Toto.class, "property", String.class).getSetter())
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("Member type doesn't match expected one for field o.c.r.AccessorsTest$Toto.property:"
						+ " expected j.l.String but is j.l.StringBuilder");
	}
	
	@Test
	void accessor_noGetter_fieldIsCompatible() {
		assertThat(Accessors.accessor(Toto.class, "propertyForBoxing", Long.class).getGetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForBoxing"));
		assertThat(Accessors.accessor(Toto.class, "propertyForTypeCompatibility", CharSequence.class).getGetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForTypeCompatibility"));
	}
		
	@Test
	void mutatorByMethod_setterExists_wrapsJDKMethod() throws NoSuchMethodException {
		Assertions.assertThat(Accessors.mutatorByMethod(Toto.class, "property").getMethod()).isEqualTo(Toto.class.getMethod("setProperty", StringBuilder.class));
	}
	
	@Test
	void mutatorByMethod_noSetter() {
		Assertions.assertThat(Accessors.mutatorByMethod(Toto.class, "propertyNoSetter")).isNull();
	}
	
	@Test
	void mutatorByMethod_noMatchingField() {
		assertThatThrownBy(() -> Accessors.mutatorByMethod(Toto.class, "noMatchingField"))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("Field noMatchingField on o.c.r.AccessorsTest$Toto was not found");
	}
	
	protected static class Toto {
		
		private StringBuilder property;
		
		private StringBuilder propertyNoSetter;
		
		private long propertyForBoxing;
		
		private String propertyForTypeCompatibility;
		
		public StringBuilder getProperty() {
			return property;
		}
		
		public void setProperty(StringBuilder property) {
			this.property = property;
		}
		
		public void setPropertyWithoutField(String value) {
		
		}
		
		public String getPropertyWithoutField() {
            return null;
        }
		
		public StringBuilder getPropertyNoSetter() {
			return propertyNoSetter;
		}
		
		public Long getNoMatchingField() {
			return null;
		}
	}
}