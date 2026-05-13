package org.codefilarete.trace;

import org.codefilarete.tool.collection.Arrays;
import org.codefilarete.trace.ObjectPrinterBuilder.ObjectPrinter;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
class ObjectPrinterBuilderTest {
	
	@Test
	void toString_basic() {
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp2);
		assertEquals("prop1=Hello,prop2=World", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	@Test
	void toString_withRelatedObject() {
		ObjectPrinter<Tata> tataObjectPrinter = new ObjectPrinterBuilder<Tata>()
				.addProperty(Tata::getProp1)
				.build();
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp3)
				.withPrinter(Tata.class, tataObjectPrinter::toString);
		assertEquals("prop1=Hello,prop3=Tata{prop1=World}", testInstance.build().toString(new Toto().setProp1("Hello").setProp3(new Tata("World"))));
	}
	
	@Test
	void toString_withCollection() {
		ObjectPrinter<Tata> tataObjectPrinter = new ObjectPrinterBuilder<Tata>()
				.addProperty(Tata::getProp1)
				.build();
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp2)
				.addProperty(Toto::getProp4, Tata.class)
				.withPrinter(Tata.class, tataObjectPrinter::toString);
		assertEquals("prop1=Hello,prop2=World,prop4=[Tata{prop1=titi},Tata{prop1=tutu}]", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World").setProp4(Arrays.asSet(new Tata("titi"), new Tata("tutu")))));
	}
	
	@Test
	void toString_excludingProperties() {
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp2)
				.except(Toto::getProp1);
		assertEquals("prop2=World", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	@Test
	void toString_overringPrinter() {
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp2)
				.withPrinter(String.class, s -> "<" + s + ">");
		assertEquals("prop1=String{<Hello>},prop2=String{<World>}", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	@Test
	void printerFor_excludingProperties() {
		ObjectPrinterBuilder<Toto> testInstance = ObjectPrinterBuilder.printerFor(Toto.class)
				.except(Toto::getProp1)
				.except(Toto::getProp3)
				.except(Toto::getProp4);
		assertEquals("prop2=World", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	private static class Toto {
		
		private String prop1;
		private String prop2;
		private Tata prop3;
		private Set<Tata> prop4;
		
		public String getProp1() {
			return prop1;
		}
		
		public Toto setProp1(String prop1) {
			this.prop1 = prop1;
			return this;
		}
		
		public String getProp2() {
			return prop2;
		}
		
		public Toto setProp2(String prop2) {
			this.prop2 = prop2;
			return this;
		}
		
		public Tata getProp3() {
			return prop3;
		}
		
		public Toto setProp3(Tata prop3) {
			this.prop3 = prop3;
			return this;
		}
		
		public Set<Tata> getProp4() {
			return prop4;
		}
		
		public Toto setProp4(Set<Tata> prop4) {
			this.prop4 = prop4;
			return this;
		}
	}
	
	private static class Tata {
		
		private String prop1;
		
		private Tata(String prop1) {
			this.prop1 = prop1;
		}
		
		public String getProp1() {
			return prop1;
		}
	}
	
}