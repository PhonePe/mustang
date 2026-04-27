# Advanced Operations

---

## Ratification — anomaly detection

Ratification cross-checks the inverted index against a brute-force scan to detect indexing anomalies. It runs **asynchronously** in a background thread.

```java
// Trigger full ratification (compares search vs scan over all combinations)
engine.ratify("my-index");

// Light-weight ratification
engine.ratify("my-index", false);

// Poll for result (non-blocking)
RatificationResult result = engine.getRatificationResult("my-index");
```

### `RatificationResult` fields

| Field | Type | Description |
|---|---|---|
| `status` | `Boolean` | `true` = no anomalies found; `false` = anomalies detected; `null` = not yet complete |
| `combinations` | `Combinations` | Statistics about how many input combinations were tested |
| `combinations.totalCount` | `int` | Total combinations evaluated |
| `combinations.cpCount` | `int` | Cartesian-product combinations |
| `combinations.ssCount` | `int` | Subset combinations |
| `anamolyDetails` | `Set<RatificationDetail>` | Details of each discrepancy found |
| `timeTakenMs` | `long` | How long the ratification run took |
| `requestedAt` | `long` | Epoch ms when ratification was requested |
| `fullFledgedRun` | `boolean` | Whether a full or light-weight run was performed |
| `ratifiedAt` | `long` | Epoch ms when ratification completed |

### Typical usage pattern

```java
engine.ratify("my-index");

// Poll until done (in practice use a scheduler or a completion hook)
RatificationResult result;
do {
    Thread.sleep(500);
    result = engine.getRatificationResult("my-index");
} while (result == null || result.getStatus() == null);

if (Boolean.TRUE.equals(result.getStatus())) {
    System.out.println("Index is consistent");
} else {
    System.out.println("Anomalies: " + result.getAnamolyDetails());
}
```

!!! info "When to ratify"
    Run ratification after a large batch of `add`/`update`/`delete` operations, or on a schedule (e.g., nightly) to validate that the index remains consistent with the raw criteria data.

---

## Similarity detection

Before indexing a new criteria, you can check how much it overlaps with the already-indexed criteria — useful for deduplication or conflict detection.

```java
SimilarityStats stats = engine.checkSimilarity("my-index", candidateCriteria);
```

### `SimilarityStats` fields

| Field | Type | Description |
|---|---|---|
| `overallScenarios` | `long` | Total input scenarios evaluated |
| `overlappingScenarios` | `long` | Scenarios where candidate matches the same criteria as an existing one |
| `similarityScore` | `float` | `overlappingScenarios / overallScenarios` — 0.0 (no overlap) to 1.0 (identical) |
| `similarities` | `List<Similarity>` | Per-criteria overlap details |

### Example

```java
SimilarityStats stats = engine.checkSimilarity("ads", newAd);

if (stats.getSimilarityScore() > 0.8f) {
    System.out.println("New ad overlaps heavily with existing ads — consider deduplication");
}
```

---

## Export & Import

Export and import let you serialize the entire in-memory index to/from JSON — useful for debugging, migration between environments, or warm-starting a new instance.

### Export

```java
String json = engine.exportIndexGroup("my-index");
// Save to file, database, object storage, etc.
```

### Import

```java
String json = Files.readString(Path.of("my-index-export.json"));
engine.importIndexGroup("my-index", json);
```

!!! warning "Import requires an empty index"
    `importIndexGroup` throws `INDEX_GROUP_EXISTS` if the target index name already has data. Either use a fresh engine instance or choose a different index name.

---

## Snapshot

A lightweight diagnostic view — shows the index structure (K-indexes, posting list keys, entry counts) without full serialization:

```java
String snapshot = engine.snapshot("my-index");
System.out.println(snapshot);
```

Useful for understanding how many conjunctions are in each K-index partition and diagnosing unexpected posting list sizes.

---

## Atomic index replacement

For production hot-reloads without any downtime:

```java
// Step 1: build the new index while the old one serves traffic
engine.add("ads-v2", newCriteriaList);

// Step 2: atomic swap — "ads" now points to new data
engine.replaceIndex("ads", "ads-v2");

// All subsequent engine.search("ads", ...) use the new index
```

The swap is atomic — there is no window where searches against `"ads"` return partial results. The old index is garbage-collected after the swap.

### Recommended reload pattern

```java
public void reload(List<Criteria> freshRules) {
    String tmpIndex = "rules-" + System.currentTimeMillis();
    engine.add(tmpIndex, freshRules);
    engine.replaceIndex("rules", tmpIndex);
}
```
