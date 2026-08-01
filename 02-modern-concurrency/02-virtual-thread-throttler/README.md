# Virtual Thread Throttler kata

Implement a class `Throttler` to limit the concurrency of tasks executed by Java Virtual Threads.

With the introduction of Java 21, Virtual Threads are lightweight and cheap to create, meaning we no longer need to pool them. However, when Virtual Threads access a limited external resource (like a database connection pool or a rate-limited 3rd-party API), we still need to _throttle_ their concurrent execution.

To create a `Throttler` instance, you provide a `maxConcurrency` limit. The `Throttler` can then be used to safely execute tasks without exceeding this limit.

## Requirements

- Develop the `Throttler` class from level to level. Please note that you will only use the `Throttler` class to manage concurrency, you could create many classes to support your implementation but you should not use them in test cases.
- You must add more tests in `ThrottlerTest.java` as you progress through the levels. There are several failing tests written so that you can get started quickly for Level 1.
- All tests in `ThrottlerTest` MUST pass.
- You MUST utilize Java 21 Virtual Threads (`Thread.ofVirtual().start(...)` or `Executors.newVirtualThreadPerTaskExecutor()`) in your tests to simulate high concurrency.
- Each level MUST be a completed by a Git commit.
- Please commit **directly** to the `master` or `main` branch.
- Please avoid committing any IDE's specific files.

## Development

- Gradle 8.7.
- Java 21.

## Class `Throttler` _public_ API

```java
class Throttler {
  public static Throttler of(int maxConcurrency);

  public void execute(Runnable task);
}
```

## Level 1 - Basic Throttling

Class `Throttler` must restrict the number of concurrently executing tasks to `maxConcurrency`.

```java
Throttler apiThrottler = Throttler.of(2);

Runnable slowApiCall = () -> {
    System.out.println("Calling API...");
    Thread.sleep(1000);
};

// If 10 virtual threads call apiThrottler.execute(slowApiCall) at the same time,
// only 2 will execute immediately. The other 8 will block and wait
// until a spot becomes available.
apiThrottler.execute(slowApiCall);
```

The following constraints **MUST** be implemented:

- `Throttler` must provide a _static factory method_ namely `of(int)` to create a new instance.
- It is not allowed to create a `Throttler` with `maxConcurrency <= 0`.
- The method `execute(Runnable)` must block the calling thread (which will be a Virtual Thread) if the limit is reached, and resume when a concurrency slot opens up.
- The `Throttler` MUST guarantee that a slot is freed even if the `Runnable` throws a `RuntimeException`.

## Level 2 - Returning Results and Exceptions

A task often returns a value or throws a checked exception. Extend `Throttler` to support `Callable<T>`.

```java
Throttler throttler = Throttler.of(3);

// Returning a value
String result = throttler.submit(() -> {
    return "Data fetched";
});

// Handling Exceptions
try {
    throttler.submit(() -> {
        throw new IOException("Network error");
    });
} catch (Exception e) {
    // The throttler slot MUST be released before this catch block is reached!
}
```

- Add `<T> T submit(Callable<T> task) throws Exception`.
- Ensure that permits/slots are correctly released regardless of success or failure.

## Level 3 - Timeouts

Sometimes, a Virtual Thread shouldn't wait forever to acquire a slot. Introduce a way to attempt execution with a timeout.

```java
Throttler throttler = Throttler.of(1);

throttler.execute(() -> Thread.sleep(5000)); // Occupies the only slot

boolean executed = throttler.tryExecute(() -> {
    System.out.println("Will I run?");
}, Duration.ofSeconds(1));

// executed should be false because the task could not acquire a slot within 1 second.
```

- Add `boolean tryExecute(Runnable task, Duration timeout)`.
- If the slot is acquired within the `timeout`, execute the task and return `true`.
- If the `timeout` elapses before a slot is acquired, do not execute the task and return `false`.

## Level 4 - Metrics and `toString()`

Implement monitoring capabilities and a useful `toString()` method for the `Throttler`.

```java
Throttler throttler = Throttler.of(5);

int active = throttler.activeCount(); // Number of tasks currently running
int waiting = throttler.waitingCount(); // Number of threads waiting for a slot

assert throttler.toString().equals("");
```

- `activeCount()` must return the exact number of currently executing tasks.
- `waitingCount()` must return an _estimate_ (or exact number) of threads currently blocked waiting for a slot.
- `toString()` should be formatted as ``.

## Level 5 - Dynamic Resizing

In cloud environments, rate limits might change at runtime. Extend `Throttler` to support changing the `maxConcurrency` dynamically.

```java
Throttler throttler = Throttler.of(10);
// Currently max concurrency is 10

throttler.setMaxConcurrency(5);
// Now max concurrency is 5.
```

- If `setMaxConcurrency(int)` is called with a higher number, waiting tasks should immediately fill the new available slots.
- If it is called with a lower number, currently running tasks should NOT be interrupted. The `Throttler` will simply stop accepting new tasks until the `activeCount` drops below the new `maxConcurrency`.

## Level 6 - Weighted Tasks

Some tasks are heavier than others and should consume more "slots".

```java
Throttler throttler = Throttler.of(10);

// Consumes 5 slots out of 10 while running
throttler.execute(5, () -> {
    // Heavy DB query
});

// Consumes 1 slot
throttler.execute(1, () -> {
    // Light DB query
});
```

- Add `execute(int weight, Runnable task)` and `<T> T submit(int weight, Callable<T> task) throws Exception`.
- If a task requests a weight greater than the current `maxConcurrency`, it should throw an `IllegalArgumentException` immediately.

## Level 7 - `Throttler` as an HTTP API

It should be possible to monitor and test the `Throttler` via an HTTP API at `/api/throttler`.

This level tests your experience in working with a web application. The requirements are quite simple:

- The HTTP API is accessible at `/api/throttler/*`. Additional sub paths and/or path parameters are free to use.
- Create a single global `Throttler` instance in your web application (e.g., `maxConcurrency = 3`).
- Provide an endpoint `GET /api/throttler/metrics` that returns a JSON representation of the throttler's current state (active, waiting, max).
- Provide an endpoint `POST /api/throttler/execute` that accepts a payload containing a `durationInSeconds`. The endpoint should submit a task to the `Throttler` that sleeps for that duration, and return `200 OK` when the task finishes.
- If we hit the `POST` endpoint 10 times concurrently, we should see the `metrics` endpoint reflecting active and waiting threads accurately.

Your are free to:

- Use any Java-based libraries or frameworks which you are familiar with (e.g., Spring Boot, Quarkus, Javalin).
- Decide the details of the HTTP API.

Bonus Points:

- There is an accompanying Integration Test for this feature.
- There is an OpenAPI Specification (v3) for the HTTP API.

## Level 8 - Further Discussions

This level does not need implementation. The questions defined in this level are reserved for the upcoming Technical Interview. You can take time to think of the answers and we will go through them in the interview.

a. Prior to Java 21, throttling was typically done by submitting tasks to a bounded `ThreadPoolExecutor` (e.g., a fixed thread pool of size `N`). What are the primary advantages of using an unbounded number of Virtual Threads paired with a `Semaphore` (or your custom `Throttler`) instead of a traditional Thread Pool?

b. What is "Thread Pinning" in the context of Java Virtual Threads? If the `Runnable` passed into your `Throttler` executes a `synchronized` block and then performs a blocking I/O operation, how does this affect the underlying OS carrier threads?

c. In Level 5, you implemented dynamic resizing. If the limit is reduced from 10 to 5, but 10 tasks are currently active, what was your strategy to ensure the `Throttler` eventually reaches the new limit of 5 without prematurely rejecting valid tasks?

d. In Level 6 (Weighted Tasks), imagine a scenario where `maxConcurrency` is 10. There is an active task with weight 8. A new task arrives requesting weight 5, so it blocks. Then a task arrives requesting weight 1. Does the weight-1 task jump ahead of the weight-5 task? How does this impact fairness and starvation, and how might you mitigate it?
