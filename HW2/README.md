# Compilers - K31
## Homework 2 – MiniJava Static Checking (Semantic Analysis)

Siotis Konstantinos\
1115201700140

### Implementation

The implementation uses jtb and javacc.\
Two visitors where used:
* FillSymbolTableVisitor
    * handles declerations of classes, methods, fields and variables and makes sure no duplicate names exist in the same class. 
    * uses them to fill the symbolTable with the apropriate stuctures.
* TypecheckVisitor
    * makes sure that the correct type of value is used in every variable, method and returned values.
    * uses pre-filled symbolTable by FillSymbolTableVisitor.

### Instructions

#### To compile:

    make

#### To use:

    java Main <File1> <File2> ...

#### Alternative use:

    runInDirectory.sh <Directory of files>

#### To cleanup files:

    make clean

### Testing

This implementation was tested on the files inside each directory in `examples` using the following commands:

##### Input: 

    $ ./runInDirectory.sh ./examples/fail

##### Output: 
``` 
    ...
    ...
    ...
Successfully parsed 0/17 files.
``` 
##### Input: 
    $ ./runInDirectory.sh ./examples/success
##### Output:
``` 
    ...
    ...
    ...
Successfully parsed 27/27 files.
``` 