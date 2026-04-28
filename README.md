# Mustang

[![CI](https://github.com/PhonePe/mustang/actions/workflows/maven.yml/badge.svg)](https://github.com/PhonePe/mustang/actions/workflows/maven.yml)
[![SonarCloud Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=PhonePe_mustang&metric=alert_status)](https://sonarcloud.io/dashboard?id=PhonePe_mustang)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=PhonePe_mustang&metric=coverage)](https://sonarcloud.io/dashboard?id=PhonePe_mustang)
[![Maven Central](https://img.shields.io/maven-central/v/com.phonepe/mustang-core.svg)](https://search.maven.org/artifact/com.phonepe/mustang-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-blue.svg)](https://openjdk.org/projects/jdk/17/)

Mustang solves the problem of efficiently indexing Boolean expressions (both **Disjunctive Normal Form (DNF)** and **Conjunctive Normal Form (CNF)**) in a high-dimensional multi-valued attribute space. The goal is to rapidly find the set of Boolean expressions that evaluate to true for a given assignment of values to attributes.

Applications include:
- **Online advertising** — Boolean expressions represent advertiser targeting criteria; attribute assignments represent visiting users.
- **General targeting systems** — Any system where rules/criteria must be matched against incoming events at scale.

Mustang uses an inverted list data structure adapted from ranked information retrieval, enabling it to efficiently return the **top-N matching expressions** — a critical capability in systems with ranked advertising, notifications, or personalization.

---

## Modules

| Module | Description |
|---|---|
| `mustang-models` | Domain model: `Criteria`, `Predicate`, `Detail`, `PreOperation`, scoring, ratification types |
| `mustang-core` | Core indexing & search engine (`MustangEngine`) |
| `mustang-dw-bundle` | Optional Dropwizard bundle for exposing a REST API over the engine |

---

## Maven Dependency

### Core engine only

```xml
<dependency>
  <groupId>com.phonepe</groupId>
  <artifactId>mustang-core</artifactId>
  <version>${mustang.version}</version>
</dependency>
```

### With Dropwizard REST bundle

```xml
<dependency>
  <groupId>com.phonepe</groupId>
  <artifactId>mustang-dw-bundle</artifactId>
  <version>${mustang.version}</version>
</dependency>
```

> **Note:** Find the latest version on [Maven Central](https://search.maven.org/artifact/com.phonepe/mustang).

---

## Overview

### Criteria forms

`Criteria` represents a Boolean expression in one of two normalized forms:

- **DNF** (Disjunctive Normal Form) — disjunction of conjunctions
  `(A ∈ {a1,a2} ∧ B ∉ {b1,b2}) ∨ (A ∈ {a1,a3} ∧ D ∉ {d1})`

- **CNF** (Conjunctive Normal Form) — conjunction of disjunctions
  `(A ∈ {a1,a2} ∨ B ∉ {b1,b2}) ∧ (A ∈ {a1,a3} ∨ D ∉ {d1})`

### Compositions and predicates

`Composition` is a set of `Predicate`s:
- `Conjunction (∧)` — satisfied only when **all** constituent predicates evaluate to `true`
- `Disjunction (∨)` — satisfied when **any** constituent predicate evaluates to `true`

`Predicate` is a conditional:
- `IncludedPredicate` — inclusion (∈)
- `ExcludedPredicate` — exclusion (∉)

### Detail types

`Detail` carries the caveat that a predicate enforces:

| `Detail` | `Caveat` |
|---|---|
| `EqualityDetail` | `EQUALITY` |
| `RegexDetail` | `REGEX` |
| `RangeDetail` | `RANGE` (gt, gte, lt, lte, between — open & closed) |
| `VersioningDetail` | `VERSIONING` |
| `SubSetDetail` | `SUBSET` |
| `SuperSetDetail` | `SUPERSET` |
| `EqualSetDetail` | `EQUALSET` |
| `ExistenceDetail` | `EXISTENCE` (attribute must be present) |
| `NonExistenceDetail` | `NON_EXISTENCE` (attribute must be absent) |

### Caveat support across data types

| `Caveat` | Data Types |
|---|---|
| `EQUALITY` | String, Number, Boolean |
| `SUBSET` | Collections (List, Set) |
| `SUPERSET` | Collections (List, Set) |
| `EQUALSET` | Collections (List, Set) |
| `REGEX` | String |
| `RANGE` | Number |
| `VERSIONING` | String |
| `EXISTENCE` | Any |
| `NON_EXISTENCE` | Any |

### PreOperations

`PreOperation`s allow lightweight transformations on attribute values before evaluation:

| PreOperation | Description |
|---|---|
| `SubStringPreOperation` | Extract substring by index range |
| `ModuloPreOperation` | Compute `value % rhs` |
| `DivisionPreOperation` | Compute `value / rhs` |
| `MultiplicationPreOperation` | Compute `value * rhs` |
| `AdditionPreOperation` | Compute `value + rhs` |
| `SubtractionPreOperation` | Compute `value - rhs` |
| `LengthPreOperation` | Returns string/collection length |
| `SizePreOperation` | Returns collection size |
| `BinaryConversionPreOperation` | Convert number to binary string |
| `DateTimePreOperation` | Extract date/time component |

---

## Usage

### Initialize the engine

```java
ObjectMapper mapper = new ObjectMapper();
MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
```

### Define a DNF criteria

```java
Criteria dnf = DNFCriteria.builder()
    .id("C1")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.a")
            // Optional: transform the attribute value before matching
            .preOperation(SubStringPreOperation.builder().beginIndex(1).endIndex(2).build())
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("A1", "A2", "A3"))
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.n")
            .detail(RangeDetail.builder()
                .lowerBound(3)
                .includeLowerBound(true) // n >= 3
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.x")
            .preOperation(ModuloPreOperation.builder().rhs(4).build())
            .detail(RangeDetail.builder()
                .upperBound(3) // (x % 4) < 3
                .build())
            .build())
        .build())
    .build();
```

### Define a CNF criteria

```java
Criteria cnf = CNFCriteria.builder()
    .id("C2")
    .disjunction(Disjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.a")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("A1", "A2"))
                .build())
            .build())
        .predicate(ExcludedPredicate.builder()
            .lhs("$.b")
            .detail(RegexDetail.builder()
                .regex("B.?")
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.v")
            .detail(VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("1.2.3.4-alpha")
                .build())
            .build())
        .build())
    .build();
```

### Index criteria

```java
// Single
engine.add("my_index", criteria);

// Bulk
engine.add("my_index", Arrays.asList(criteria1, criteria2, criteria3));
```

### Search

```java
// Build the evaluation context from a JSON event
JsonNode event = mapper.readTree("{\"a\":\"A1\",\"b\":\"B3\",\"n\":5,\"p\":true}");
EvaluationContext context = EvaluationContext.builder().node(event).build();

// Search (with scoring/ranking)
Set<String> results = engine.search("my_index", context);

// Search (skip scoring for raw speed)
Set<String> results = engine.search("my_index", context, false);
```

### Update and delete

```java
engine.update("my_index", updatedCriteria);  // upsert semantics
engine.delete("my_index", criteria);
```

### Scan (evaluate a list in-memory)

```java
List<Criteria> matching = engine.scan(criteriaList, context);
```

### Evaluate a single criteria

```java
boolean result = engine.evaluate(criteria, context);
```

### Index replacement (atomic swap)

```java
// Build a fresh index under a temp name, then atomically replace
engine.add("my_index_new", allCriteria);
engine.replace("my_index", "my_index_new");
```

### Ratification (anomaly detection)

```java
engine.ratify("my_index");  // run in background

// Poll for results
RatificationResult result = engine.getRatificationResult("my_index");
```

### Similarity detection

```java
SimilarityStats stats = engine.checkSimilarity("my_index", candidateCriteria);
```

### Export / Import (for debugging)

```java
String exported = engine.exportIndexGroup("my_index");
engine.importIndexGroup(exported);
Set<String> results = engine.search("my_index", context);
```

### Top-N ranked search

Supply weights on predicates and the engine scores criteria based on overlap with the assignment:

```java
// Score_conj(E, S) = Σ w_E(A,v) * w_S(A,v)  for (A,v) ∈ IN(E) ∩ S
// DNFCriteria score = max over conjunctions
// CNFCriteria score = sum over disjunctions
```

---

## Dropwizard Bundle

`mustang-dw-bundle` exposes read-only REST endpoints over a `MustangEngine` instance:

```java
// In your Application class
bootstrap.addBundle(new MustangBundle<>(config -> config.getMustangConfig()));
```

Endpoints include `/mustang/search`, `/mustang/scan`, `/mustang/debug`, and `/mustang/ratify`.

---

## Backward Compatibility

`3.x` is fully backward-compatible with `2.x`. Necessary transformations are applied transparently. All users on `2.x` are encouraged to upgrade.

---

---

## Documentation

Full documentation is available at the [project docs site](https://phonepe.github.io/mustang/).

To build the docs locally:

```bash
cd docs
pip install -r requirements.txt
zensical build --clean
# Output is in docs/site/
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Code of Conduct

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

Distributed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

## Academic Reference

The core indexing algorithm is based on:  
**"Indexing Boolean Expressions"** — Proceedings of VLDB 2009. See [`reference/vldb09-indexing.pdf`](reference/vldb09-indexing.pdf).
