# Custom Collector kata

Implement a utility class `MyCollectors` that provides several custom implementations of the
`java.util.stream.Collector` interface.

To use these collectors, a developer will pass them into the `.collect()` terminal operation of a Java `Stream`. These
custom collectors will solve common data aggregation problems that the standard `java.util.stream.Collectors` utility
class does not cover out of the box.

## Requirements

- Develop the `MyCollectors` class from level to level. Please note that you will only use the `MyCollectors` class to
  expose the static factory methods. You can (and should) create many helper classes/records to support your
  implementation (e.g., custom Accumulator classes), but they should not be directly instantiated in the test cases.
- You must add more tests in `MyCollectorsTest.java` as you progress through the levels. There are several failing tests
  written so that you can get started quickly for Level 1.
- All tests in `MyCollectorsTest` MUST pass.
- Each level MUST be completed by a Git commit.
- Please commit **directly** to the `master` or `main` branch.
- Please avoid committing any IDE's specific files.

## Development

- Gradle 8.7.
- Java 21.

## Class `MyCollectors` *public* API

```java
public final class MyCollectors {

    private MyCollectors() {
        // prevent instantiation
    }

    // Factory methods will be added here
}
```

## Level 1 - Batching Elements

The standard JDK does not have a built-in collector to chunk a stream into batches of a specific size. Implement
`toBatches(int batchSize)`.

```java
List<List<Integer>> batches = Stream.of(1, 2, 3, 4, 5)
        .collect(MyCollectors.toBatches(2));

// batches contains: [,,]
```

The following constraints **MUST** be implemented:

- `MyCollectors.toBatches(int batchSize)` must return a `Collector<T, ?, List<List<T>>>`.
- If `batchSize <= 0`, it must throw an `IllegalArgumentException`.
- The final batch may be smaller than `batchSize` if the total number of elements is not a perfect multiple of
  `batchSize`.
- The order of elements must be preserved.

## Level 2 - Number Statistics

Standard `Collectors.summarizingInt` returns an `IntSummaryStatistics`. We want to build our own, more extensible
version called `Stats`, capable of capturing count, sum, min, max, and exact average as a `double`.

```java
class Stats {
    public long count();

    public long sum();

    public int min();

    public int max();

    public double average();
}
```

Implement `toStats()` for streams of `Integer`.

```java
Stats stats = Stream.of(1, 5, 10)
        .collect(MyCollectors.toStats());

stats.

count(); // 3
stats.

sum(); // 16
stats.

min(); // 1
stats.

max(); // 10
stats.

average(); // 5.333333333333333
```

- If the stream is empty, `count` and `sum` should be `0`. `min` and `max` should be `0`, and `average` should be `0.0`.

## Level 3 - Make it generic with Top N elements

Provide a collector that retains only the "Top N" elements of a stream based on a provided `Comparator<T>`.

```java
Collector<String, ?, List<String>> topTwoLongest = MyCollectors.topN(
        2,
        Comparator.comparingInt(String::length).reversed()
);

List<String> result = Stream.of("apple", "kiwi", "banana", "pear", "watermelon")
        .collect(topTwoLongest);

// result contains:
```

- If the stream has fewer elements than `N`, it returns all elements sorted by the comparator.
- Memory constraint: The internal state (accumulator) should ideally not store all elements of an exceptionally large
  stream if we only need the top N elements.

## Level 4 - Partitioning with a Custom Result Type

The built-in `Collectors.partitioningBy` returns a `Map<Boolean, List<T>>` which is often awkward to read. Implement
`splitBy(Predicate<T>)` which returns a custom `SplitResult<T>` interface/record.

```java
interface SplitResult<T> {
    List<T> matched();

    List<T> unmatched();
}
```

```java
SplitResult<Integer> split = Stream.of(1, 2, 3, 4, 5, 6)
        .collect(MyCollectors.splitBy(n -> n % 2 == 0));

split.

matched(); //
split.

unmatched(); //
```

## Level 5 - The Finisher: Filtered Frequency Map

Implement a frequency map collector that excludes elements appearing fewer times than a given threshold. This will
require manipulating the `finisher` function of the Collector to filter the final map.

```java
Map<String, Long> frequencies = Stream.of("a", "b", "a", "c", "a", "b")
        .collect(MyCollectors.toFrequency(2));

// frequencies contains:
// "a" -> 3
// "b" -> 2
// "c" is completely missing because it only appeared once (1 < 2).
```

## Level 6 - Parse a Collector Strategy

It should be possible to dynamically create a `Collector<String, ?, String>` based on a string literal configuration.

This level tests your API design and parsing skills. Please decide the signature of the factory method
`parseStringCollector(String config)` yourself.

```java
// "join:," -> joins elements with a comma
Collector<String, ?, String> commaJoiner = MyCollectors.parseStringCollector("join:,");
assert Stream.

of("A","B","C").

collect(commaJoiner).

equals("A,B,C");

// "concat" -> simply concatenates
Collector<String, ?, String> concater = MyCollectors.parseStringCollector("concat");
assert Stream.

of("A","B","C").

collect(concater).

equals("ABC");

// "uppercase-join:|" -> transforms to uppercase, then joins
Collector<String, ?, String> upperJoiner = MyCollectors.parseStringCollector("uppercase-join:|");
assert Stream.

of("a","b","c").

collect(upperJoiner).

equals("A|B|C");
```

## Level 7 - Collectors as an HTTP API

It should be possible to utilize the logic from `toStats()` and `toBatches()` via an HTTP API at `/api/collect/*`.

This level tests your experience in working with a web application. The requirements are quite simple:

- The HTTP API is accessible at `/api/collect/*`. Additional sub-paths and/or path parameters are free to use.
- The HTTP API should receive a JSON payload containing an array of integers.
- Based on the endpoint or query parameters, the server should process the array using the custom collectors you wrote (
  e.g., `toBatches` or `toStats`) and return the resulting data structure as a JSON response.

You are free to:

- Use any Java-based libraries or frameworks with which you are familiar (Spring Boot, Quarkus, Javalin, etc.).
- Decide the details of the HTTP API (e.g., HTTP method, exact JSON schema). It is not required at all to make it
  strictly RESTful.

Bonus Points:

- There is an accompanying Integration Test for this feature.
- There is an OpenAPI Specification (v3) for the HTTP API.

## Level 8 - Further Discussions

This level does not need implementation. The questions defined in this level are reserved for the upcoming Technical
Interview. You can take time to think of the answers and we will go through them in the interview.

a. In Level 1 (`toBatches`), the built-in streams framework can execute operations in parallel using `.parallel()`. How
does your `toBatches` collector behave with a parallel stream? What changes (if any) are required to make it
thread-safe, and what are the performance implications?

b. Is it possible for a `Collector` to "short-circuit" a stream? For example, if we create a collector
`MyCollectors.first10()`, can the collector tell an infinite stream to stop generating elements once 10 items are
collected? If not, how would you solve this requirement using the Java Stream API?

c. Let's look at Level 3 (`topN`). If the stream contains 1 billion records and we want `topN(10)`, an inefficient
implementation might collect 1 billion elements into a list and then sort it, causing an `OutOfMemoryError`. How did you
design the internal accumulator to prevent this?

d. In Level 2, `toStats()` relies on an internal accumulator to sum up values. If dealing with floating point numbers (
e.g., `DoubleStream`), simply adding numbers together can lead to a loss of precision. How might you redesign the
aggregator to mitigate floating-point arithmetic errors?

