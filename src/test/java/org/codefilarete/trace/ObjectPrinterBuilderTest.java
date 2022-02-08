package org.codefilarete.trace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
class ObjectPrinterBuilderTest {
	
	@Test
	void testToString_basic() {
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp2);
		assertEquals("prop1=Hello,prop2=World", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	@Test
	void testToString_excludingProperties() {
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp2)
				.except(Toto::getProp1);
		assertEquals("prop2=World", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	@Test
	void testToString_overringPrinter() {
		ObjectPrinterBuilder<Toto> testInstance = new ObjectPrinterBuilder<Toto>()
				.addProperty(Toto::getProp1)
				.addProperty(Toto::getProp2)
				.withPrinter(String.class, s -> "<" + s + ">");
		assertEquals("prop1=<Hello>,prop2=<World>", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	@Test
	void testFrom_excludingProperties() {
		ObjectPrinterBuilder<Toto> testInstance = ObjectPrinterBuilder.printerFor(Toto.class)
				.except(Toto::getProp1);
		assertEquals("prop2=World", testInstance.build().toString(new Toto().setProp1("Hello").setProp2("World")));
	}
	
	private static class Toto {
		
		private String prop1;
		private String prop2;
		
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
	}
	
}