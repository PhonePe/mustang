# Example: Online Ad Targeting

This example models an online advertising system where each advertiser campaign is a Boolean targeting rule. When a user visits a page, Mustang finds all matching campaigns in milliseconds — even across millions of rules.

---

## Scenario

An ad platform has campaigns with rules like:

| Campaign | Rule |
|---|---|
| `cricket-fans-india` | Country ∈ {IN} AND interest ∈ {cricket, sports} |
| `tech-apac-premium` | (Country ∈ {IN,SG,JP} AND tier ∈ {premium}) OR (Country ∈ {AU} AND age ≥ 25) |
| `global-exclude-ios` | Country ∈ {IN,SG,US,GB} AND platform ∉ {ios} |
| `young-android` | Age ∈ [18,25] AND platform ∈ {android} |

A user visits with profile: `{country: "IN", age: 23, platform: "android", interest: "cricket", tier: "free"}`.

---

## Step 1: Build the engine and index all campaigns

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.detail.impl.EqualityDetail;
import com.phonepe.mustang.detail.impl.RangeDetail;
import com.google.common.collect.Sets;

ObjectMapper mapper = new ObjectMapper();
MustangEngine engine = MustangEngine.builder().mapper(mapper).build();

// Campaign 1: cricket fans in India
Criteria campaign1 = DNFCriteria.builder()
    .id("cricket-fans-india")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .weight(3L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("IN")).build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.interest")
            .weight(10L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("cricket", "sports")).build())
            .build())
        .build())
    .build();

// Campaign 2: tech-savvy APAC users, premium tier or Australia + age
Criteria campaign2 = DNFCriteria.builder()
    .id("tech-apac-premium")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .weight(2L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("IN","SG","JP")).build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.tier")
            .weight(5L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("premium")).build())
            .build())
        .build())
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("AU")).build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.age")
            .detail(RangeDetail.builder().lowerBound(25).includeLowerBound(true).build())
            .build())
        .build())
    .build();

// Campaign 3: broad reach, exclude iOS
Criteria campaign3 = DNFCriteria.builder()
    .id("global-exclude-ios")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .weight(1L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("IN","SG","US","GB")).build())
            .build())
        .predicate(ExcludedPredicate.builder()
            .lhs("$.platform")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("ios")).build())
            .build())
        .build())
    .build();

// Campaign 4: young Android users
Criteria campaign4 = DNFCriteria.builder()
    .id("young-android")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.age")
            .weight(4L)
            .detail(RangeDetail.builder()
                .lowerBound(18).includeLowerBound(true)
                .upperBound(25).includeUpperBound(true)
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.platform")
            .weight(3L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("android")).build())
            .build())
        .build())
    .build();

engine.add("ads", List.of(campaign1, campaign2, campaign3, campaign4));
```

---

## Step 2: Search for a user event

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.phonepe.mustang.common.RequestContext;

JsonNode userEvent = mapper.readTree("""
    {
      "country": "IN",
      "age": 23,
      "platform": "android",
      "interest": "cricket",
      "tier": "free"
    }
    """);

RequestContext ctx = RequestContext.builder().node(userEvent).build();

// All matching campaigns
Set<String> matches = engine.search("ads", ctx);
// → ["cricket-fans-india", "global-exclude-ios", "young-android"]
// Note: "tech-apac-premium" does NOT match because tier="free" and country≠AU
```

---

## Step 3: Top-N ranked selection

In display advertising you can only show a limited number of ads. Use top-N to get the best-scoring campaigns:

```java
// Show only top 2 campaigns (by relevance score)
Set<String> top2 = engine.search("ads", ctx, 2);
// Scores:
//   cricket-fans-india:  3*1 + 10*1 = 13
//   global-exclude-ios:  1*1        = 1
//   young-android:       4*1 + 3*1  = 7
// Top 2 → ["cricket-fans-india", "young-android"]
```

---

## Step 4: Debug why a campaign matched or didn't

```java
DebugResult debug = engine.debug(campaign2, ctx);
System.out.println(debug.isResult());   // false

// Per-conjunction breakdown:
debug.getCompositionDebugResults().forEach(conjResult -> {
    System.out.println("Conjunction matched: " + conjResult.isResult());
    conjResult.getPredicateDebugResults().forEach(p ->
        System.out.println("  " + p.getLhs() + " → " + p.isResult())
    );
});
// Output:
//   Conjunction matched: false
//     $.country → true   (IN is in {IN,SG,JP})
//     $.tier    → false  (free is not in {premium})
//   Conjunction matched: false
//     $.country → false  (IN is not in {AU})
//     $.age     → true   (23 >= 25 is false... actually false)
```

---

## Step 5: Live campaign updates

```java
// Advertiser updates their campaign targeting
Criteria updatedCampaign1 = DNFCriteria.builder()
    .id("cricket-fans-india")          // same ID → replaces existing
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .weight(3L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("IN", "PK")).build()) // added PK
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.interest")
            .weight(10L)
            .detail(EqualityDetail.builder().values(Sets.newHashSet("cricket", "sports")).build())
            .build())
        .build())
    .build();

engine.update("ads", updatedCampaign1);

// Pause campaign
engine.delete("ads", campaign4);
```

---

## Step 6: Nightly consistency check

```java
engine.ratify("ads");

// Later...
RatificationResult result = engine.getRatificationResult("ads");
if (!Boolean.TRUE.equals(result.getStatus())) {
    alertOncall("Ad index anomaly detected: " + result.getAnamolyDetails());
}
```
