# Getting Started

This guide takes you from zero to a running index and search in about 5 minutes.

---

## Prerequisites

- Java 17 or higher
- Maven 3.9+ (or equivalent Gradle)

---

## 1. Add the dependency

=== "Core only"

    ```xml
    <dependency>
      <groupId>com.phonepe</groupId>
      <artifactId>mustang-core</artifactId>
      <version>${mustang.version}</version>
    </dependency>
    ```

=== "With Dropwizard REST bundle"

    ```xml
    <dependency>
      <groupId>com.phonepe</groupId>
      <artifactId>mustang-dw-bundle</artifactId>
      <version>${mustang.version}</version>
    </dependency>
    ```

    The DW bundle transitively includes `mustang-core`.

> **Note:** Find the latest version on [Maven Central](https://search.maven.org/artifact/com.phonepe/mustang).

---

## 2. Create the engine

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.mustang.MustangEngine;

ObjectMapper mapper = new ObjectMapper();

MustangEngine engine = MustangEngine.builder()
    .mapper(mapper)
    .build();
```

`MustangEngine` is **thread-safe**. Create a single instance per application and reuse it — it manages all internal index state.

---

## 3. Define a criteria

A **criteria** is a Boolean expression in DNF or CNF form. It has a unique string ID and is composed of predicates.

### DNF — disjunction of conjunctions

Satisfied if **any** conjunction (AND-group) matches.

```java
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.detail.impl.EqualityDetail;
import com.phonepe.mustang.detail.impl.RangeDetail;
import com.google.common.collect.Sets;

// Rule: (country ∈ {IN, SG} AND age ∈ [18,35])
//     OR (country ∈ {US} AND platform ∉ {ios})
Criteria rule = DNFCriteria.builder()
    .id("segment-001")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("IN", "SG"))
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.age")
            .detail(RangeDetail.builder()
                .lowerBound(18).includeLowerBound(true)
                .upperBound(35).includeUpperBound(true)
                .build())
            .build())
        .build())
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("US"))
                .build())
            .build())
        .predicate(ExcludedPredicate.builder()
            .lhs("$.platform")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("ios"))
                .build())
            .build())
        .build())
    .build();
```

### CNF — conjunction of disjunctions

Satisfied if **all** disjunctions (OR-groups) match.

```java
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.composition.impl.Disjunction;

// Rule: (tier ∈ {premium} OR credits > 100) AND (region ∈ {APAC, EMEA})
Criteria plan = CNFCriteria.builder()
    .id("plan-gate-001")
    .disjunction(Disjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.tier")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("premium"))
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.credits")
            .detail(RangeDetail.builder()
                .lowerBound(100).includeLowerBound(false)
                .build())
            .build())
        .build())
    .disjunction(Disjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.region")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("APAC", "EMEA"))
                .build())
            .build())
        .build())
    .build();
```

---

## 4. Index the criteria

```java
// Single
engine.add("my-index", rule);

// Bulk (more efficient for large initial loads)
engine.add("my-index", List.of(rule, plan, otherCriteria));
```

An index is created automatically the first time you add to it. The index name is an arbitrary string — use it to namespace different rule sets.

---

## 5. Search

Build a `RequestContext` from your event JSON and call `search`:

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.phonepe.mustang.common.RequestContext;

// Event: user from India, age 25, on Android
JsonNode event = mapper.readTree("""
    {"country": "IN", "age": 25, "platform": "android"}
    """);

RequestContext context = RequestContext.builder()
    .node(event)
    .build();

// Returns IDs of all matching criteria
Set<String> matches = engine.search("my-index", context);
// → ["segment-001"]
```

`lhs` paths follow [JSONPath](https://goessner.net/articles/JsonPath/) syntax (`$.field`, `$.nested.field`, `$.array[0]`, etc.).

---

## 6. Evaluate a single criteria

If you just need to evaluate one criteria without indexing:

```java
boolean result = engine.evaluate(rule, context);
// → true
```

---

## 7. Debug

Get a per-predicate breakdown of why a criteria matched or didn't:

```java
DebugResult debug = engine.debug(rule, context);
// debug.isResult() → true/false
// debug.getCompositionDebugResults() → per-conjunction/disjunction detail
```

---

## What next?

| Topic | Link |
|---|---|
| All predicate types (Range, Regex, Versioning, Subset…) | [Predicates & Details](concepts/predicates.md) |
| Value transformations before matching | [PreOperations](concepts/preoperations.md) |
| Scoring and top-N ranked search | [Scoring & Ranking](concepts/scoring.md) |
| All engine API methods | [Indexing](engine/indexing.md) · [Searching](engine/searching.md) · [Advanced](engine/advanced.md) |
| Worked end-to-end examples | [Examples](examples/ad-targeting.md) |
| REST API via Dropwizard | [Dropwizard Bundle](dropwizard.md) |
| How the algorithm works | [The Algorithm](algorithm.md) |
