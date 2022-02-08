A project aimed at providing access to bean properties through one main class : [PropertyAccessor](reflection/src/main/java/org/codefilarete/reflection/PropertyAccessor.java)

You can access properties [for reading](reflection/src/main/java/org/codefilarete/reflection/IAccessor.java) or [for writing](reflection/src/main/java/org/codefilarete/reflection/IMutator.java)
in different ways such as:
- fields [read](reflection/src/main/java/org/codefilarete/reflection/AccessorByField.java), [write](reflection/src/main/java/org/codefilarete/reflection/MutatorByField.java)
- methods [read](reflection/src/main/java/org/codefilarete/reflection/AccessorByMethod.java), [write](reflection/src/main/java/org/codefilarete/reflection/MutatorByMethod.java)
- method reference [read](reflection/src/main/java/org/codefilarete/reflection/AccessorByMethodReference.java), [write](reflection/src/main/java/org/codefilarete/reflection/MutatorByMethodReference.java)

A general purpose class also helps to give a general entry point : [Accessors](reflection/src/main/java/org/codefilarete/reflection/Accessors.java)