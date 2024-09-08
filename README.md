[![Build Status](https://ci.codefilarete.org/jenkins/buildStatus/icon?job=Reflection+pipeline)](https://ci.codefilarete.org/jenkins/job/Reflection%20pipeline/)
[![Quality Gate Status](https://ci.codefilarete.org/sonar/api/project_badges/measure?project=Reflection&metric=alert_status)](https://ci.codefilarete.org/sonar/dashboard?id=Reflection)
[![Coverage](https://ci.codefilarete.org/sonar/api/project_badges/measure?project=Reflection&metric=coverage)](https://ci.codefilarete.org/sonar/dashboard?id=Reflection)
[![Vulnerabilities](https://ci.codefilarete.org/sonar/api/project_badges/measure?project=Reflection&metric=vulnerabilities)](https://ci.codefilarete.org/sonar/dashboard?id=Reflection)

A project aimed at providing access to bean properties through one main class : [PropertyAccessor](src/main/java/org/codefilarete/reflection/PropertyAccessor.java)

You can access properties [for reading](src/main/java/org/codefilarete/reflection/Accessor.java) or [for writing](src/main/java/org/codefilarete/reflection/Mutator.java)
in different ways such as:
- fields [read](src/main/java/org/codefilarete/reflection/AccessorByField.java), [write](src/main/java/org/codefilarete/reflection/MutatorByField.java)
- methods [read](src/main/java/org/codefilarete/reflection/AccessorByMethod.java), [write](src/main/java/org/codefilarete/reflection/MutatorByMethod.java)
- method reference [read](src/main/java/org/codefilarete/reflection/AccessorByMethodReference.java), [write](src/main/java/org/codefilarete/reflection/MutatorByMethodReference.java)

A general purpose class also helps to give a general entry point : [Accessors](src/main/java/org/codefilarete/reflection/Accessors.java)