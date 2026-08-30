# Range Kata

Implement a generic `Range` class that represents a contiguous interval on a totally ordered domain (any type that
implements `Comparable`). This kata is inspired by the powerful `com.google.common.collect.Range` class from Google's
Guava library.

To create a `Range` instance, a developer will use various static factory methods to define its bounds (e.g., closed,
open). A `Range` can then be used to check if it contains a value, or to perform relational operations with other
`Range` instances (like intersection and span).

## Requirements

- Develop the `Range` class from level to level. Please note that you will only use the `Range` class to expose the
  public API. You can (and should) create helper classes or records to support your implementation internally, but they
  should not be directly instantiated in the test cases.
- You must add more tests in `RangeTest.java` as you progress through the levels. There are several failing tests
  written so that you can get started quickly for Level 1.
- All tests in `RangeTest` MUST pass.
- Each level MUST be a completed by a Git commit.
- Please commit **directly** to the `master` or `main` branch.
- Please avoid committing any IDE's specific files.

## Class `Range` _public_ API

```java
public final class Range<C extends Comparable<C>> {

    private Range(...) {
        // internal constructor
    }

    // Factory methods and operations will be added here
}
```

## Level 1 - Bound Types and `contains`

Mathematically, a `Range` can have bounds that are either inclusive (closed) or exclusive (open). Implement the
foundational factory methods and the `contains` operation for a generic `Comparable`.

```java
Range<Integer> closed = Range.closed(5, 7); // [5, 7]
closed.contains(4); // false
closed.contains(5); // true
closed.contains(7); // true

Range<Integer> open = Range.open(5, 7); // (5, 7)
open.contains(5); // false
open.contains(6); // true

Range<Integer> openClosed = Range.openClosed(5, 7); // (5, 7]
openClosed.contains(5); // false
openClosed.contains(7); // true

Range<Integer> closedOpen = Range.closedOpen(5, 7); //[5, 7)
closedOpen.contains(5); // true
closedOpen.contains(7); // false
```

The following constraints **MUST** be implemented:

- `Range` must be _immutable_.
- It is not allowed to create a `Range` where `lowerbound > upperbound`. Throw an `IllegalArgumentException` in this
  case.
- `Range` must work with any `Comparable` type (e.g., `String`, `LocalDate`).

## Level 2 - Unbounded Ranges

Extend your `Range` class to support open-ended (half-bounded or completely unbounded) intervals.

```java
Range<Integer> lessThanFive = Range.lessThan(5); // (-Infinity, 5)
lessThanFive.contains(4); // true
lessThanFive.contains(5); // false

Range<Integer> atMostFive = Range.atMost(5); // (-Infinity, 5]
atMostFive.contains(5); // true

Range<Integer> greaterThanFive = Range.greaterThan(5); // (5, +Infinity)
greaterThanFive.contains(5); // false
greaterThanFive.contains(6); // true

Range<Integer> atLeastFive = Range.atLeast(5); //[5, +Infinity)
atLeastFive.contains(5); // true

Range<Integer> all = Range.all(); // (-Infinity, +Infinity)
all.contains(Integer.MIN_VALUE); // true
all.contains(Integer.MAX_VALUE); // true
```

## Level 3 - `encloses`

Implement the `encloses` method. It returns `true` if the bounds of the _other_ range do not extend outside the bounds
of _this_ range.

```java
Range<Integer> base = Range.closed(3, 6); // [3, 6]

base.encloses(Range.closed(4, 5)); // true
base.encloses(Range.open(3, 6));   // true
base.encloses(Range.closedOpen(4, 7)); // false, 7 is outside

Range<Integer> unbounded = Range.atLeast(3); //[3, +Infinity)
unbounded.encloses(Range.closed(4, 100)); // true
```

## Level 4 - `isConnected` and `intersection`

Implement relational operations to see how two ranges overlap.

1. `isConnected(Range<C> other)`: Returns `true` if there exists a (possibly empty) range which is enclosed by both this
   range and the other. Equivalently, it checks if their union would form a single contiguous range.
2. `intersection(Range<C> other)`: Returns the maximal range enclosed by both this range and the other. If the ranges
   are not connected, this method should throw an `IllegalArgumentException`.

```java
Range<Integer> r1 = Range.closed(3, 5); // [3, 5]
Range<Integer> r2 = Range.open(5, 10);  // (5, 10)
Range<Integer> r3 = Range.closed(6, 10); // [6, 10]

r1.isConnected(r2); // true, because they touch at 5
r1.isConnected(r3); // false, gap exists between 5 and 6

Range<Integer> overlap1 = Range.closed(1, 5);
Range<Integer> overlap2 = Range.closed(3, 9);
Range<Integer> intersect = overlap1.intersection(overlap2);
// intersect is [3, 5]

Range<Integer> touch1 = Range.closed(1, 5);
Range<Integer> touch2 = Range.closedOpen(5, 9);
Range<Integer> intersectTouch = touch1.intersection(touch2);
// intersectTouch is[5, 5) - an empty range!
```

## Level 5 - `span`

Implement `span(Range<C> other)`. This method returns the minimal range that encloses both this range and the other
range. If the ranges are both finite, it spans from the lowest lower-bound to the highest upper-bound.

```java
Range<Integer> r1 = Range.closed(1, 3);
Range<Integer> r2 = Range.closed(5, 8);

Range<Integer> span = r1.span(r2);
// span is[1, 8]

Range<Integer> openSpan = Range.lessThan(5).span(Range.greaterThan(10));
// openSpan is (-Infinity, +Infinity) (i.e., Range.all())
```

## Level 6 - Formatting and Parsing

Implement a meaningful `toString()` method representing the bounds and types standard mathematical notation, using
`-Infinity` and `+Infinity` for unbounded ends.

Then, implement a static `parse(String rangeString, Function<String, C> valueParser)` method to reconstruct the `Range`.

```java
Range<Integer> range = Range.openClosed(1, 5);
assert range.toString().equals("(1, 5]");

Range<Integer> parsed = Range.parse("[3, +Infinity)", Integer::valueOf);
assert parsed.contains(3);
assert parsed.contains(999);
assert !parsed.contains(2);
```

## Level 7 - Range intersection as an HTTP API

It should be possible to calculate the intersection of two ranges via an HTTP API at `/api/range/intersection`.

This level tests your experience in working with a web application. The requirements are:

- The HTTP API is accessible at `/api/range/intersection`.
- The API should receive a JSON payload containing two strings representing integer ranges (e.g., `"[1, 10]"` and
  `"(5, 15)"`).
- The API should parse these ranges, compute the intersection, and return the resulting `Range` string.
- If the ranges are not connected, the API should return an appropriate HTTP error code (e.g., `400 Bad Request`) with
  an error message.

You are free to:

- Use any Java-based libraries or frameworks you are familiar with (e.g., Spring Boot).
- Decide the exact JSON schema.

Bonus Points:

- There is an accompanying Integration Test for this feature.
- There is an OpenAPI Specification (v3) for the HTTP API.

## Level 8 - Further Discussions

This level does not need implementation. The questions defined in this level are reserved for the upcoming Technical
Interview. You can take time to think of the answers and we will go through them in the interview.

a. In Level 1, we made `Range` accept any `Comparable<T>`. What happens if someone creates a `Range<Date>` and then
mutates one of the `Date` objects after the range is created? How would you design your API or document it to
prevent/handle this?

b. In Guava, there is a concept of a `DiscreteDomain`. For integers, the range `(3, 5)` strictly contains only the
integer `4`. Therefore, `(3, 5)` and `[4, 4]` are conceptually identical. If you were to add a
`Range.canonical(DiscreteDomain domain)` method to convert ranges to their most "closed" standard form, how would you
architect the `DiscreteDomain` interface and its implementation for `Integer`?

c. Notice how `intersection` between `[1, 5]` and `(5, 10)` results in `[5, 5)` (an empty range). What does it
conceptually mean for a range to be "empty"? Should `[5, 5)` be considered equal to `(6, 6]`?

d. If we wanted to track non-contiguous intervals (for example, the result of unioning `[1, 5]` and `[10, 15]`), a
single `Range` object is no longer sufficient. How would you design a `RangeSet` class to handle this, and how would its
internal data structure look to optimize for merging overlapping intervals automatically?
