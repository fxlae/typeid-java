# typeid-java

[![CircleCI](https://circleci.com/gh/fxlae/typeid-java.svg?style=shield)](https://circleci.com/gh/fxlae/typeid-java)

## A Java implementation of [TypeID](https://github.com/jetpack-io/typeid).

TypeIDs are a modern, **type-safe**, globally unique identifier based on the upcoming
UUIDv7 standard. They provide a ton of nice properties that make them a great choice
as the primary identifiers for your data in a database, APIs, and distributed systems.
Read more about TypeIDs in their [spec](https://github.com/jetpack-io/typeid).

## Requirements
- Java 8 or higher

## Usage
An instance of `TypeID` can be obtained in several ways. It is immutable and thread-safe. Examples:

Generate a new `TypeID`, based on UUIDv7:

```java
TypeId typeId = TypeId.generate();
typeId.getPrefix(); // ""
typeId.getUuid(); // v7, java.util.UUID(01890a5d-ac96-774b-bcce-b302099a8057)
typeId.toString(); // "01h455vb4pex5vsknk084sn02q"

TypeId typeId = TypeId.generate("someprefix");
typeId.getPrefix(); // "someprefix"
typeId.toString(); // "someprefix_01h455vb4pex5vsknk084sn02q"
```

Construct a `TypeID` from arguments (any UUID version):
```java
TypeId typeId = TypeId.of("someprefix", UUID.randomUUID()); 
typeId.getUuid(); // v4, java.util.UUID(9c8ec0e7-020b-4caf-87c0-38fb6c0ebbe2)
```

Obtain an instance of `TypeID` from a text string (any UUID version):
```java
TypeId typeId = TypeId.parse("01h455vb4pex5vsknk084sn02q")
TypeId typeId = TypeId.parse("someprefix_01h455vb4pex5vsknk084sn02q")
```

## Building From Source
```console
foo@bar:~$ git clone https://github.com/fxlae/typeid-java.git
foo@bar:~$ cd typeid-java
foo@bar:~/typeid-java$ ./gradlew assemble
```