# Benchmarks

Mustang includes a [JMH](https://github.com/openjdk/jmh) micro-benchmark suite under the `benchmarking/` module. This page explains what is measured, how to run benchmarks, and what the results mean.

---

## What is measured

Three benchmark classes cover the main search scenarios:

### `MustangSearchBenchmark` — mixed DNF + CNF index

Indexes both DNF and CNF criteria and measures throughput under a random event stream.

- **Index**: equal mix of DNF and CNF criteria
- **DNF criteria**: 1 conjunction, 1–3 predicates (80% inclusion, 20% exclusion)
- **CNF criteria**: 1 disjunction, 1–2 predicates (80% inclusion, 20% exclusion)
- **Event**: random subset of 26 attributes (`a`–`z`), each with a random integer value 0–99

### `MustangOnlyCNFSearchBenchmark` — CNF-only index

Same setup but indexes only CNF criteria with up to 5 predicates per disjunction. Isolates CNF algorithm performance.

### `MustangOnlyDNFSearchBenchmark` — DNF-only index

Same setup but indexes only CNF criteria structured as single-disjunction expressions. Isolates the conjunction algorithm performance under purely disjunctive criteria.

---

## JMH configuration

All benchmarks share these annotations:

| Annotation | Value | Meaning |
|---|---|---|
| `@BenchmarkMode` | `Throughput` | Measures operations per time unit (ops/s) |
| `@Fork` | `value=1, warmups=1` | 1 warmup fork + 1 measurement fork |
| `@Warmup` | `iterations=1` | 1 warmup iteration per fork |
| `@Measurement` | `iterations=1` | 1 measurement iteration |
| `@Threads` | `Threads.MAX` | All available hardware threads |

**Index sizes tested** (parameterized via `@Param`):

```
10, 100, 1000, 10000
```

Each benchmark runs at all four index sizes in a single JMH run.

---

## Index generation details

The `Utils` class defines the attribute space:

```java
// 26 attributes: "a" through "z"
List<String> PATHS = ["a", "b", "c", ..., "z"]

// Random integer values in [0, 99]
int getRandom() { return RANDOM.nextInt(100); }
```

For each indexed criteria:

- Attribute paths are **shuffled** for each new criteria → randomizes which attributes appear
- Predicates sample a random 1–3 (DNF) or 1–5 (CNF) attributes from the shuffled list
- Each predicate has an 80% chance of being `IncludedPredicate`, 20% `ExcludedPredicate`

For each search:

- A fresh event is generated **per invocation** (`@Setup(Level.Invocation)`)
- The event contains a random subset of attributes with random values

---

## Building and running

```bash
# Build the benchmarks JAR
cd benchmarking
mvn clean package -DskipTests

# Run all benchmarks
java -jar target/benchmarks.jar

# Run a specific benchmark
java -jar target/benchmarks.jar MustangSearchBenchmark

# Run with specific index size only
java -jar target/benchmarks.jar -p indexSize=1000

# Run with more measurement iterations for stable results
java -jar target/benchmarks.jar -wi 3 -i 5 -f 2
```

---

## Profiling

Attach a profiler to identify hot spots:

=== "async-profiler (CPU)"

    ```bash
    java -agentpath:/path/to/libasyncProfiler.so=start,event=cpu,file=profile.html \
         -jar target/benchmarks.jar MustangSearchBenchmark
    ```

    Opens `profile.html` as a flame graph in any browser.

=== "async-profiler (allocation)"

    ```bash
    java -agentpath:/path/to/libasyncProfiler.so=start,event=alloc,file=alloc.html \
         -jar target/benchmarks.jar
    ```

=== "YourKit"

    ```bash
    java -agentpath:/Applications/YourKit-Java-Profiler-2021.11.app/Contents/Resources/bin/mac/libyjpagent.dylib=delay=10000,listen=all \
         -jar target/benchmarks.jar
    ```

---

## Interpreting results

JMH output looks like:

```
Benchmark                              (indexSize)  Mode  Cnt       Score   Error  Units
MustangSearchBenchmark.search               10     thrpt    1  123456.789          ops/s
MustangSearchBenchmark.search              100     thrpt    1   98765.432          ops/s
MustangSearchBenchmark.search             1000     thrpt    1   45678.901          ops/s
MustangSearchBenchmark.search            10000     thrpt    1   12345.678          ops/s
```

Key points:

- **`ops/s`** = searches per second across all threads combined
- Throughput typically **decreases sub-linearly** as index size increases — this sub-linear scaling is the key property of the inverted-index algorithm
- The random 80/20 inclusion/exclusion ratio and random event generation simulate a realistic workload
- For rigorous comparisons, increase `-wi` (warmup iterations) to ≥ 5 and `-i` (measurement iterations) to ≥ 10 with `-f 3` forks

---

̉

## Adding a new benchmark

1. Create a new class in `benchmarking/src/main/java/com/phonepe/mustang/benchmark/benchmarks/`
2. Add a `@State` class with `@Setup` to build the index
3. Add a `@Benchmark` method that calls `engine.search(...)` and consumes the result via `Blackhole`
4. Rebuild and run with `java -jar target/benchmarks.jar YourNewBenchmark`

```java
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Threads(Threads.MAX)
public class MyBenchmark {

    @State(Scope.Benchmark)
    public static class Ctx {
        @Param({"1000", "10000"})
        int indexSize;

        MustangEngine engine;
        RequestContext context;

        @Setup(Level.Trial)
        public void setUp() { /* build index */ }

        @Setup(Level.Invocation)
        public void buildContext() { /* generate event */ }
    }

    @Benchmark
    public void search(Blackhole bh, Ctx ctx) {
        bh.consume(ctx.engine.search("PERF", ctx.context, false));
    }
}
```
