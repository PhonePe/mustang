# PreOperations

A **PreOperation** transforms the raw attribute value extracted from the event **before** the detail caveat is evaluated. This lets you express conditions on derived values without changing the structure of your events.

PreOperations are attached to a predicate via `preOperation` (single) or `preOperations` (list, chained left-to-right):

```java
IncludedPredicate.builder()
    .lhs("$.timestamp")
    .preOperation(DateTimePreOperation.builder()
        .extractionType(DateExtractionType.YEAR)
        .build())
    .detail(EqualityDetail.builder()
        .values(Sets.newHashSet(2024, 2025))
        .build())
    .build()
```

---

## Arithmetic operations

### `ModuloPreOperation`

Computes `value % rhs`.

```java
ModuloPreOperation.builder().rhs(4).build()
// value=9 → 9 % 4 = 1
```

**Use case** — shard/bucket routing: only match users in shard 0 out of 4:

```java
IncludedPredicate.builder()
    .lhs("$.userId")
    .preOperation(ModuloPreOperation.builder().rhs(4).build())
    .detail(EqualityDetail.builder().values(Sets.newHashSet(0)).build())
    .build()
```

---

### `DivisionPreOperation`

Computes `value / rhs`.

```java
DivisionPreOperation.builder().rhs(1000.0).build()
// value=5000 → 5.0
```

---

### `MultiplicationPreOperation`

Computes `value * rhs`.

```java
MultiplicationPreOperation.builder().rhs(100).build()
// value=0.85 → 85.0
```

---

### `AdditionPreOperation`

Computes `value + rhs`.

```java
AdditionPreOperation.builder().rhs(5).build()
```

---

### `SubtractionPreOperation`

Computes `value - rhs`.

```java
SubtractionPreOperation.builder().rhs(10).build()
```

---

## String operations

### `SubStringPreOperation`

Extracts a substring by index range `[beginIndex, endIndex)`.

```java
SubStringPreOperation.builder()
    .beginIndex(0)
    .endIndex(2)
    .build()
// value="IN-Mumbai" → "IN"
```

**Use case** — match on country code prefix of a locale string:

```java
IncludedPredicate.builder()
    .lhs("$.locale")
    .preOperation(SubStringPreOperation.builder().beginIndex(0).endIndex(2).build())
    .detail(EqualityDetail.builder().values(Sets.newHashSet("IN", "SG")).build())
    .build()
```

---

### `LengthPreOperation`

Returns the length of the string (or the size of a collection).

```java
LengthPreOperation.builder().build()
// value="Hello" → 5
```

---

### `BinaryConversionPreOperation`

Converts a number to its binary string representation.

```java
BinaryConversionPreOperation.builder().build()
// value=10 → "1010"
```

---

## Collection operations

### `SizePreOperation`

Returns the number of elements in a collection.

```java
SizePreOperation.builder().build()
// value=["a","b","c"] → 3
```

---

## Date / time operations

### `DateTimePreOperation`

Extracts a specific component from an epoch-millisecond timestamp.

```java
DateTimePreOperation.builder()
    .extractionType(DateExtractionType.YEAR)
    .build()
// value=1704067200000L → 2024
```

Available `DateExtractionType` values:

| Type | Description |
|---|---|
| `YEAR` | Calendar year (e.g., 2024) |
| `MONTH` | Month of year (1–12) |
| `DAY_OF_MONTH` | Day (1–31) |
| `DAY_OF_WEEK` | Day of week (1=Sunday … 7=Saturday) |
| `HOUR_OF_DAY` | Hour (0–23) |
| `MINUTE` | Minute (0–59) |
| `SECOND` | Second (0–59) |

---

### `ToDateTimePreOperation`

Converts a formatted date string to an epoch millisecond timestamp (for subsequent date arithmetic).

---

## Chaining operations

Multiple PreOperations can be chained — they are applied **left-to-right** in sequence:

```java
IncludedPredicate.builder()
    .lhs("$.eventTime")
    .preOperations(List.of(
        DateTimePreOperation.builder().extractionType(DateExtractionType.HOUR_OF_DAY).build(),
        ModuloPreOperation.builder().rhs(6).build()   // which 6-hour slot?
    ))
    .detail(EqualityDetail.builder().values(Sets.newHashSet(0)).build())  // midnight–6am slot
    .build()
```

The `ChainOperator` utility applies the list internally: `operate([op1, op2, op3], value)` → `op3(op2(op1(value)))`.

---

## `IdentityOperation`

A no-op — passes the value through unchanged. Rarely needed directly but used internally.

---

## Summary table

| Class | Input type | Output type | Description |
|---|---|---|---|
| `ModuloPreOperation` | Number | Number | `value % rhs` |
| `DivisionPreOperation` | Number | Number | `value / rhs` |
| `MultiplicationPreOperation` | Number | Number | `value * rhs` |
| `AdditionPreOperation` | Number | Number | `value + rhs` |
| `SubtractionPreOperation` | Number | Number | `value - rhs` |
| `SubStringPreOperation` | String | String | `value[begin:end]` |
| `LengthPreOperation` | String/Collection | Number | length/size |
| `BinaryConversionPreOperation` | Number | String | binary string |
| `SizePreOperation` | Collection | Number | element count |
| `DateTimePreOperation` | Long (epoch ms) | Number | date component |
| `ToDateTimePreOperation` | String | Long (epoch ms) | parse date |
| `IdentityOperation` | Any | Any | no-op |
