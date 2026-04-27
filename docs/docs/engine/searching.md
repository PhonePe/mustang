# Searching

Mustang provides several ways to query criteria against an incoming event. All search operations accept a `RequestContext` wrapping a `JsonNode` event.

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.phonepe.mustang.common.RequestContext;

JsonNode event = mapper.readTree("{\"country\":\"IN\",\"age\":25}");
RequestContext context = RequestContext.builder().node(event).build();
```

---

## `search` — inverted-index search

The primary search method. Uses the inverted index (Conjunction or CNF algorithm) for sub-linear lookup.

```java
// Unscored — fastest, returns all matching IDs
Set<String> ids = engine.search("my-index", context);

// Scored — returns all matching IDs, internally computes relevance scores
Set<String> ids = engine.search("my-index", context, true);

// Unscored explicit
Set<String> ids = engine.search("my-index", context, false);

// Top-N — returns only the N highest-scoring IDs
Set<String> ids = engine.search("my-index", context, 5);
```

| Overload | Scoring | Returns |
|---|---|---|
| `search(index, ctx)` | Off | All matches |
| `search(index, ctx, false)` | Off | All matches |
| `search(index, ctx, true)` | On | All matches (ordered by relevance internally) |
| `search(index, ctx, N)` | On | Top-N matches only |

!!! tip "Unscored vs. scored"
    Unscored search skips weight computation entirely — use it when you only need to know *which* criteria matched, not how well. Top-N search is often *faster* than full scoring for large match sets because it prunes candidates using score upper bounds.

---

## `scan` — brute-force over an index

Evaluates every criteria in the named index one-by-one. Much slower than `search` for large indexes, but useful for:

- Cross-checking search results (see Ratification)
- Small in-memory indexes where indexing overhead is not worth it

```java
Set<String> ids = engine.scan("my-index", context);
```

---

## `scan` — evaluate an in-memory list

Evaluate a provided `List<Criteria>` directly, without any index:

```java
List<Criteria> criteriaList = List.of(c1, c2, c3);
List<Criteria> matching = engine.scan(criteriaList, context);
```

Returns the `Criteria` objects themselves (not just IDs).

---

## `evaluate` — single criteria check

Evaluate a single criteria against a context without any index lookup:

```java
boolean result = engine.evaluate(criteria, context);
```

Equivalent to `criteria.evaluate(context)`. No index required.

---

## `debug` — per-predicate breakdown

Get a detailed result showing which conjunctions/disjunctions and predicates matched or failed:

```java
DebugResult result = engine.debug(criteria, context);

result.isResult();                      // overall true/false
result.getId();                         // criteria ID
result.getForm();                       // DNF or CNF
result.getCompositionDebugResults();    // per-conjunction/disjunction breakdown
// Each CompositionDebugResult contains:
//   .isResult()               — whether this composition matched
//   .getPredicateDebugResults() — per-predicate detail
```

**Example output structure:**

```
DebugResult {
  id: "segment-001",
  form: DNF,
  result: true,
  compositionDebugResults: [
    {
      result: true,         ← first conjunction matched
      predicateResults: [
        { lhs: "$.country", result: true  },
        { lhs: "$.age",     result: true  }
      ]
    },
    {
      result: false,        ← second conjunction did not match
      predicateResults: [
        { lhs: "$.country", result: true  },
        { lhs: "$.platform",result: false }
      ]
    }
  ]
}
```

---

## `score` — compute relevance score

```java
// Score one criteria
double s = engine.score(criteria, context);

// Score a list — returns List<Pair<id, score>>
import org.apache.commons.lang3.tuple.Pair;
List<Pair<String, Double>> scores = engine.score(criteriaList, context);
```

See [Scoring & Ranking](../concepts/scoring.md) for the formula.

---

## JSONPath attribute references

The `lhs` field on every predicate is a **JSONPath** expression evaluated against the event `JsonNode`. Examples:

| JSONPath | Resolves to |
|---|---|
| `$.country` | Top-level field `country` |
| `$.user.age` | Nested field `age` inside `user` |
| `$.tags[0]` | First element of array `tags` |
| `$.meta.flags` | Nested object or array |

Paths are evaluated using [Jayway JsonPath](https://github.com/json-path/JsonPath). A missing path evaluates to `null`, which causes `IncludedPredicate` to return `false` and `ExcludedPredicate` to return `true` (WEAK-∉ semantics per the paper).
