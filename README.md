# typeid-java

![Build Status](https://github.com/fxlae/typeid-java/actions/workflows/build-on-push.yml/badge.svg) ![Maven Central](https://img.shields.io/maven-central/v/de.fxlae/typeid-java) ![License Info](https://img.shields.io/github/license/fxlae/typeid-java)

## A Java implementation of [TypeID](https://github.com/jetpack-io/typeid).

TypeIDs are a modern, type-safe, globally unique identifier based on the upcoming
UUIDv7 standard. They provide a ton of nice properties that make them a great choice
as the primary identifiers for your data in a database, APIs, and distributed systems.
Read more about TypeIDs in their [spec](https://github.com/jetpack-io/typeid).

## Installation

Starting with version `0.3.0`, `typeid-java` requires at least Java 17.

<details>
<summary>(Details on Java 8+ support)</summary>
Up to version 0.2.x, a separate artifact called `typeid-java-jdk8` was published, supporting Java versions 8 and higher, and covering all relevant use cases, albeit with less syntactic sugar. If you are running Java 8 through 16, you can still use `typeid-java-jdk8:0.2.x`, which is still available and remains fully functional. However, it will no longer receive updates and is limited to the TypeId spec version 0.2.0.
</details>

To install via Maven:

```xml
<dependency>
    <groupId>de.fxlae</groupId>
    <artifactId>typeid-java</artifactId>
    <version>0.3.1</version>
</dependency>
```

For installation via Gradle:

```kotlin
implementation("de.fxlae:typeid-java:0.3.1")
```

## Usage

`TypeId` instances can be obtained in several ways. They are immutable and thread-safe.

### Generating new TypeIDs


#### generate

To generate a new `TypeId`, based on UUIDv7 as per specification:

```java
var typeId = TypeId.generate("user");
typeId.toString(); // "user_01h455vb4pex5vsknk084sn02q"
typeId.prefix(); // "user"
typeId.uuid(); // java.util.UUID(01890a5d-ac96-774b-bcce-b302099a8057), based on UUIDv7
```

#### of

To construct (or reconstruct) a `TypeId` from existing arguments:

```java
var typeId = TypeId.of("user", someUuid);
```
As a side effect, `of` can also be used as an "extension point" to plug-in custom UUID generators.
### Parsing TypeID strings

For parsing, the library supports both an imperative programming model and a more functional style.

#### parse
The most straightforward way to parse the textual representation of a TypeID:

```java
var typeId = TypeId.parse("user_01h455vb4pex5vsknk084sn02q");
```

Invalid inputs will result in an `IllegalArgumentException`, with a message explaining the cause of the parsing failure.

#### parseToOptional

It's also possible to obtain an `Optional<TypeId>` in cases where the concrete error message is not relevant.

```java
var maybeTypeId = TypeId.parseToOptional("user_01h455vb4pex5vsknk084sn02q");
```

#### parseToValidated

If you prefer working with errors modeled as return values rather than exceptions, this is also possible (and is *much* more performant for untrusted input with high error rates, as no stacktrace is involved):


```java
var validated = TypeId.parseToValidated("user_01h455vb4pex5vsknk084sn02q");

if(validated.isValid) {
    var typeId = validated.get();
    // Proceed with typeId
} else {
    var message = validated.message();
    // Optionally, do something with the error message (or omit this branch completely)
}
```
Note: Checking `validated.isValid` is advisable for untrusted input. Similar to `Optional.get`, invoking `Validated.get` for invalid TypeIds (or `Validated.message` for valid TypeIds) will lead to a `NoSuchElementException`.

`Validated` and its implementations `Valid` and `Invalid` form a sealed type hierarchy. This feature becomes especially useful in more recent Java versions, beginning with Java 21, which facilitates Record Patterns (destructuring) and Pattern Matching for switch (yes, `TypeId` is a `record`):

```java
// this compiles and runs from Java 21 onwards

var report = switch(TypeId.parseToValidated("...")) {
    case Valid(TypeId(var prefix, var uuid)) when "user".equals(prefix) -> "user with UUID" + uuid;
    case Valid(TypeId(var prefix, var ignored)) -> "Not a user. Prefix is " + prefix;
    case Invalid(var message) -> "Parsing failed :( ... " + message;
};
```
Note the absent (and superfluous) default case. Exhaustiveness is checked during compilation!

Another safe alternative for working with `Validated<TypeId>` involves methods that can be called without risk, namely:

- For transformations: `map`, `flatMap`, `filter`, `orElse`
- For implementing side effects: `ifValid` and `ifInvalid`

```java
// transform
var mappedToPrefix = TypeId.parseToValidated("dog_01h455vb4pex5vsknk084sn02q")
    .map(TypeId::prefix)  // Validated<TypeId> -> Validated<String>
    .filter("Not a cat! :(", prefix -> !"cat".equals(prefix)); // the predicate fails

// execute side effects, e.g. logging
mappedToPrefix.ifValid(prefix -> log.info(prefix)) // called on success, so not in this case
mappedToPrefix.ifInvalid(message -> log.warn(message)) // logs "Not a cat! :("
```



## But wait, isn't this less type-safe than it could be?
 <details>
    <summary>Details</summary>

That's correct. The prefix of a TypeId is currently just a simple `String`. If you want to validate the prefix against a specific "type" of prefix, this subtly means you'll have to perform a string comparison.

Here's how more type-safe variants could look like, which I have implemented experimentally (**currently not included in the artifact**):

```java
TypeId<User> typeId = TypeId.generate(USER);
TypeId<User> anotherTypeId = TypeId.parse(USER, "user_01h455vb4pex5vsknk084sn02q");
```

The downside to this approach is that each possible prefix has to be defined manually as its own type that contains the prefix' string representation, e.g.:

```java
final class User implements TypedPrefix {
    @Override
    public String name() {
        return "user";
    }
}

static final User USER = new User();
```

Another solution is to validate the names of the prefix types at compile time. This solution is somewhat more complex as it requires an annotation processor.

```java
@TypeId(name = "UserId", prefix = "user")
class MyApp {}

UserId userId = UserId.generate();
UserId anotherUserId = UserId.parse("user_01h455vb4pex5vsknk084sn02q");
```

If I find the motivation, I will complete the experimental version and integrate it as a separate variant into its own package (e.g., `..typed`), which can be used alternatively.
 </details>

## Building From Source & Benchmarks
 <details>
    <summary>Details</summary>

```console
foo@bar:~$ git clone https://github.com/fxlae/typeid-java.git
foo@bar:~$ cd typeid-java
foo@bar:~/typeid-java$ ./gradlew build
```

There is a small [JMH](https://github.com/openjdk/jmh) microbenchmark included:
```console
foo@bar:~/typeid-java$ ./gradlew jmh
```

In a single-threaded run, all operations perform in the range of millions of calls per second, which should be sufficient for most use cases (used setup: Eclipse Temurin 17 OpenJDK Server VM, 2021 AMD mid-range notebook CPU). 

| method                         |  op/s |
|--------------------------------|------:|
| `TypeId.generate` + `toString` | 10.2M |
| `TypeId.parse`                 |  9.8M |

</details>