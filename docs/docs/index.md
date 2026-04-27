# Mustang

**Mustang** is a high-performance Java engine for indexing and searching **Boolean expressions** over a high-dimensional, multi-valued attribute space — built by PhonePe Engineering.

Given a large collection of Boolean rules (criteria), Mustang rapidly finds every rule that evaluates to `true` for a given event, in sub-linear time.

---

## Why Mustang?

Traditional approaches evaluate each rule against every incoming event — an O(N) linear scan that does not scale past a few thousand rules. Mustang flips the problem: it builds an **inverted index** over the rules themselves, so that a search over 1 million rules takes the same order of time as a search over 10,000.

| Capability | Description |
|---|---|
| **DNF & CNF** | Index arbitrary conjunctions and disjunctions over multi-valued attributes |
| **Sub-linear search** | Inverted-list merge; complexity proportional to matching entries, not total index size |
| **Top-N ranked results** | Built-in scoring returns the highest-relevance matches first |
| **Rich predicate types** | Equality, Range, Regex, Versioning, Subset/Superset/EqualSet, Existence |
| **PreOperations** | Transform attribute values (modulo, substring, date extraction, …) before matching |
| **UNF (Unrestricted Normal Form)** | Compose arbitrary nested criteria trees beyond pure DNF/CNF |
| **Live mutations** | Add, update, delete, and atomically replace entire index groups |
| **Ratification** | Anomaly detection — compares inverted-index search against brute-force scan |
| **Similarity detection** | Check how much a candidate criteria overlaps existing indexed criteria |
| **Dropwizard bundle** | Optional REST API (search, scan, debug, ratify, export) |

---

## Architecture at a glance

```
                         ┌──────────────────────────────────────────┐
                         │             MustangEngine                │
                         │                                          │
  add/update/delete ────►│  IndexingFacade                          │
                         │    ├── DNFIndexer  ──► DNFInvertedIndex  │
                         │    └── CNFIndexer  ──► CNFInvertedIndex  │
                         │                                          │
  search(context) ──────►│  SearchFacade                            │
                         │    ├── DNFMatcher  (Conjunction Alg.)    │
                         │    └── CNFMatcher  (CNF Alg.)            │
                         │           └── CaveatEnforcer             │
                         │                                          │
  scan(list, context) ──►│  Scanner  (brute-force, for comparison)  │
                         │                                          │
  ratify(index) ────────►│  Ratifier (search vs. scan diff)         │
                         │                                          │
  checkSimilarity() ────►│  SimilarityDetector                      │
                         └──────────────────────────────────────────┘

                         ┌──────────────────────────────────────────┐
                         │          mustang-dw-bundle               │
                         │  JAX-RS resources over MustangEngine     │
                         │  POST /mustang/v1/search                 │
                         │  POST /mustang/v1/scan/{index,criteria}  │
                         │  POST /mustang/v1/debug                  │
                         │  POST /mustang/v1/index/{export,ratify}  │
                         └──────────────────────────────────────────┘
```

### How the inverted index works

Each `InvertedIndex` stores **posting lists** keyed by `(attribute, value)` pairs — called **keys**. For DNF, posting list entries are `ConjunctionPostingEntry` records that carry the conjunction ID, an `∈`/`∉` annotation, and an optional weight. Conjunctions are partitioned by **size** (number of `∈` predicates) into **K-indexes**.

At search time, only the posting lists whose keys appear in the incoming event are retrieved. The **Conjunction Algorithm** then merges these lists in sorted order, using a sorted-list advance-and-skip technique (inspired by WAND from IR) to efficiently find exactly which conjunctions are fully satisfied — without scanning all of them.

For CNF, the **CNF Algorithm** extends this by tracking a per-disjunction satisfaction counter, accepting a CNF only when every disjunction's counter is positive.

See [The Algorithm](algorithm.md) for a complete walkthrough.

---

## Project modules

```
mustang/
├── mustang-models/      Domain types: Criteria, Predicate, Detail, PreOperation, …
├── mustang-core/        Indexing & search engine (MustangEngine)
├── mustang-dw-bundle/   Dropwizard REST bundle (optional)
├── benchmarking/        JMH micro-benchmarks
└── reference/           VLDB 2009 paper (vldb09-indexing.pdf)
```

---

## Academic reference

The indexing and search algorithms are based on:

> **"Indexing Boolean Expressions"**
> Steven Euijong Whang, Hector Garcia-Molina (Stanford); Chad Brower, Jayavel Shanmugasundaram, Sergei Vassilvitskii, Erik Vee, Ramana Yerneni (Yahoo! Research)
> *Proceedings of VLDB 2009, Lyon, France*
> [`reference/vldb09-indexing.pdf`](https://github.com/PhonePe/mustang/blob/main/reference/vldb09-indexing.pdf)

---

## License

Apache 2.0 — see [LICENSE](https://github.com/PhonePe/mustang/blob/main/LICENSE).
