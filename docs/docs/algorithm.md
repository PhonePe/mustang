# The Algorithm

Mustang's indexing and search algorithms are based on the paper:

> **"Indexing Boolean Expressions"**
> Whang, Garcia-Molina (Stanford); Brower, Shanmugasundaram, Vassilvitskii, Vee, Yerneni (Yahoo! Research)
> *VLDB 2009*

This page explains the algorithm in detail, with concrete examples showing exactly how the Java implementation corresponds to the paper.

---

## The Problem

Given:

- A large collection of **Boolean Expressions (BEs)** — e.g., advertiser targeting criteria, pub/sub subscriptions
- An incoming **assignment** — e.g., a user's attributes: `{country=IN, age=28, platform=android}`

Find all BEs that evaluate to `true` for the assignment.

The naive approach evaluates each BE one-by-one: O(N × P) where N = number of BEs and P = average predicates per BE. At 1 million BEs this is too slow.

---

## Core insight: inverted lists over expressions

Inverted lists are classically used to search *documents* given a *query*. This paper inverts the problem: search for *queries (BEs)* given *data (the assignment)*. The key insight is that a conjunction of `∈` predicates is satisfied by an assignment only if the assignment contains *every* (attribute, value) pair in the conjunction. This is structurally analogous to finding a document that contains all query keywords.

---

## Terminology

| Term | Meaning |
|---|---|
| **Key** | An `(attribute, value)` pair, e.g., `(country, IN)` |
| **Assignment** | The incoming event — a set of keys with values |
| **Conjunction** | A set of `∈`/`∉` predicates joined by AND |
| **Disjunction** | A set of predicates joined by OR |
| **K** | The *size* of a conjunction = number of `∈` predicates (ignoring `∉`) |
| **K-index** | The partition of the inverted index containing all conjunctions of size K |
| **Posting list** | A sorted list of conjunction/CNF entries for a given key |
| **Z list** | A special posting list in K=0 that catches size-0 conjunctions |

---

## DNF Algorithm

### Index Construction

DNF criteria are first split into individual conjunctions. For example:

```
E = (age ∈ {3} ∧ state ∉ {CA}) ∨ (age ∈ {3} ∧ gender ∈ {F})
```

splits into:

| ID | Expression | Size (K) |
|---|---|---|
| c1 | `age ∈ {3} ∧ state ∈ {NY}` | 2 |
| c2 | `age ∈ {3} ∧ gender ∈ {F}` | 2 |
| c3 | `age ∈ {3} ∧ gender ∈ {M} ∧ state ∉ {CA}` | 2 |
| c4 | `state ∈ {CA} ∧ gender ∈ {M}` | 2 |
| c5 | `age ∈ {3,4}` | 1 |
| c6 | `state ∉ {CA,NY}` | 0 |

Conjunctions are partitioned into K-indexes. For each K-index, a posting list is created for every key that appears in any conjunction of that size:

```
K=2:
  (state,NY)  → [(c1, ∈)]
  (age,3)     → [(c1, ∈), (c2, ∈), (c3, ∈)]
  (gender,F)  → [(c2, ∈)]
  (state,CA)  → [(c3, ∉), (c4, ∈)]
  (gender,M)  → [(c3, ∈), (c4, ∈)]

K=1:
  (age,3)     → [(c5, ∈)]
  (age,4)     → [(c5, ∈)]

K=0:
  (state,CA)  → [(c6, ∉)]
  (state,NY)  → [(c6, ∉)]
  Z           → [(c6, ∈)]    ← ensures c6 appears in at least one list
```

Each entry stores:

1. The conjunction ID
2. `∈` or `∉` annotation
3. An optional weight (for scoring)

Entries within a posting list are sorted by conjunction ID. Within the same conjunction ID across lists, `∉` entries sort before `∈` entries.

### The Conjunction Algorithm (Search)

Given assignment `S = {age=3, state=CA, gender=M}`:

**Key observation**: A conjunction `c` of size K matches S if and only if:

1. There exist exactly K posting lists (for keys in S) where `c` appears with an `∈` annotation.
2. There is no posting list (for a key in S) where `c` appears with a `∉` annotation.

The algorithm works for each K-index from max down to 0:

1. Retrieve all posting lists whose keys appear in S → `PLists`
2. If `|PLists| < K`: skip (impossible to satisfy any conjunction)
3. Sort `PLists` by their current entry (conjunction ID)
4. **Check**: do `PLists[0]` and `PLists[K-1]` have the same conjunction ID at their current position?
   - **No** → skip `PLists[0..K-2]` forward to `PLists[K-1]`'s current ID (no conjunction between them can have K matching lists)
   - **Yes** → check if `PLists[0]`'s entry has `∉` (violation)
     - **Violation** → skip all lists at this ID forward past it
     - **No violation** → conjunction is satisfied; add its ID to results; advance past it

This advance-and-skip loop terminates when the K-th list reaches end-of-list.

**Worked example** with `S = {age=3, state=CA, gender=M}`, `K=2`:

```
Step 1: PLists = [(age,3), (state,CA), (gender,M)]
        Sorted by current entry:
          (age,3):   [c1, c2, c3]   ← current: c1
          (state,CA):[c3∉, c4]      ← current: c3∉
          (gender,M):[c3, c4]       ← current: c3

Step 2: PLists[0].ID=c1, PLists[1].ID=c3 → not equal
        Skip (age,3) to c3.
        Sorted: (state,CA)[c3∉,c4], (age,3)[c3], (gender,M)[c3]

Step 3: PLists[0].ID=c3, PLists[1].ID=c3 → equal
        PLists[0] entry = (c3, ∉) → VIOLATION
        Skip all lists at c3 forward.
        Sorted: (state,CA)[c4], (gender,M)[c4], (age,3)[EOL]

Step 4: PLists[0].ID=c4, PLists[1].ID=c4 → equal
        PLists[0] entry = (c4, ∈) → no violation
        ✓ c4 SATISFIED

K=1: PLists = [(age,3)]
     c5: one ∈ predicate satisfied, no ∉ violation → ✓ c5 SATISFIED

K=0: c6 has ∉ annotation in first list → VIOLATION → c6 rejected

Result: {c4, c5} → DNF IDs that own these conjunctions
```

### Complexity

- Worst case: O(log|S| × |C| × P_avg)
- In practice sub-linear because |S| is small and skipping avoids most entries

---

## CNF Algorithm

CNF BEs are **not** split into disjunctions. Instead, the size of a CNF is the number of disjunctions with **no `∉` predicates**.

Key differences from DNF:

1. Each posting list entry stores a **disjunction ID** in addition to the CNF ID and annotation.
2. When K matching lists are found for a CNF, a per-disjunction **counter array** is checked:
   - Counter for disjunction `d` starts at `−(number of ∉ predicates in d)`
   - Incremented by 1 for each `∉` entry found for `d`
   - Set to 1 when an `∈` entry is found for `d`
   - A CNF is satisfied only if **all** counters are non-zero (i.e. ≥ 1, or at least one `∉` was triggered for `∉`-only disjunctions)
3. The K upper bound is not capped at `|S|` (a CNF can have more disjunctions than assignment keys).

**Worked example** with `S = {A=1, C=2}`, CNF `c3 = (A∈{1}∨B∈{1}) ∧ (C∈{2}∨D∈{1})`:

```
K=2 posting lists for S:
  (A,1): [c1∈0, c2∈0, c3∈0, c4∈0]
  (C,2): [c2∈0, c3∈1]

Processing c3:
  Counters = [0, 0]  (both disjunctions have no ∉ predicates)
  (A,1) entry for c3: disjunction 0, ∈  → Counter[0] = 1
  (C,2) entry for c3: disjunction 1, ∈  → Counter[1] = 1
  All counters > 0 → ✓ c3 SATISFIED
```

---

## Scoring and Top-N Pruning

Weights are attached to each `(attribute, value)` key in the criteria. The score of a conjunction `E` against assignment `S` is:

```
Score_conj(E, S) = Σ  w_E(A,v) × w_S(A,v)
                 (A,v) ∈ IN(E) ∩ S
```

Where `IN(E)` = all keys from `∈` predicates of `E` (exclusions are not scored).

| Criteria form | Score definition |
|---|---|
| `DNFCriteria` | `max` over all constituent conjunctions |
| `CNFCriteria` | `sum` over all constituent disjunctions |
| `UNFCriteria` (AND) | `sum` over sub-criteria and predicates |
| `UNFCriteria` (OR) | `max` over sub-criteria and predicates |

**Top-N pruning**: Each posting list head stores an upper bound `UB(A,v) = max weight of (A,v) across all criteria`. During search, if the sum of upper bounds for the current candidate is below the N-th highest score seen so far, the candidate and all subsequent candidates in that K-index can be skipped without evaluation.

---

## How the Java implementation maps to the paper

| Paper concept | Java class |
|---|---|
| Boolean Expression | `Criteria` (abstract) |
| DNF BE | `DNFCriteria` |
| CNF BE | `CNFCriteria` |
| Conjunction | `Conjunction` |
| Disjunction | `Disjunction` |
| ∈ predicate | `IncludedPredicate` |
| ∉ predicate | `ExcludedPredicate` |
| Key (A,v) | `Key` |
| Posting list entry (DNF) | `ConjunctionPostingEntry` |
| Posting list entry (CNF) | `DisjunctionPostingEntry` |
| Inverted index | `DNFInvertedIndex` / `CNFInvertedIndex` |
| K-index partition | Internal map keyed by size within `DNFInvertedIndex` |
| Conjunction Algorithm | `DNFMatcher` |
| CNF Algorithm | `CNFMatcher` |
| Assignment | `RequestContext` (wraps a `JsonNode`) |
| Caveat evaluation | `CaveatEnforcer` |
| Indexing entry point | `IndexingFacade` → `DNFIndexer` / `CNFIndexer` |
| Search entry point | `SearchFacade` → `CriteriaSearchHandler` |
| Brute-force scan | `Scanner` |
| Anomaly detection | `Ratifier` (compares `SearchFacade` vs `Scanner`) |

---

## Extensions beyond the paper

Mustang extends the core VLDB algorithm with:

- **Multi-caveat predicates**: Beyond `∈`/`∉` equality, Mustang supports `RANGE`, `REGEX`, `VERSIONING`, `SUBSET`, `SUPERSET`, `EQUALSET`, `EXISTENCE`, `NON_EXISTENCE` — all evaluated by `CaveatEnforcer` after the posting-list merge identifies candidates.
- **PreOperations**: Value transformation before predicate evaluation — e.g., `x % 4`, `substring(x, 1, 3)`, `dateExtract(x, YEAR)`.
- **UNFCriteria**: Unrestricted Normal Form — arbitrary nesting of criteria trees, evaluated recursively without indexing (useful for low-cardinality rule sets or complex compositions).
- **Ratification**: A background process that cross-checks the inverted index against a brute-force scan across a sample of possible assignments, detecting any indexing anomalies.
- **Similarity detection**: Before indexing a new criteria, check what fraction of possible assignments it shares with already-indexed criteria.
- **Atomic index replacement**: Build a new index under a temporary name, then atomically swap it into production — zero downtime rebuilds.
