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

## Operators

### Dual operators
| precedence | symbol | latex | unicode | description |
| ---------- | ------:| ------- | ----- | ----------- |
| 3 | &#8746;   | \cup  | \u222A | meet |
| 3 | &#8745;   | \cap  | \u2229 | join |
| 3 | &#124;  | \vert | \u007C | geometric product |
| 3 | &#8901;   | \cdot | \u22C5 | inner product |
| 3 | &#8970; | \llcorner | \u230B | right contraction |
| 3 | &#8971; | \lrcorner | \u230A | left contraction |
| 3 | &#8743; | \wedge | \u2227 | outer product |
| 3 | &#8744; | \vee | \u2228 | shortcut for (X* &#8743; Y*)* |
| 2 | &#42;  | * | | scalar product |
| 2 | &#47;  | \StrikingThrough | \u2F | division |
| 1 | &#43;  | + | \u2B | sum |
| 1 | &#45; | - | \u2D| subtraction |

### Single (right) side operators
| precedence | symobol | latex | description |
| ---------- | ------:| ----- | ----------- |
| 4 | &#8315;&#185;    | \textsuperscript{-1} | general inverse |
| 4 | *    | \textsuperscript{*} | dual |
| 4 | &#8315;*    | \textsuperscript{-*} | undual |
| 4 | &#732;    | \textsuperscript{\tilde} | reverse |
| 4 | &#8224;    | \textsuperscript{\textdagger} | conjugate |

### Buildin functions
| precedence | symbol | latex | description |
| ---------- | ------:| ----- | ----------- |
| 4 | exp()    | \exp{} | exponential |
| 4 | involute()    |  | grade inversion |
| 4 | reverse()    |  \textsuperscript{\tilde} | reverse |
| 4 | conjugate()    | \textsuperscript{\textdagger} | clifford conjugate |

### Symbols
| symbol | latex | description |
| ------:| ----- | ----------- |
| o   |  | base vector representing the origin |
| &#8734;      |  | base vector representing the infinity |
| &#949;&#8321;   | \textepsilon   | base vector representing x direction |
| &#949;&#8322;  | \textepsilon  | base vector representing y direction |
| &#949;&#8323;   | \textepsilon  | base vector representing y direction |
