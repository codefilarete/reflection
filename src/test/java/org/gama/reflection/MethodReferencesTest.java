package org.gama.reflection;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.text.Collator;

import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gama.reflection.MethodReferences.getTargetMethodRawSignature;
import static org.gama.reflection.MethodReferences.buildSerializedLambda;

/**
 * @author Guillaume Mary
 */
public class MethodReferencesTest {
	
	@Test
	public void testGetTargetMethodRawSignature() throws ReflectiveOperationException {
		assertThat(getTargetMethodRawSignature(MethodReferences.buildSerializedLambda(Object::toString))).isEqualTo("java/lang/ObjecttoString()Ljava/lang/String;");
		assertThat(getTargetMethodRawSignature(MethodReferences.buildSerializedLambda(Integer::shortValue))).isEqualTo("java/lang/IntegershortValue()S");
		assertThat(getTargetMethodRawSignature(buildSerializedLambda(Collator::setStrength))).isEqualTo("java/text/CollatorsetStrength(I)V");
	}
	
	@Test
	public void testBuildSerializedLambda_getter() throws ReflectiveOperationException {
		SerializedLambda serializedLambda = MethodReferences.buildSerializedLambda(Object::toString);
		Method method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName());
		assertThat(method).isEqualTo(Reflections.getMethod(Object.class, "toString"));
		
		serializedLambda = MethodReferences.buildSerializedLambda(Integer::shortValue);
		method = Reflections.getMethod(Class.forName(serializedLambda.getImplClass().replace("/", ".")), serializedLambda.getImplMethodName());
		assertThat(method).isEqualTo(Reflections.getMethod(Integer.class, "shortValue"));
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
		assertThat(method).isEqualTo(Reflections.getMethod(DummyClassWithSetter.class, "setX", Integer.class));
	}
	
	public static class DummyClassWithSetter {
		public void setX(Integer x) {
			
		}
	}
}