# Scoring & Ranking

Mustang can score every matched criteria against the incoming event and return the **top-N highest-scoring** results. This is useful when you have more matches than you can act on — for example, showing only the 3 best-matching advertisements out of thousands.

---

## How scoring works

Scoring is defined per the VLDB 2009 paper. Each predicate carries an optional **weight**, and the score of a criteria measures how "relevant" it is to the incoming event.

### Conjunction score

For a conjunction `E` against an assignment `S`:

```
Score_conj(E, S) = Σ  w_E(A,v) × w_S(A,v)
                 (A,v) ∈ IN(E) ∩ S
```

Where:

- `IN(E)` = all `(attribute, value)` pairs from `∈` predicates of `E` (exclusions are **not** scored)
- `w_E(A,v)` = the **weight** on that predicate in the criteria
- `w_S(A,v)` = the **weight** on that key in the search context (defaults to 1.0 if not set)

Only keys that appear **both** in the criteria's inclusion predicates and in the event contribute to the score.

### Criteria-level score

| Form | Score formula |
|---|---|
| `DNFCriteria` | `max` of scores over all conjunctions |
| `CNFCriteria` | `sum` of scores over all disjunctions (returns `NO_MATCH` if any disjunction has score 0) |
| `UNFCriteria` (AND) | `sum` of scores of all sub-criteria and predicates |
| `UNFCriteria` (OR) | `max` of scores of all sub-criteria and predicates |

### `ExcludedPredicate` scoring

Excluded predicates contribute **0** to the score when satisfied (not `NO_MATCH`). A violated exclusion returns `NO_MATCH` and causes the entire criteria to score 0.

---

## Setting weights on predicates

Assign a `weight` to any `IncludedPredicate`:

```java
IncludedPredicate.builder()
    .lhs("$.interest")
    .weight(10L)    // high-value signal
    .detail(EqualityDetail.builder()
        .values(Sets.newHashSet("cricket"))
        .build())
    .build()

IncludedPredicate.builder()
    .lhs("$.country")
    .weight(3L)     // lower-value signal
    .detail(EqualityDetail.builder()
        .values(Sets.newHashSet("IN"))
        .build())
    .build()
```

If no weight is set, the predicate has weight 0 and does not contribute to scoring (but still participates in matching).

---

## Searching with scoring enabled

```java
// Scoring disabled (default) — faster, returns all matches unordered
Set<String> matches = engine.search("my-index", context);

// Scoring enabled — returns all matches, internally scored
Set<String> matches = engine.search("my-index", context, true);

// Top-N — returns only the N highest-scoring matches
Set<String> topMatches = engine.search("my-index", context, 5);
```

!!! tip "When to use top-N"
    Top-N search is **faster** than full scoring for large match sets, because the scoring upper-bound pruning in the algorithm can skip entire K-indexes once the N-th score threshold is established. The more matches there are, the bigger the speedup.

---

## Scoring a single criteria or a list

```java
// Score one criteria against a context
double score = engine.score(criteria, context);

// Score a list — returns List<Pair<criteriaId, score>>
List<Pair<String, Double>> scores = engine.score(criteriaList, context);
```

---

## Example: ad targeting with weighted scoring

```java
// Criteria: target cricket fans in India
Criteria ad = DNFCriteria.builder()
    .id("ad-cricket-india")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .weight(3L)
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("IN"))
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.interest")
            .weight(10L)
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("cricket", "sports"))
                .build())
            .build())
        .build())
    .build();

engine.add("ads", ad);

// User event
JsonNode event = mapper.readTree("""
    {"country": "IN", "interest": "cricket", "age": 28}
    """);
RequestContext ctx = RequestContext.builder().node(event).build();

// Score: w_E(country,IN)*1 + w_E(interest,cricket)*1 = 3 + 10 = 13
double score = engine.score(ad, ctx);  // → 13.0

// Top-3 ads
Set<String> best = engine.search("ads", ctx, 3);
```

---

## `RankingStrategy`

The engine accepts a `RankingStrategy` enum at construction time:

```java
MustangEngine engine = MustangEngine.builder()
    .mapper(mapper)
    .rankingStrategy(RankingStrategy.EXPLICIT_WEIGHTS)  // default
    .build();
```

Currently `EXPLICIT_WEIGHTS` is the supported strategy — scores are computed from predicate weights as described above.

---

## Upper-bound pruning (how top-N stays fast)

Each posting list head in the inverted index stores an **upper bound** `UB(A,v)` — the maximum weight any criteria assigns to the key `(A,v)`. During top-N search:

1. After accepting a match with score S, update the N-th threshold.
2. For any subsequent candidate, compute `sum of UB(A,v) × w_S(A,v)` over the K posting lists that could contribute.
3. If this upper bound < N-th threshold → skip the entire candidate without evaluating it.

This means that as more high-scoring matches are found, progressively more candidates can be pruned, making top-N searches faster on high-match workloads.
