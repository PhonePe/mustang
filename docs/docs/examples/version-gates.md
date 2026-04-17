# Example: Feature Flags & Version Gates

This example shows how to use Mustang to implement sophisticated feature flag rules and version gates — conditions based on app version, user tier, device, and geography.

---

## Scenario

A mobile app backend controls feature availability using Boolean rules:

| Feature Flag | Rule |
|---|---|
| `new-checkout-ui` | appVersion ≥ 4.0.0 AND platform ∈ {android, ios} |
| `dark-mode-beta` | tier ∈ {premium, beta} AND appVersion ≥ 3.5.0 |
| `upi-autopay` | country ∈ {IN} AND appVersion ≥ 3.2.0 |
| `legacy-warning` | appVersion < 2.0.0 |
| `ab-bucket-0` | userId % 10 == 0 (10% rollout, bucket 0) |

---

## Setup

```java
import com.phonepe.mustang.detail.impl.VersioningDetail;
import com.phonepe.mustang.detail.impl.VersioningDetail.CheckType;
import com.phonepe.mustang.preoperation.impl.ModuloPreOperation;

MustangEngine engine = MustangEngine.builder().mapper(mapper).build();

// new-checkout-ui: appVersion >= 4.0.0 AND platform ∈ {android, ios}
Criteria newCheckout = DNFCriteria.builder()
    .id("new-checkout-ui")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.appVersion")
            .detail(VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("4.0.0")
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.platform")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("android", "ios"))
                .build())
            .build())
        .build())
    .build();

// dark-mode-beta: tier ∈ {premium, beta} AND appVersion >= 3.5.0
Criteria darkMode = DNFCriteria.builder()
    .id("dark-mode-beta")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.tier")
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("premium", "beta"))
                .build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.appVersion")
            .detail(VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("3.5.0")
                .build())
            .build())
        .build())
    .build();

// upi-autopay: country=IN AND appVersion >= 3.2.0
Criteria upiAutopay = DNFCriteria.builder()
    .id("upi-autopay")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.country")
            .detail(EqualityDetail.builder().values(Sets.newHashSet("IN")).build())
            .build())
        .predicate(IncludedPredicate.builder()
            .lhs("$.appVersion")
            .detail(VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("3.2.0")
                .build())
            .build())
        .build())
    .build();

// legacy-warning: appVersion < 2.0.0
Criteria legacyWarning = DNFCriteria.builder()
    .id("legacy-warning")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.appVersion")
            .detail(VersioningDetail.builder()
                .check(CheckType.BELOW)
                .baseVersion("2.0.0")
                .excludeBase(true)   // strictly below 2.0.0
                .build())
            .build())
        .build())
    .build();

// ab-bucket-0: userId % 10 == 0
Criteria abBucket0 = DNFCriteria.builder()
    .id("ab-bucket-0")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.userId")
            .preOperation(ModuloPreOperation.builder().rhs(10).build())
            .detail(EqualityDetail.builder().values(Sets.newHashSet(0)).build())
            .build())
        .build())
    .build();

engine.add("features", List.of(newCheckout, darkMode, upiAutopay, legacyWarning, abBucket0));
```

---

## Check features for a user request

```java
JsonNode request = mapper.readTree("""
    {
      "userId": 120,
      "country": "IN",
      "platform": "android",
      "appVersion": "4.1.0",
      "tier": "premium"
    }
    """);

RequestContext ctx = RequestContext.builder().node(request).build();
Set<String> enabledFeatures = engine.search("features", ctx);
// → ["new-checkout-ui", "dark-mode-beta", "upi-autopay", "ab-bucket-0"]
//
// Explanation:
//   new-checkout-ui:  4.1.0 >= 4.0.0 ✓, platform=android ✓   → ENABLED
//   dark-mode-beta:   tier=premium ✓, 4.1.0 >= 3.5.0 ✓        → ENABLED
//   upi-autopay:      country=IN ✓, 4.1.0 >= 3.2.0 ✓          → ENABLED
//   legacy-warning:   4.1.0 < 2.0.0 ✗                         → DISABLED
//   ab-bucket-0:      120 % 10 = 0 ✓                           → ENABLED
```

---

## Percentage rollouts with PreOperations

Use `ModuloPreOperation` to implement arbitrary rollout percentages:

```java
// 25% rollout — buckets 0-24 out of 100
Criteria rollout25 = DNFCriteria.builder()
    .id("new-payment-flow-25pct")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.userId")
            .preOperation(ModuloPreOperation.builder().rhs(100).build())
            .detail(RangeDetail.builder()
                .lowerBound(0).includeLowerBound(true)
                .upperBound(25).includeUpperBound(false)
                .build())
            .build())
        .build())
    .build();
```

---

## Locale-based feature gates with SubString

Target specific country codes from a locale string like `"en-IN"`:

```java
import com.phonepe.mustang.preoperation.impl.SubStringPreOperation;

// Gate on country code extracted from locale (e.g., "en-IN" → "IN")
Criteria indiaGate = DNFCriteria.builder()
    .id("india-locale-feature")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.locale")
            .preOperation(SubStringPreOperation.builder()
                .beginIndex(3)
                .endIndex(5)
                .build())
            .detail(EqualityDetail.builder()
                .values(Sets.newHashSet("IN"))
                .build())
            .build())
        .build())
    .build();
```

---

## Time-based gates with DateTimePreOperation

Enable a feature only during business hours (9am–6pm):

```java
import com.phonepe.mustang.preoperation.impl.DateTimePreOperation;
import com.phonepe.mustang.preoperation.impl.DateExtractionType;

Criteria businessHours = DNFCriteria.builder()
    .id("business-hours-only")
    .conjunction(Conjunction.builder()
        .predicate(IncludedPredicate.builder()
            .lhs("$.requestTime")   // epoch millis in the event
            .preOperation(DateTimePreOperation.builder()
                .extractionType(DateExtractionType.HOUR_OF_DAY)
                .build())
            .detail(RangeDetail.builder()
                .lowerBound(9).includeLowerBound(true)
                .upperBound(18).includeUpperBound(false)
                .build())
            .build())
        .build())
    .build();
```
