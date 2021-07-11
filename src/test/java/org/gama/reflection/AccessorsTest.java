package org.gama.reflection;

import org.gama.lang.Reflections;
import org.gama.lang.Reflections.MemberNotFoundException;
import org.gama.lang.StringAppender;
import org.gama.lang.collection.Arrays;
import org.gama.reflection.model.City;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.gama.reflection.Accessors.*;

/**
 * @author Guillaume Mary
 */
class AccessorsTest {
	
	@Test
	void giveInputType() {
		assertThat(Accessors.giveInputType(propertyAccessor(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(propertyAccessor(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(mutatorByField(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(mutatorByField(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(mutatorByMethod(City.class, "citizenCount", int.class))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(mutatorByMethod(City.class, "capital", boolean.class))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(mutatorByMethodReference(City::setCitizenCount))).isEqualTo(int.class);
		assertThat(Accessors.giveInputType(mutatorByMethodReference(City::setCapital))).isEqualTo(boolean.class);
		assertThat(Accessors.giveInputType(new AccessorChainMutator<>(Arrays.asList(Object::toString), mutatorByMethodReference(String::contains)))).isEqualTo(CharSequence.class);
		assertThat(Accessors.giveInputType(new PropertyAccessor(accessorByMethodReference(City::getName), mutatorByMethodReference(City::setName)))).isEqualTo(String.class);
	}
	
	@Test
	void giveReturnType() {
		assertThat(Accessors.giveReturnType(propertyAccessor(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveReturnType(propertyAccessor(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(accessorByField(City.class, "citizenCount"))).isEqualTo(int.class);
		assertThat(Accessors.giveReturnType(accessorByField(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(accessorByMethod(City.class, "name"))).isEqualTo(String.class);
		assertThat(Accessors.giveReturnType(accessorByMethod(City.class, "capital"))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(accessorByMethodReference(City::getName))).isEqualTo(String.class);
		assertThat(Accessors.giveReturnType(accessorByMethodReference(City::isCapital))).isEqualTo(boolean.class);
		assertThat(Accessors.giveReturnType(new AccessorChain<>(accessorByMethodReference(City::isCapital), accessorByMethodReference(Object::toString)))).isEqualTo(String.class);
		assertThat(Accessors.giveReturnType(new PropertyAccessor(accessorByMethodReference(City::getName), mutatorByMethodReference(City::setName)))).isEqualTo(String.class);
	}
	
	@Test
	void mutator() throws NoSuchFieldException {
		Field appender = StringAppender.class.getDeclaredField("appender");
		assertThat(Accessors.mutator(StringAppender.class, "appender").getSetter()).isEqualTo(appender);
		assertThat(Accessors.mutator(StringAppender.class, "appender", StringBuilder.class).getSetter()).isEqualTo(appender);
		assertThat(Accessors.mutator(StringAppender.class, "appender", CharSequence.class).getSetter()).isEqualTo(appender);
		
		assertThatThrownBy(() -> Accessors.mutator(StringAppender.class, "appender", String.class).getSetter())
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("Member type doesn't match expected one for field o.g.l.StringAppender.appender:"
						+ " expected j.l.String but is j.l.StringBuilder");
	}
	
	@Test
	void mutator_noSetter_fieldIsCompatible() {
		assertThat(Accessors.mutator(Toto.class, "propertyForBoxing", Long.class).getSetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForBoxing"));
		assertThat(Accessors.mutator(Toto.class, "propertyForTypeCompatibility", CharSequence.class).getSetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForTypeCompatibility"));
	}
	
	@Test
	void mutator_withMethodReferenceSetter() {
		assertThat(Accessors.mutator(Toto::setProperty).getMutator().getClass()).isEqualTo(MutatorByMethodReference.class);
		assertThat(((ValueAccessPointByMethod) Accessors.mutator(Toto::setProperty).getAccessor()).getMethod()).isEqualTo(Reflections.getMethod(Toto.class, "getProperty"));
	}
	
	@Test
	void accessor() {
		Method appender = Reflections.getMethod(StringAppender.class, "getAppender");
		assertThat(Accessors.accessor(StringAppender.class, "appender", StringBuilder.class).getGetter()).isEqualTo(appender);
		assertThat(Accessors.accessor(StringAppender.class, "appender", CharSequence.class).getGetter()).isEqualTo(appender);

		assertThatThrownBy(() -> Accessors.mutator(StringAppender.class, "appender", String.class).getSetter())
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("Member type doesn't match expected one for field o.g.l.StringAppender.appender:"
						+ " expected j.l.String but is j.l.StringBuilder");
	}
	
	@Test
	void accessor_noGetter_fieldIsCompatible() {
		assertThat(Accessors.accessor(Toto.class, "propertyForBoxing", Long.class).getGetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForBoxing"));
		assertThat(Accessors.accessor(Toto.class, "propertyForTypeCompatibility", CharSequence.class).getGetter()).isEqualTo(Reflections.findField(Toto.class, "propertyForTypeCompatibility"));
	}
		
	@Test
	void accessor_withMethodReferenceSetter() {
		assertThat(Accessors.accessor(Toto::getProperty).getAccessor().getClass()).isEqualTo(AccessorByMethodReference.class);
		assertThat(((ValueAccessPointByMethod) Accessors.accessor(Toto::getProperty).getMutator()).getMethod()).isEqualTo(Reflections.getMethod(Toto.class, "setProperty", Long.class));
	}
	
	@Test
	void mutatorByMethod_noSetter() throws NoSuchMethodException {
		assertThat(mutatorByMethod(Toto.class, "property").getMethod()).isEqualTo(Toto.class.getMethod("setProperty", Long.class));
	}
	
	@Test
	void mutatorByMethod_noMatchingField() {
		assertThatThrownBy(() -> mutatorByMethod(Toto.class, "noMatchingField"))
				.isInstanceOf(MemberNotFoundException.class)
				.hasMessage("Method setNoMatchingField() on o.g.r.AccessorsTest$Toto was not found");
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