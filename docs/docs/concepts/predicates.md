# Predicates & Details

A **predicate** is the atomic unit of a criteria. It tests whether a single attribute in the incoming event satisfies a condition.

There are two predicate types:

| Type | Semantics | Java class |
|---|---|---|
| `IncludedPredicate` | Attribute **must satisfy** the detail condition (`∈`) | `IncludedPredicate` |
| `ExcludedPredicate` | Attribute **must NOT satisfy** the detail condition (`∉`) | `ExcludedPredicate` |

Every predicate has:

- **`lhs`** — a JSONPath expression pointing to the attribute in the event (e.g., `$.country`, `$.user.age`)
- **`detail`** — the caveat that defines what "satisfying the condition" means
- **`weight`** (optional) — a numeric weight used for scoring
- **`preOperations`** (optional) — a list of transformations applied to the value before evaluation

---

## Detail types

### `EqualityDetail` — exact value match

Tests whether the attribute value is a member of a given set.

```java
EqualityDetail.builder()
    .values(Sets.newHashSet("IN", "SG", "US"))
    .build()
```

Supports `String`, `Number`, `Boolean`.

**Example — country must be India or Singapore:**

```java
IncludedPredicate.builder()
    .lhs("$.country")
    .detail(EqualityDetail.builder()
        .values(Sets.newHashSet("IN", "SG"))
        .build())
    .build()
```

**Shorthand** — you can pass `values` directly without wrapping in `EqualityDetail`:

```java
IncludedPredicate.builder()
    .lhs("$.country")
    .values(Sets.newHashSet("IN", "SG"))   // implicit EqualityDetail
    .build()
```

---

### `RangeDetail` — numeric range

Tests whether the attribute value falls within a numeric range. Both bounds are optional — omitting a bound means open (unbounded).

```java
RangeDetail.builder()
    .lowerBound(18)
    .includeLowerBound(true)   // >=
    .upperBound(35)
    .includeUpperBound(true)   // <=
    .build()
```

| Scenario | Configuration |
|---|---|
| `x >= 18` | `lowerBound(18).includeLowerBound(true)` |
| `x > 18` | `lowerBound(18).includeLowerBound(false)` |
| `x < 100` | `upperBound(100).includeUpperBound(false)` |
| `x <= 100` | `upperBound(100).includeUpperBound(true)` |
| `18 <= x < 35` | both bounds, `includeLowerBound(true)`, `includeUpperBound(false)` |
| unbounded above | omit `upperBound` (defaults to `Double.MAX_VALUE`) |
| unbounded below | omit `lowerBound` (defaults to `Double.MIN_VALUE`) |

**Example — age between 18 and 35 inclusive:**

```java
IncludedPredicate.builder()
    .lhs("$.age")
    .detail(RangeDetail.builder()
        .lowerBound(18).includeLowerBound(true)
        .upperBound(35).includeUpperBound(true)
        .build())
    .build()
```

---

### `RegexDetail` — regular expression

Tests the attribute value against a Java regular expression.

```java
RegexDetail.builder()
    .regex("^[A-Z]{2}\\d+$")
    .build()
```

Only applies to `String` values. Uses Java's `java.util.regex` engine with the `rgxgen` library for expression validation.

**Example — phone number starting with +91:**

```java
IncludedPredicate.builder()
    .lhs("$.phone")
    .detail(RegexDetail.builder()
        .regex("^\\+91\\d{10}$")
        .build())
    .build()
```

---

### `VersioningDetail` — semantic version comparison

Compares the attribute value as a semantic version string against a base version, using Maven's `ComparableVersion` — which handles formats like `1.2.3`, `1.2.3-alpha`, `2.0.0.RC1`.

```java
VersioningDetail.builder()
    .check(CheckType.ABOVE)       // ABOVE | BELOW | EQUAL
    .baseVersion("3.2.0")
    .excludeBase(false)           // if true: strictly above/below (not equal to base)
    .build()
```

| `CheckType` | `excludeBase=false` | `excludeBase=true` |
|---|---|---|
| `ABOVE` | `version >= baseVersion` | `version > baseVersion` |
| `BELOW` | `version <= baseVersion` | `version < baseVersion` |
| `EQUAL` | `version == baseVersion` | (same) |

**Example — app version must be 3.2.0 or higher:**

```java
IncludedPredicate.builder()
    .lhs("$.appVersion")
    .detail(VersioningDetail.builder()
        .check(CheckType.ABOVE)
        .baseVersion("3.2.0")
        .build())
    .build()
```

---

### `SubSetDetail` — subset check

Tests whether the attribute (a collection) is a **subset** of the given set — i.e., every element in the attribute appears in the detail's value set.

```java
SubSetDetail.builder()
    .values(Sets.newHashSet("READ", "WRITE", "DELETE"))
    .build()
```

**Example — user permissions are a subset of {READ, WRITE}:**

```java
IncludedPredicate.builder()
    .lhs("$.permissions")
    .detail(SubSetDetail.builder()
        .values(Sets.newHashSet("READ", "WRITE"))
        .build())
    .build()
```

---

### `SuperSetDetail` — superset check

Tests whether the attribute (a collection) is a **superset** of the given set — i.e., every element in the detail's value set appears in the attribute.

```java
SuperSetDetail.builder()
    .values(Sets.newHashSet("ADMIN"))
    .build()
```

**Example — user must have at least the ADMIN role:**

```java
IncludedPredicate.builder()
    .lhs("$.roles")
    .detail(SuperSetDetail.builder()
        .values(Sets.newHashSet("ADMIN"))
        .build())
    .build()
```

---

### `EqualSetDetail` — exact set equality

Tests whether the attribute collection is **exactly equal** to the given set (same elements, no more, no less).

```java
EqualSetDetail.builder()
    .values(Sets.newHashSet("READ", "WRITE"))
    .build()
```

---

### `ExistenceDetail` — attribute must be present

Tests that the attribute path exists (is non-null) in the event.

```java
ExistenceDetail.builder().build()
```

**Example — event must have a `userId` field:**

```java
IncludedPredicate.builder()
    .lhs("$.userId")
    .detail(ExistenceDetail.builder().build())
    .build()
```

---

### `NonExistenceDetail` — attribute must be absent

Tests that the attribute path does **not** exist (is null or missing) in the event.

```java
NonExistenceDetail.builder().build()
```

---

## Caveat support matrix

| Caveat | String | Number | Boolean | Collection |
|---|---|---|---|---|
| `EQUALITY` | ✓ | ✓ | ✓ | — |
| `RANGE` | — | ✓ | — | — |
| `REGEX` | ✓ | — | — | — |
| `VERSIONING` | ✓ | — | — | — |
| `SUBSET` | — | — | — | ✓ |
| `SUPERSET` | — | — | — | ✓ |
| `EQUALSET` | — | — | — | ✓ |
| `EXISTENCE` | ✓ | ✓ | ✓ | ✓ |
| `NON_EXISTENCE` | ✓ | ✓ | ✓ | ✓ |

---

## `ExcludedPredicate` — negation

Any detail can be negated by wrapping it in an `ExcludedPredicate`. Evaluation is `!detail.validate(value)`.

```java
// platform must NOT be in {ios, windows}
ExcludedPredicate.builder()
    .lhs("$.platform")
    .detail(EqualityDetail.builder()
        .values(Sets.newHashSet("ios", "windows"))
        .build())
    .build()
```

!!! note "Scoring for exclusions"
    Exclusion predicates contribute **0** to the score (not `NO_MATCH`), since their satisfaction is implicit. Only inclusion predicates with explicit weights contribute to the relevance score.

---

## Combining predicates in compositions

### `Conjunction` (AND)

```java
Conjunction.builder()
    .predicate(p1)
    .predicate(p2)
    .predicate(p3)
    .build()
// satisfied iff p1 AND p2 AND p3
```

### `Disjunction` (OR)

```java
Disjunction.builder()
    .predicate(p1)
    .predicate(p2)
    .build()
// satisfied iff p1 OR p2
```

---

## UNFCriteria — unrestricted nesting

For complex compositions that go beyond pure DNF or CNF, `UNFCriteria` allows nesting arbitrary criteria trees:

```java
import com.phonepe.mustang.criteria.impl.UNFCriteria;
import com.phonepe.mustang.composition.CompositionType;

UNFCriteria complex = UNFCriteria.builder()
    .id("complex-rule")
    .type(CompositionType.AND)     // AND or OR at this level
    .criteria(subCriteria1)        // nested criteria
    .criteria(subCriteria2)
    .predicate(standaloneP1)       // predicates at this level
    .build();
```

!!! warning "UNF is not indexed"
    `UNFCriteria` is evaluated by `engine.evaluate()` or `engine.scan()`. It cannot be added to an inverted index via `engine.add()` — only `DNFCriteria` and `CNFCriteria` are indexable.
