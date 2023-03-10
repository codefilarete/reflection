package org.codefilarete.reflection;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.text.Collator;

import org.assertj.core.api.Assertions;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Mary
 */
public class MethodReferencesTest {
	
	@Test
	void getTargetMethodRawSignature() {
		Assertions.assertThat(MethodReferences.getTargetMethodRawSignature(MethodReferences.buildSerializedLambda(Object::toString))).isEqualTo("java/lang/ObjecttoString()Ljava/lang/String;");
		Assertions.assertThat(MethodReferences.getTargetMethodRawSignature(MethodReferences.buildSerializedLambda(Integer::shortValue))).isEqualTo("java/lang/IntegershortValue()S");
		Assertions.assertThat(MethodReferences.getTargetMethodRawSignature(MethodReferences.buildSerializedLambda(Collator::setStrength))).isEqualTo("java/text/CollatorsetStrength(I)V");
	}
	
	@Test
	void buildSerializedLambda_getter() throws ReflectiveOperationException {
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(Object::toString);
		Method method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName());
		assertThat(method).isEqualTo(Reflections.getMethod(Object.class, "toString"));
		
		serializedLambda = MethodReferences.buildSerializedLambda(Integer::shortValue);
		method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName());
		assertThat(method).isEqualTo(Reflections.getMethod(Integer.class, "shortValue"));
	}
	
	@Test
	void buildSerializedLambda_setter() throws ReflectiveOperationException {
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(DummyClassWithSetter::setX);
		// extracting method type argument from serialized lambda method type
		String instantiatedMethodType = serializedLambda.getInstantiatedMethodType();
		// cf Class#getName()
		// removing parenthesis, trailing "V", last ";" (to avoid array given by split method to have a last null value)
		instantiatedMethodType = instantiatedMethodType.substring(1, instantiatedMethodType.length()-3);
		// we skip the very first one because it is target instance type (BiConsumer)
		String[] lambdaParameterTypeNames = instantiatedMethodType.split(";");
		String methodParameterTypeName = Strings.cutHead(lambdaParameterTypeNames[1], 1).toString();
		
		Class<?> methodParameterType = Class.forName(methodParameterTypeName.replace("/", "."));
		Method method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName(), methodParameterType);
		assertThat(method).isEqualTo(Reflections.getMethod(DummyClassWithSetter.class, "setX", Integer.class));
	}
	
	@Test
	void giveImplementingClass() {
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(DummyClassWithSetter::setX);
		assertThat(MethodReferences.giveImplementingClass(serializedLambda)).isEqualTo(DummyClassWithSetter.class);
		
		serializedLambda = MethodReferences.buildSerializedLambda(InheritingDummyClassWithSetter::setX);
		assertThat(MethodReferences.giveImplementingClass(serializedLambda)).isEqualTo(DummyClassWithSetter.class);
		
		serializedLambda = MethodReferences.buildSerializedLambda(Integer::equals);
		assertThat(MethodReferences.giveImplementingClass(serializedLambda)).isEqualTo(Integer.class);
		
		serializedLambda = MethodReferences.buildSerializedLambda(Object::equals);
		assertThat(MethodReferences.giveImplementingClass(serializedLambda)).isEqualTo(Object.class);
		
		// test with interface
		serializedLambda = MethodReferences.buildSerializedLambda(CharSequence::length);
		assertThat(MethodReferences.giveImplementingClass(serializedLambda)).isEqualTo(CharSequence.class);
	}
	
	@Test
	void giveInstantiatedClass() {
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(DummyClassWithSetter::setX);
		assertThat(MethodReferences.giveInstantiatedClass(serializedLambda)).isEqualTo(DummyClassWithSetter.class);
		
		serializedLambda = MethodReferences.buildSerializedLambda(InheritingDummyClassWithSetter::setX);
		assertThat(MethodReferences.giveInstantiatedClass(serializedLambda)).isEqualTo(InheritingDummyClassWithSetter.class);
		
		serializedLambda = MethodReferences.buildSerializedLambda(Integer::equals);
		assertThat(MethodReferences.giveInstantiatedClass(serializedLambda)).isEqualTo(Integer.class);
		
		serializedLambda = MethodReferences.buildSerializedLambda(Object::equals);
		assertThat(MethodReferences.giveInstantiatedClass(serializedLambda)).isEqualTo(Object.class);
		
		// test with interface
		serializedLambda = MethodReferences.buildSerializedLambda(String::length);
		assertThat(MethodReferences.giveInstantiatedClass(serializedLambda)).isEqualTo(String.class);
	}
	
	public static class DummyClassWithSetter {
		public void setX(Integer x) {
			
		}
	}
	
	public static class InheritingDummyClassWithSetter extends DummyClassWithSetter {
	
	}
}