A project aimed at providing access to bean properties through one main class : [PropertyAccessor](src/main/java/org/gama/reflection/PropertyAccessor.java)

You can access properties [for reading](src/main/java/org/gama/reflection/IAccessor.java) or [for writing](src/main/java/org/gama/reflection/IMutator.java)
in different ways such as:
- fields [read](src/main/java/org/gama/reflection/AccessorByField.java), [write](src/main/java/org/gama/reflection/MutatorByField.java)
- methods [read](src/main/java/org/gama/reflection/AccessorByMethod.java), [write](src/main/java/org/gama/reflection/MutatorByMethod.java)
- method reference [read](src/main/java/org/gama/reflection/AccessorByMethodReference.java), [write](src/main/java/org/gama/reflection/MutatorByMethodReference.java)

A general purpose class also helps to give a general entry point : [Accessors](src/main/java/org/gama/reflection/Accessors.java)