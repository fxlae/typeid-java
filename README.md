# typeid-java

![Build Status](https://github.com/fxlae/typeid-java/actions/workflows/build-on-push.yml/badge.svg) ![Maven Central](https://img.shields.io/maven-central/v/de.fxlae/typeid-java) ![License Info](https://img.shields.io/github/license/fxlae/typeid-java)

## A Java implementation of [TypeID](https://github.com/jetpack-io/typeid).

TypeIDs are a modern, type-safe, globally unique identifier based on the upcoming
UUIDv7 standard. They provide a ton of nice properties that make them a great choice
as the primary identifiers for your data in a database, APIs, and distributed systems.
Read more about TypeIDs in their [spec](https://github.com/jetpack-io/typeid).

## Installation

This library is designed to support all current LTS versions, including Java 8, whilst also making use of the features provided by the latest or upcoming Java versions. As a result, it is offered in two variants:

- `typeid-java`: Requires at least Java 17. Opt for this one if the Java version is not a concern
- *OR* `typeid-java-jdk8`: Supports all versions from Java 8 onwards. It handles all relevant use cases, albeit with less syntactic sugar

To install via Maven:

```xml
<dependency>
    <groupId>de.fxlae</groupId>
    <artifactId>typeid-java</artifactId> <!-- or 'typeid-java-jdk8' -->
    <version>0.2.0</version>
</dependency>
```

For installation via Gradle:

```kotlin
implementation("de.fxlae:typeid-java:0.2.0") // or ...typeid-java-jdk8:0.2.0
```

## Usage

`TypeId` instances can be obtained in several ways. They are immutable and thread-safe.

### Generating new TypeIDs

To generate a new `TypeId`, based on UUIDv7 as per specification:

```java
var typeId = TypeId.generate("user");
typeId.toString(); // "user_01h455vb4pex5vsknk084sn02q"
typeId.prefix(); // "user"
typeId.uuid(); // java.util.UUID(01890a5d-ac96-774b-bcce-b302099a8057), based on UUIDv7
```

To construct (or reconstruct) a `TypeId` from existing arguments, which can also be used as an "extension point" to plug-in custom UUID generators:

```java
var typeId = TypeId.of("user", UUID.randomUUID()); // a TypeId based on UUIDv4
```
### Parsing TypeID strings

For parsing, the library supports both an imperative programming model and a more functional style.
The most straightforward way to parse the textual representation of a TypeID:

```java
var typeId = TypeId.parse("user_01h455vb4pex5vsknk084sn02q");
```

Invalid inputs will result in an `IllegalArgumentException`, with a message explaining the cause of the parsing failure. If you prefer working with errors modeled as return values rather than exceptions, this is also possible (and is *much* more performant for untrusted input, as no stacktrace is involved at all):

```java
var maybeTypeId = TypeId.parseToOptional("user_01h455vb4pex5vsknk084sn02q"); 

// or, if you are interested in possible errors, provide handlers for success and failure
var maybeTypeId = TypeId.parse("...",
        Optional::of,   // (1) Function<TypeId, T>, called on success
        message -> {    // (2) Function<String, T>, called on failure
            log.warn("Parsing failed: {}", message);
            return Optional.empty();
        });
```
**Everything shown so far works for both artifacts, `typeid-java` as well as `typeid-java-jdk8`. The following section is about features that are only available when using `typeid-java`**.

When using `typeid-java`:
- the type `TypeId` is implemented as a Java `record`
- it has an additional method that *can* be used for parsing, `TypeId.parseToValidated`, which returns a "monadic-like" structure: `Validated<T>`, or in this particular context, `Validated<TypeId>`

`Validated<TypeId>` can be of subtype:
- `Valid<TypeId>`: encapsulates a successfully parsed `TypeId`
- or otherwise `Invalid<TypeId>`: contains an error message

A simplistic method to interact with `Validated` is to manually unwrap it, analogous to `java.util.Optional.get`:

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

A safe alternative involves methods that can be called without risk, namely:

- For transformations: `map`, `flatMap`, `filter`, `orElse`
- For implementing side effects: `ifValid` and `ifInvalid`

```java
// transform
var mappedToPrefix = TypeId.parseToValidated("user_01h455vb4pex5vsknk084sn02q");
    .map(TypeId::prefix)  // Validated<TypeId> -> Validated<String>
    .filter("Not a cat! :(", prefix -> !"cat".equals(prefix)); // the predicate fails

// execute side effects, e.g. logging
mappedToPrefix.ifValid(prefix -> log.info(prefix)) // called on success, so not in this case
mappedToPrefix.ifInvalid(message -> log.warn(message)) // logs "Not a cat! :("
```

`Validated<T>` and its implementations `Valid<T>` and `Invalid<T>` form a sealed type hierarchy. This feature becomes especially useful in future Java versions, beginning with Java 21, which will facilitate Record Patterns (destructuring) and Pattern Matching for switch:

```java
// this compiles and runs with oracle openjdk-21-ea+30 (preview enabled)

var report = switch(TypeId.parseToValidated("...")) {
    case Valid(TypeId(var prefix, var uuid)) when "user".equals(prefix) -> "user with UUID" + uuid;
    case Valid(TypeId(var prefix, _)) -> "Not a user, ignore the UUID. Prefix is " + prefix;
    case Invalid(var message) -> "Parsing failed :( ... " + message;
}
```
Note the absent (and superfluous) default case. Exhaustiveness is checked during compilation!

## But wait, isn't this less type-safe than it could be?
 <details>
    <summary>Details</summary>

That's correct. The prefix of a TypeId is currently just a simple `String`. If you want to validate the prefix against a specific "type" of prefix, this subtly means you'll have to perform a string comparison.

Here's how a more type-safe variant could look, which I have implemented experimentally (currently not included in the artifact):

```java
TypeId<User> typeId = TypeId<>.generate(USER);
TypeId<User> anotherTypeId = TypeId<>.parse(USER, "user_01h455vb4pex5vsknk084sn02q");
```

The downside to this approach is that each possible prefix type has to be defined manually. In particular, one must ensure that the embedded prefix name is syntactically correct:

```java
static final User USER = new User();
record User() implements TypedPrefix {
    @Override
    public String name() {
        return "user";
    }
}
```

This method would still be an improvement, as it allows `TypeId`s to be passed around in the code in a type-safe manner. However, the preferred solution would be to validate the names of the prefix types at compile time. This solution is somewhat more complex and might require, for instance, the use of an annotation processor.

If I find the motivation, I will complete the experimental version and integrate it as a separate variant into its own package (e.g., `..typed`), which can be used alternatively.
 </details>

## A word on UUIDv7
 <details>
    <summary>Details</summary>

TypeIDs are purposefully based on UUIDv7, one of several new UUID versions. UUIDs of version 7 begin with the current timestamp represented in the most significant bits, enabling their generation in a monotonically increasing order. This feature presents certain advantages, such as when using indexes in a database. Indexes based on B-Trees significantly benefit from monotonically ascending values.

However, the [IETF specification for the new UUID versions](https://datatracker.ietf.org/doc/html/draft-ietf-uuidrev-rfc4122bis) is a draft yet to be finalized, meaning modifications can still be introduced, including to UUIDv7. Additionally, the specification grants certain liberties in regards to the structure of a version 7 UUID. It must always commence with a timestamp (with a minimum precision of a millisecond, but potentially more if necessary), but in the least significant bits, aside from random values, it may or may not optionally include a counter and an InstanceId.

For these reasons, this library uses a robust implementation of UUIDs for Java (as its only runtime-dependency) , specifically [java-uuid-generator (JUG)](https://github.com/cowtowncoder/java-uuid-generator). It adheres closely to the specification and, for instance, utilizes `SecureRandom` for generating random numbers, as strongly recommended by the specification (see [section 6.8](https://datatracker.ietf.org/doc/html/draft-ietf-uuidrev-rfc4122bis#section-6.8) of the sepcification).

Nevertheless, as stated earlier, it is possible to use any other UUID generator implementation and/or UUID version by invoking `TypeId.of` instead of `TypeId.generate`.

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

In a single-threaded run, all operations perform in the range of millions of calls per second, which should be sufficient for most use cases (used setup: Eclipse Temurin 17 OpenJDK 64-Bit Server VM, AMD 2019gen CPU @ 3.6Ghz, 16GiB memory). 

| method                           |                  op/s |
|----------------------------------|----------------------:|
| `TypeId.generate` + `toString`   |                  9.1M |
| `TypeId.parse`                   |                  9.8M |

The library strives to avoid heap allocations as much as possible. The only allocations made are for return values and data from `SecureRandom`.
</details>