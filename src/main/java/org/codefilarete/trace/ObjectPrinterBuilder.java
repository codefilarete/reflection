package org.codefilarete.trace;

import org.codefilarete.reflection.Accessor;
import org.codefilarete.reflection.AccessorByMethod;
import org.codefilarete.reflection.AccessorByMethodReference;
import org.codefilarete.reflection.AccessorDefinition;
import org.codefilarete.reflection.PropertyAccessor;
import org.codefilarete.reflection.SerializableAccessor;
import org.codefilarete.reflection.ValueAccessPoint;
import org.codefilarete.reflection.ValueAccessPointByMethodReference;
import org.codefilarete.reflection.ValueAccessPointSet;
import org.codefilarete.tool.Experimental;
import org.codefilarete.tool.Reflections;
import org.codefilarete.tool.StringAppender;
import org.codefilarete.tool.bean.InstanceMethodIterator;
import org.codefilarete.tool.collection.Iterables;
import org.codefilarete.tool.collection.KeepOrderSet;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Builder for {@link ObjectPrinter}. {@link ObjectPrinter} may be used to give a trace of some instances, to be logged or debug.
 * Kind of Apache Commons ToStringBuilder with method references.
 * 
 * @author Guillaume Mary
 */
@Experimental(todo = { "implement recursion and overall prevent from stackoverflow", "test !" })
public class ObjectPrinterBuilder<C> {
	
	/**
	 * Starts a printer configurer that will print all (public) methods of given class (including inherited ones).
	 * Non wished properties may be removed by using {@link #except(SerializableAccessor)} on result.
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
	
	private final KeepOrderSet<PropertyDefinition<? extends C>> printableProperties = new KeepOrderSet<>();
	
	/** @apiNote we use a {@link ValueAccessPointSet} because its supports well contains() method with {@link Accessor} as argument */
	private final ValueAccessPointSet<C, ValueAccessPoint<C>> excludedProperties = new ValueAccessPointSet<>();
	
	private final Map<Class, Function<Object, String>> overriddenPrinters = new HashMap<>();
	
	/**
	 * Adds a property to be printed through its getter
	 * 
	 * @param getter the method reference that gives access to the property, can be one the parameterized class or one of its subtype
	 * @return this
	 */
	public <D extends C> ObjectPrinterBuilder<C> addProperty(SerializableAccessor<D, ?> getter) {
		AccessorByMethodReference<D, ?> accessor = new AccessorByMethodReference<>(getter);
		return addProperty(accessor);
	}
	
	private <D extends C> ObjectPrinterBuilder<C> addProperty(PropertyAccessor<D, ?> getter) {
		this.printableProperties.add(new PropertyDefinition<>(getter, null));
		return this;
	}
	
	public <D extends C, X, S extends Collection<X>> ObjectPrinterBuilder<C> addProperty(SerializableAccessor<D, S> getter, Class<X> componentType) {
		AccessorByMethodReference<D, ?> accessor = new AccessorByMethodReference<>(getter);
		return addProperty(accessor, componentType);
	}
	
	private <D extends C, X> ObjectPrinterBuilder<C> addProperty(PropertyAccessor<D, ?> getter, Class<X> componentType) {
		this.printableProperties.add(new PropertyDefinition<>(getter, componentType));
		return this;
	}
	
	/**
	 * Excludes a property from being printed, useful in combination of {@link #printerFor(Class)}
	 *
	 * @param getter the method reference that gives access to the property
	 * @return this
	 */
	public ObjectPrinterBuilder<C> except(SerializableAccessor<C, ?> getter) {
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
		this.overriddenPrinters.put(overridenType, (Function<Object, String>) printer);
		return this;
	}
	
	/**
	 * Builds final printer
	 * 
	 * @return a configured printer for current type
	 */
	public ObjectPrinter<C> build() {
		LinkedHashMap<String, PropertyDefinition<C>> printingFunctionByPropertyName = new LinkedHashMap<>();
		for (PropertyDefinition<? extends C> printableProperty : printableProperties) {
			String methodName = AccessorDefinition.giveDefinition(printableProperty.getGetter()).getName();
			if (!excludedProperties.contains(printableProperty.getGetter())) {
				printingFunctionByPropertyName.put(methodName, (PropertyDefinition<C>) printableProperty);
			}
		}
		return new ObjectPrinter<>(printingFunctionByPropertyName, overriddenPrinters);
	}
	
	/**
	 * Printer for parameterized type
	 * 
	 * @param <C> target type to print
	 */
	public static class ObjectPrinter<C> {
		
		private final Map<String, PropertyDefinition<C>> printableProperties;
		private final Map<Class, Function<Object, String>> overriddenPrinters;
		
		/**
		 * @apiNote private because {@link ObjectPrinterBuilder} is expected to be used for configuration 
		 */
		private ObjectPrinter(LinkedHashMap<String, PropertyDefinition<C>> printableProperties, Map<Class, Function<Object, String>> overriddenPrinters) {
			this.printableProperties = printableProperties;
			this.overriddenPrinters = overriddenPrinters;
		}
		
		/**
		 * @param object an instance to be printed
		 * @return a {@link String} representing given instance according to configured properties to print
		 */
		public String toString(C object) {
			StringAppender result = new StringAppender();
			String separator = ",";
			printableProperties.forEach((propName, getter) -> {
				if (getter.getComponentType() != null) {
					Object value = getter.getGetter().get(object);
					if (value != null) {
						StringAppender collectionResult = new StringAppender();
						((Collection) value).forEach(item -> {
							
							collectionResult.cat(item.getClass().getSimpleName(), "{");
							Entry<Class, Function<Object, String>> foundOverringPrinter = Iterables.find(overriddenPrinters.entrySet(),
									e -> getter.getComponentType().isAssignableFrom(e.getKey()));
							Object valueToPrint;
							if (foundOverringPrinter != null) {
								valueToPrint = foundOverringPrinter.getValue().apply(item);
							} else {
								valueToPrint = item;
							}
							collectionResult.cat(valueToPrint, "}", separator);
						});
						collectionResult.cutTail(separator.length());
						result.cat(propName, "=[", collectionResult, "]", separator);
					} else {
						result.cat(propName, "=null", separator);
					}
				} else {
					// we prevent subclass property accessor of being invoked on parent class
					boolean getterCompliesWithInstance;
					if (getter.getGetter() instanceof ValueAccessPointByMethodReference) {
						getterCompliesWithInstance = ((ValueAccessPointByMethodReference<C>) getter.getGetter()).getDeclaringClass().isInstance(object);
					} else {
						// necessarly AccessorByMethod, see printerFor(Class)
						getterCompliesWithInstance = ((AccessorByMethod) getter.getGetter()).getGetter().getDeclaringClass().isInstance(object);
					}
					if (getterCompliesWithInstance) {
						final Object value = getter.getGetter().get(object);
						Entry<Class, Function<Object, String>> foundOverringPrinter = Iterables.find(overriddenPrinters.entrySet(),
								e -> e.getKey().isInstance(value));
						Object valueToPrint;
						if (foundOverringPrinter != null) {
							String printerValue = foundOverringPrinter.getValue().apply(value);
							valueToPrint = foundOverringPrinter.getKey().getSimpleName() + "{" + printerValue + "}";
						} else {
							valueToPrint = value;
						}
						result.cat(propName, "=", valueToPrint, separator);
					}
				}
			});
			return result.cutTail(separator.length()).toString();
		}
	}
	
	private static class PropertyDefinition<C> {
		
		private final PropertyAccessor<C, ?> getter;
		private final Class<?> componentType;
		
		PropertyDefinition(PropertyAccessor<C, ?> getter, Class<?> componentType) {
			this.getter = getter;
			this.componentType = componentType;
		}
		
		public Accessor<C, ?> getGetter() {
			return getter;
		}
		
		public Class<?> getComponentType() {
			return componentType;
		}
	}
}
