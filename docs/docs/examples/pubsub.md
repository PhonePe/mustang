# Example: Pub/Sub Event Routing

This example models a content-based publish/subscribe system. Subscribers register Boolean filter expressions. When an event is published, Mustang finds all matching subscribers in sub-linear time — regardless of how many subscriptions are registered.

---

## Scenario

A financial data platform routes events to subscribers:

| Subscriber | Filter |
|---|---|
| `sports-alerts` | category ∈ {Sports} AND rating ∈ {4,5} |
| `finance-non-tech` | category ∈ {Finance} AND industry ∉ {Tech} |
| `breaking-any` | priority ∈ {breaking} |
| `finance-or-sports` | (category ∈ {Finance}) OR (category ∈ {Sports} AND rating ≥ 4) |

---

## Index all subscriptions

```java
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.composition.impl.Disjunction;

MustangEngine engine = MustangEngine.builder().mapper(mapper).build();

// sports-alerts: category=Sports AND rating∈{4,5}
Criteria sportsAlerts = DNFCriteria.builder()
    .id("sports-alerts")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.category")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("Sports")).build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.rating")
            .detail(EqualityDetail.builder().values(Sets.newHashSet(4, 5)).build())
            .build())
        .build())
    .build();

// finance-non-tech: category=Finance AND industry∉{Tech}
Criteria financeNonTech = DNFCriteria.builder()
    .id("finance-non-tech")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.category")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("Finance")).build())
            .build())
        .predicate(ExcludedPredicate.builder()
            .lhs("$.industry")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("Tech")).build())
            .build())
        .build())
    .build();

// breaking-any: priority=breaking (no other constraints)
Criteria breakingAny = DNFCriteria.builder()
    .id("breaking-any")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.priority")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("breaking")).build())
            .build())
        .build())
    .build();

// finance-or-sports (CNF): (category=Finance) AND (category=Sports OR rating≥4)
// Expressed as CNF: two disjunctions that must both be satisfied
Criteria financeOrSports = CNFCriteria.builder()
    .id("finance-or-sports")
    .disjunction(Disjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.category")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("Finance")).build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.category")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("Sports")).build())
            .build())
        .build())
    .disjunction(Disjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.rating")
            .detail(RangeDetail.builder().lowerBound(4).includeLowerBound(true).build())
            .build())
        .build())
    .build();

engine.add("subscriptions", List.of(sportsAlerts, financeNonTech, breakingAny, financeOrSports));
```

---

## Route an incoming event

```java
// Event: a sports story with rating 5
JsonNode event = mapper.readTree("""
    {
      "category": "Sports",
      "rating": 5,
      "industry": "Media",
      "priority": "normal"
    }
    """);

RequestContext ctx = RequestContext.builder().node(event).build();
Set<String> subscribers = engine.search("subscriptions", ctx);
// → ["sports-alerts", "finance-or-sports"]
// Explanation:
//   sports-alerts:    category=Sports ✓, rating∈{4,5} ✓ → MATCH
//   finance-non-tech: category∈{Finance} ✗             → NO MATCH
//   breaking-any:     priority=breaking ✗               → NO MATCH
//   finance-or-sports: disjunction1: Sports ✓, disjunction2: rating≥4 ✓ → MATCH
```

---

## Route a breaking finance event

```java
JsonNode breaking = mapper.readTree("""
    {
      "category": "Finance",
      "industry": "Banking",
      "rating": 3,
      "priority": "breaking"
    }
    """);

RequestContext ctx2 = RequestContext.builder().node(breaking).build();
Set<String> subscribers2 = engine.search("subscriptions", ctx2);
// → ["finance-non-tech", "breaking-any", "finance-or-sports"]
// Explanation:
//   finance-non-tech:  category=Finance ✓, industry∉{Tech} ✓ → MATCH
//   breaking-any:      priority=breaking ✓                   → MATCH
//   finance-or-sports: disjunction1: Finance ✓, disjunction2: rating≥4? (3<4) ✗ → NO MATCH
//                      Wait — disjunction2 is NOT satisfied (rating=3 < 4).
//                      → Actually NO MATCH for finance-or-sports
```

---

## Dynamically add/remove subscriptions

```java
// New subscriber registers
Criteria newSub = DNFCriteria.builder()
    .id("tech-finance")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.category")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("Finance")).build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.industry")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("Tech")).build())
            .build())
        .build())
    .build();

engine.add("subscriptions", newSub);

// Subscriber cancels
engine.delete("subscriptions", breakingAny);
```

---

## Reload the full subscription set atomically

When you reload all subscriptions from a database (e.g., on startup or periodically):

```java
List<Criteria> freshSubs = loadFromDatabase();  // your DB loader
String tmpIndex = "subscriptions-" + System.currentTimeMillis();
engine.add(tmpIndex, freshSubs);
engine.replaceIndex("subscriptions", tmpIndex);
// Zero downtime — no events are missed during the reload
```
