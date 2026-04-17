# Example: PreOperation Recipes

A collection of practical PreOperation patterns for common real-world transformations.

---

## Shard / bucket routing

Route traffic to a specific shard using `ModuloPreOperation`:

```java
// userId % 8 == 3  →  shard 3 of 8
IncludedPredicate.builder()
    .lhs("$.userId")
    .preOperation(ModuloPreOperation.builder().rhs(8).build())
    .detail(EqualityDetail.builder().values(Sets.newHashSet(3)).build())
    .build()
```

**Percentage rollout** (25% of users):

```java
// userId % 100 < 25
IncludedPredicate.builder()
    .lhs("$.userId")
    .preOperation(ModuloPreOperation.builder().rhs(100).build())
    .detail(RangeDetail.builder()
        .lowerBound(0).includeLowerBound(true)
        .upperBound(25).includeUpperBound(false)
        .build())
    .build()
```

---

## Country code from locale string

Extract the country suffix from `"en-IN"`, `"zh-CN"`, `"pt-BR"`:

```java
// "en-IN" → substring[3,5) → "IN"
IncludedPredicate.builder()
    .lhs("$.locale")
    .preOperation(SubStringPreOperation.builder().beginIndex(3).endIndex(5).build())
    .detail(EqualityDetail.builder().values(Sets.newHashSet("IN", "SG")).build())
    .build()
```

---

## Phone number ISD code

Match the first 3 characters of a phone number:

```java
// "+91xxxxxxxxxx" → "+91"
IncludedPredicate.builder()
    .lhs("$.phone")
    .preOperation(SubStringPreOperation.builder().beginIndex(0).endIndex(3).build())
    .detail(EqualityDetail.builder().values(Sets.newHashSet("+91")).build())
    .build()
```

---

## Time-of-day gating

Use `DateTimePreOperation` to restrict a rule to specific hours:

```java
// Peak hours: 10am–2pm
IncludedPredicate.builder()
    .lhs("$.eventEpochMs")
    .preOperation(DateTimePreOperation.builder()
        .extractionType(DateExtractionType.HOUR_OF_DAY)
        .build())
    .detail(RangeDetail.builder()
        .lowerBound(10).includeLowerBound(true)
        .upperBound(14).includeUpperBound(false)
        .build())
    .build()
```

**Day-of-week gating** (weekdays only, 2=Monday … 6=Friday):

```java
IncludedPredicate.builder()
    .lhs("$.eventEpochMs")
    .preOperation(DateTimePreOperation.builder()
        .extractionType(DateExtractionType.DAY_OF_WEEK)
        .build())
    .detail(RangeDetail.builder()
        .lowerBound(2).includeLowerBound(true)
        .upperBound(6).includeUpperBound(true)
        .build())
    .build()
```

---

## Collection size check

Fire a rule only when a user has at least 3 items in cart:

```java
IncludedPredicate.builder()
    .lhs("$.cartItems")      // a JSON array
    .preOperation(SizePreOperation.builder().build())
    .detail(RangeDetail.builder()
        .lowerBound(3).includeLowerBound(true)
        .build())
    .build()
```

---

## String length gate

Only process short usernames (≤ 15 chars):

```java
IncludedPredicate.builder()
    .lhs("$.username")
    .preOperation(LengthPreOperation.builder().build())
    .detail(RangeDetail.builder()
        .upperBound(15).includeUpperBound(true)
        .build())
    .build()
```

---

## Chained operations: extract year from a date field then check

```java
// requestDate is ISO string like "2024-03-15T10:00:00Z"
// Chain: parse string → epoch ms → extract year
IncludedPredicate.builder()
    .lhs("$.requestDate")
    .preOperations(List.of(
        ToDateTimePreOperation.builder().build(),          // "2024-03-15T..." → epoch ms
        DateTimePreOperation.builder()
            .extractionType(DateExtractionType.YEAR)
            .build()                                        // epoch ms → 2024
    ))
    .detail(EqualityDetail.builder()
        .values(Sets.newHashSet(2024, 2025))
        .build())
    .build()
```

---

## Binary feature flag from a bitmask

Check whether bit 3 is set in a feature flags integer:

```java
// flags & 8 == 8  →  bit 3 is set
// Approach: flags % 16 >= 8  (checks bits 3 downward)
// For a clean power-of-2 bit check, use division + modulo chain:

// Extract bit 3: (flags / 8) % 2 == 1
IncludedPredicate.builder()
    .lhs("$.featureFlags")
    .preOperations(List.of(
        DivisionPreOperation.builder().rhs(8.0).build(),   // shift right by 3
        ModuloPreOperation.builder().rhs(2).build()        // isolate bit
    ))
    .detail(EqualityDetail.builder().values(Sets.newHashSet(1)).build())
    .build()
```

---

## Percentage-of-value gate

Trigger a rule when a discount is more than 20% of the original price:

```java
// discountPct = (discount / originalPrice) * 100 > 20
// Use two separate predicates and let the conjunction handle the AND:

// Predicate 1: originalPrice > 0 (guard)
// Predicate 2: discountPct > 20
IncludedPredicate.builder()
    .lhs("$.discountPct")
    .detail(RangeDetail.builder()
        .lowerBound(20).includeLowerBound(false)
        .build())
    .build()
```

Or with a PreOperation if the event only has raw values:

```java
// discount * 100 / originalPrice — use in conjunction with a static range check
// Note: for ratio calculations, pre-compute in your event producer for cleaner rules
```
