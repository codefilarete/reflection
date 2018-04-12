package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.text.Collator;

import org.gama.lang.Reflections;
import org.gama.lang.Strings;
import org.junit.jupiter.api.Test;

import static org.gama.reflection.MethodReferences.getTargetMethodRawSignature;
import static org.gama.reflection.MethodReferences.buildSerializedLambda;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Guillaume Mary
 */
public class MethodReferencesTest {
	
	@Test
	public void testGetTargetMethodRawSignature() throws ReflectiveOperationException {
		assertEquals("java/lang/ObjecttoString()Ljava/lang/String;", getTargetMethodRawSignature(MethodReferences.buildSerializedLambda(Object::toString)));
		assertEquals("java/lang/IntegershortValue()S", getTargetMethodRawSignature(MethodReferences.buildSerializedLambda(Integer::shortValue)));
		assertEquals("java/text/CollatorsetStrength(I)V", getTargetMethodRawSignature(buildSerializedLambda(Collator::setStrength)));
	}
	
	@Test
	public void testBuildSerializedLambda_getter() throws ReflectiveOperationException {
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(Object::toString);
		Method method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName());
		assertEquals(Reflections.getMethod(Object.class, "toString"), method);
		
		serializedLambda = MethodReferences.buildSerializedLambda(Integer::shortValue);
		method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName());
		assertEquals(Reflections.getMethod(Integer.class, "shortValue"), method);
	}
	
	@Test
	public void testBuildSerializedLambda_setter() throws ReflectiveOperationException {
		SerializedLambda serializedLambda = buildSerializedLambda(DummyClassWithSetter::setX);
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
		assertEquals(Reflections.getMethod(DummyClassWithSetter.class, "setX", Integer.class), method);
	}
	
	public static class DummyClassWithSetter {
		public void setX(Integer x) {
			
		}
	}
}