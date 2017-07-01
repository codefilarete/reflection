package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

import org.gama.lang.Reflections;
import org.gama.lang.Strings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Guillaume Mary
 */
public class MethodReferencesTest {
	
	@Test
	public void testGetSerializedLambda_getter() throws ReflectiveOperationException {
		SerializedLambda serializedLambda = MethodReferences.getSerializedLambda(Object::toString);
		Method method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName());
		assertEquals(Reflections.getMethod(Object.class, "toString"), method);
	}
	
	@Test
	public void testGetSerializedLambda_setter() throws ReflectiveOperationException {
		SerializedLambda serializedLambda = MethodReferences.getSerializedLambda(DummyClassWithSetter::setX);
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