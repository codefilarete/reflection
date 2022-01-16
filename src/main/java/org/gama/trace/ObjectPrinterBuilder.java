package org.gama.trace;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.danekja.java.util.function.serializable.SerializableFunction;
import org.gama.lang.Experimental;
import org.gama.lang.Reflections;
import org.gama.lang.StringAppender;
import org.gama.lang.bean.InstanceMethodIterator;
import org.gama.lang.collection.Iterables;
import org.gama.lang.collection.KeepOrderSet;
import org.gama.reflection.Accessor;
import org.gama.reflection.AccessorByMethod;
import org.gama.reflection.AccessorByMethodReference;
import org.gama.reflection.AccessorDefinition;
import org.gama.reflection.ValueAccessPointByMethodReference;
import org.gama.reflection.ValueAccessPointSet;

/**
 * Builder for {@link ObjectPrinter}. {@link ObjectPrinter} may be used to give a trace of some instances, to be logged or debug.
 * Kind of Apache Commons ToStringBuilder with method references.
 * 
 * @author Guillaume Mary
 */
@Experimental(todo = { "implement recursivity and overall prevent from stackoverflow", "test !" })
public class ObjectPrinterBuilder<C> {
	
	/**
	 * Starts a printer configurer that will print all (public) methods of given class (including inherited ones).
	 * Non wished properties may be removed by using {@link #except(SerializableFunction)} on result.
	 *
	 * @param type the class which methods must be printed
	 * @return a {@link ObjectPrinterBuilder} that will print all methods of given class, and may be further configured
	 */
	@Experimental(todo = { "remove addProperty from result" })
	public static <T> ObjectPrinterBuilder<T> printerFor(Class<T> type) {
		ObjectPrinterBuilder<T> result = new ObjectPrinterBuilder<>();
		Iterable<Method> methodIterable = () -> new InstanceMethodIterator(type);
		// we add class getters 
		for (Method method : methodIterable) {
			AccessorByMethod<T, Object> accessorByMethod = Reflections.onJavaBeanPropertyWrapperNameGeneric(method.getName(), method,
					m -> new AccessorByMethod<>(method),
					m -> null,
					m -> new AccessorByMethod<>(method),
					m -> null /* method is not a getter, we exclude it by returning null (filtered below) */);
			if (accessorByMethod != null) {
				result.addProperty(accessorByMethod);
			}
		}
		return result;
	}
	
	/** @apiNote  */
	private final KeepOrderSet<Accessor<C, Object>> printableProperties = new KeepOrderSet<>();
	/** @apiNote we use a {@link ValueAccessPointSet} because its supports well contains() method with {@link Accessor} as argument */
	private final ValueAccessPointSet excludedProperties = new ValueAccessPointSet();
	
	private final Map<Class, Function<Object, String>> overridenPrinters = new HashMap<>();
	
	/**
	 * Adds a property to be printed through its getter
	 * 
	 * @param getter the method reference that gives access to the property, can be one the parameterized class or one of its subtype
	 * @return this
	 */
	public <D extends C> ObjectPrinterBuilder<C> addProperty(SerializableFunction<D, Object> getter) {
		return this.addProperty(new AccessorByMethodReference(getter));
	}
	
	private ObjectPrinterBuilder<C> addProperty(Accessor<C, Object> getter) {
		this.printableProperties.add(getter);
		return this;
	}
	
	/**
	 * Excludes a property from being printed, usefull in combination of {@link #printerFor(Class)}
	 *
	 * @param getter the method reference that gives access to the property
	 * @return this
	 */
	public ObjectPrinterBuilder<C> except(SerializableFunction<C, Object> getter) {
		this.excludedProperties.add(new AccessorByMethodReference<>(getter));
		return this;
	}
	
	/**
	 * Specifies a printer for a particular type
	 * 
	 * @param overridenType the type which printing must be changed
	 * @param printer the function to use for printing
	 * @param <E> customized type
	 * @return this
	 */
	public <E> ObjectPrinterBuilder<C> withPrinter(Class<E> overridenType, Function<E, String> printer) {
		this.overridenPrinters.put(overridenType, (Function<Object, String>) printer);
		return this;
	}
	
	/**
	 * Builds final printer
	 * 
	 * @return a configured printer for current type
	 */
	public ObjectPrinter<C> build() {
		LinkedHashMap<String, Accessor<C, Object>> printingFunctionByPropertyName = new LinkedHashMap<>();
		for (Accessor<C, Object> printableProperty : printableProperties) {
			String methodName = AccessorDefinition.giveDefinition(printableProperty).getName();
			if (!excludedProperties.contains(printableProperty)) {
				printingFunctionByPropertyName.put(methodName, printableProperty);
			}
		}
		return new ObjectPrinter<>(printingFunctionByPropertyName, overridenPrinters);
	}
	
	/**
	 * Printer for parameterized type
	 * 
	 * @param <C> target type to print
	 */
	public static class ObjectPrinter<C> {
		
		private final Map<String, Accessor<C, Object>> printableProperties;
		private final Map<Class, Function<Object, String>> overridenPrinters;
		
		/**
		 * @apiNote private because {@link ObjectPrinterBuilder} is expected to be used for configuration 
		 */
		private ObjectPrinter(LinkedHashMap<String, Accessor<C, Object>> printableProperties, Map<Class, Function<Object, String>> overridenPrinters) {
			this.printableProperties = printableProperties;
			this.overridenPrinters = overridenPrinters;
		}
		
		/**
		 * @param object an instance to be printed
		 * @return a {@link String} representing given instance according to configured properties to print
		 */
		public String toString(C object) {
			StringAppender result = new StringAppender();
			String separator = ",";
			printableProperties.forEach((propName, getter) -> {
				// we prevent subclass property accessor of being invoked on parent class
				boolean getterCompliesWithInstance;
				if (getter instanceof ValueAccessPointByMethodReference) {
					getterCompliesWithInstance = ((ValueAccessPointByMethodReference) getter).getDeclaringClass().isInstance(object);
				} else {
					// necessarly AccessorByMethod, see printerFor(Class)
					getterCompliesWithInstance = ((AccessorByMethod) getter).getGetter().getDeclaringClass().isInstance(object);
				}
				if (getterCompliesWithInstance) {
					final Object value = getter.get(object);
					Entry<Class, Function<Object, String>> foundOverringPrinter = Iterables.find(overridenPrinters.entrySet(),
							e -> e.getKey().isInstance(value));
					Object valueToPrint;
					if (foundOverringPrinter != null) {
						valueToPrint = foundOverringPrinter.getValue().apply(value);
					} else {
						valueToPrint = value;
					}
					result.cat(propName, "=", valueToPrint, separator);
				}
			});
			return result.cutTail(separator.length()).toString();
		}
	}
}
