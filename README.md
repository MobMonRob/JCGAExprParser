# DSL4GeometricAlgebra

This repository contains code to work with multivector expressions of conformal algebra. The code is used as a reference implementation to demonstrate and test the software pipeline of truffle/graal in the context of geometric algebra and underlaying multivector implementations.

The code is in very early stage.

## GraalVM Setup
Download the [GraalVM](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.0.0.2/graalvm-ce-java11-linux-amd64-22.0.0.2.tar.gz)

Extract the downloaded archive to an arbitrary location.

### Netbeans configuration
Add a new java platform with the name "GraalVM 11". \
Within the Netbeans 13 IDE you can do this if you follow these steps:
- open project properties via right-click on the project
- navigate to Build / Compile
- click "Manage Java Platforms..."

or navigate to this point via the Tools main menu.

- click "Add Platform..."
- In the poping-up wizard:
  - select platform type "Java Standard Edition"
  - choose the platform folder within the extracted archive.
  - name it "GraalVM 11"

### Netbeans project configuration
- open project properties via right-click on the project
- navigate to Build / Compile
- in the drop-down list labeled "Java Platform" choose "GraalVM 11"

