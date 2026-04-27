# Mustang Benchmarks

JMH throughput benchmarks for the [Mustang](https://github.com/PhonePe/Mustang) criteria-indexing engine.

## What is benchmarked

Three benchmark classes measure search throughput under different index compositions, all using `@BenchmarkMode(Throughput)` and `@Threads(MAX)` to simulate concurrent production load.

| Benchmark | Index contents | What it measures |
|---|---|---|
| `MustangSearchBenchmark` | Mixed DNF + CNF criteria | Throughput of a real-world index where both forms coexist |
| `MustangOnlyDNFSearchBenchmark` | CNF criteria only | Isolated DNF search path performance |
| `MustangOnlyCNFSearchBenchmark` | DNF criteria only | Isolated CNF search path performance |

Each benchmark is parameterised over index sizes of **10, 100, 1000 and 10 000** criteria, giving 4 × 3 = 12 data points per run.

### Index generation

- 26 attributes (`a`–`z`), values randomly drawn from `[0, 100)`
- 80% `IncludedPredicate`, 20% `ExcludedPredicate` per conjunction / disjunction
- Each `RequestContext` (the incoming event) is reshuffled per invocation

## Prerequisites

`mustang-core` is not yet published to Maven Central. Build and install it from the project root first:

```bash
mvn install -f ../pom.xml -DskipTests
```

## Build

```bash
mvn clean package -DskipTests
```

This produces `target/benchmarks.jar` — a self-contained uber-JAR with JMH's `Main` as the entry point.

## Run

**All benchmarks:**
```bash
java -jar target/benchmarks.jar
```

**A specific benchmark:**
```bash
java -jar target/benchmarks.jar MustangSearchBenchmark
```

**Specific index size only:**
```bash
java -jar target/benchmarks.jar -p indexSize=1000
```

## Profiling

**async-profiler** (CPU flamegraph):
```bash
java -agentpath:/<path-to-async-profiler>/build/libasyncProfiler.so=start,event=cpu,file=profile.html \
     -jar target/benchmarks.jar
```

**YourKit:**
```bash
java -agentpath:/Applications/YourKit-Java-Profiler-2021.11.app/Contents/Resources/bin/mac/libyjpagent.dylib=delay=10000,listen=all \
     -jar target/benchmarks.jar
```
