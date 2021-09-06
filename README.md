# Metrics


[![Quality Gate Status](http://prd-sonarqubeapp101.phonepe.nm5:9000/api/project_badges/measure?project=com.phonepe.growth%3Amustang&metric=alert_status)](http://prd-sonarqubeapp101.phonepe.nm5:9000/dashboard?id=com.phonepe.growth%3Amustang)
[![Coverage](http://prd-sonarqubeapp101.phonepe.nm5:9000/api/project_badges/measure?project=com.phonepe.growth%3Amustang&metric=coverage)](http://prd-sonarqubeapp101.phonepe.nm5:9000/dashboard?id=com.phonepe.growth%3Amustang)
[![Bugs](http://prd-sonarqubeapp101.phonepe.nm5:9000/api/project_badges/measure?project=com.phonepe.growth%3Amustang&metric=bugs)](http://prd-sonarqubeapp101.phonepe.nm5:9000/dashboard?id=com.phonepe.growth%3Amustang)
[![Vulnerabilities](http://prd-sonarqubeapp101.phonepe.nm5:9000/api/project_badges/measure?project=com.phonepe.growth%3Amustang&metric=vulnerabilities)](http://prd-sonarqubeapp101.phonepe.nm5:9000/dashboard?id=com.phonepe.growth%3Amustang)
[![Reliability Rating](http://prd-sonarqubeapp101.phonepe.nm5:9000/api/project_badges/measure?project=com.phonepe.growth%3Amustang&metric=reliability_rating)](http://prd-sonarqubeapp101.phonepe.nm5:9000/dashboard?id=com.phonepe.growth%3Amustang)
[![Security Rating](http://prd-sonarqubeapp101.phonepe.nm5:9000/api/project_badges/measure?project=com.phonepe.growth%3Amustang&metric=security_rating)](http://prd-sonarqubeapp101.phonepe.nm5:9000/dashboard?id=com.phonepe.growth%3Amustang)
[![Technical Debt](http://prd-sonarqubeapp101.phonepe.nm5:9000/api/project_badges/measure?project=com.phonepe.growth%3Amustang&metric=sqale_index)](http://prd-sonarqubeapp101.phonepe.nm5:9000/dashboard?id=com.phonepe.growth%3Amustang)

# Mustang


Mustang solves the problem of efficiently indexing Boolean expressions (both Disjunctive Normal Form (DNF) 
and Conjunctive Normal Form (CNF)) in a high-dimensional multi-valued attribute space. The goal is to rapidly find the set of 
Boolean expressions that evaluate to true for a given assignment of values to attributes. A solution to this problem 
has applications in online advertising (where a Boolean expression represents an advertiser’s user targeting 
requirements, and an assignment of values to attributes represents the characteristics of a user visiting an online 
page) and in general any targeting system (where a Boolean expression represents a targeting criteria, and an 
assignment of values to attributes represents an event).

Mustang presents a novel solution based on the inverted list data structure that enables us to index arbitrarily 
complex DNF and CNF Boolean expressions over multi-valued attributes. An interesting aspect of our solution is that, 
by virtue of leveraging inverted lists traditionally used for ranked information retrieval, we can efficiently return 
the top-N matching Boolean expressions. This capability enables applications such as ranked targeting systems, 
where only the top targeting criterias that match an event are desired. For example, in online advertising there is a limit 
on the number of advertisements that can be shown on a given page and only the “best” advertisements can be displayed.



## Add Maven Dependency

```xml
<dependency>
  <groupId>com.phonepe.growth</groupId>
  <artifactId>mustang</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Overview

Mustang allows indexing Boolean Expressions in high-dimensional multi-valued attribute space.

	`Criteria` represents the boolean expressions in one of the two normalized forms.

- DNF : Disjunctive Normal Form, which is a disjunction of conjunctions

	`(A ∈ {a1, a2} ∧ B ∉ {b1, b2} ∧ C ∈ {c1}) ∨ (A ∈ {a1, a3} ∧ D ∉ {d1})`

- CNF : Conjunctive Normal Form, which is a conjunction of disjunctions

	`(A ∈ {a1, a2} ∨ B ∉ {b1, b2} ∨ C ∈ {c1}) ∧ (A ∈ {a1, a3} ∨ D ∉ {d1})`


`Composition` is a set of `Predicate`(s). Depending upon how the constituent results are considered, it could be either :

- `Conjunction(∧)` is satisfied only when all constituent predicates evaluate to true.
- `Disjunction(∨)` is satisfied when any of the constituent predicates evaluate to true.



`Predicate` is a conditional and has the `Detail` that needs to be satisfied.

- `INCLUDED` to indicate inclusion.
- `EXCLUDED` to indicate exclusion.

Further, Mustang allows for logical grouping of `Criteria`(s) when indexing through identification by a name.
`Criteria` of any form can be indexed into an index-group. And searches are always directed to a specific index-group.


`Detail` holds the information about the `Caveat` that needs to be satisfied. `Detail` can be predominantly of three types :

- `EqualityDetail` to enforce `EQUALITY` caveat.
- `RegexDetail` to enforce `REGEX` caveat.
- `RangeDetail` to enforce `RANGE` caveat. Supports all flavors - greater_than, greater_than_equals, less_than, less_than_equals and between (both open & closed).


Below table summarizes `Caveat` support across data types -

| `Caveat `    | Data Types Supported      |
| :--------   | :------------------------ |
| `EQUALITY`   | String, Number, Boolean   |
| `REGEX`      | String                    |
| `RANGE`      | Number                    |



### Usage

#### Initializing Mustang Engine

``` java
ObjectMapper mapper = new ObjectMapper();
MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
```


#### Defining DNF criteria

``` java
Criteria dnf = DNFCriteria.builder()
                .id("C1") // id we would get back should this criteria match a given assignment
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("A1", "A2", "A3"))
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder() // example for less_than_equals
                                        .lowerBound(3)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x")
                                .detail(RangeDetail.builder() // example for greater_than
                                        .upperBound(3)
                                        .build())
                                .build())
                        .build())
                .build();
```

#### Defining CNF criteria

``` java
Criteria cnf = CNFCriteria.builder()
                .id("C2") // id we would get back should this criteria match a given assignment
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("A1", "A2"))
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .detail(RegexDetail.builder()
                                        .regex("B.?")
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(EqualityDetail.builder()
                                        .values(Sets
                                                .newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x")
                                .detail(RangeDetail.builder() // Example for greater_than_equals
                                        .upperBound(7)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet(true))
                                        .build())
                                .build())
                        .build())
                .build();
```

#### Indexing criteria

Index a single criteria

```java
engine.add("index_name", criteria)
```

OR 

Multiple criteria(s) at once.

```java
engine.add("index_name", Arrays.asList(criteria1, criteria2, ...));
```

#### Searching criteria(s) matching an assignment

An assignment is a set of attribute name and value pairs. Json is a very good example of multiple-level K-V pairs.

Example : `JsonNode event = { "a" : "A1", "b" : "B3", "n" : 5, "p" : true }`

First we need to build the context -

```java
EvaluationContext context = EvaluationContext.builder().node(event).build();
```

And search it in the required index - 

``` java
Set<String> searchResults = engine.search("index_name",context);
```

which returns a set of id(s) of all matching criteria(s) in an ordered manner (More on this in the topN section).

At times, an ordered list is not required, in which case, we can skip the scoring part as below.

``` java
Set<String> searchResults = engine.search("index_name",context, false);
```


#### Searching TOP N criteria(s) matching an assignment

We would need to supply the weights for each of the `predicates` to arrive at a notion of scores for any `Criteria`.
These are then leveraged to sort rank the top N criteria.

Score of a criteria - `E` reflects its relevance wrt to an assignment - `S`.

If E is a conjunction of ∈ and ∉ predicates, the score of E is defined as

Score<sub>conj</sub>(E,S) = \sum _{(A,v) \in IN(E) \cap S} w_{E}(A,v) * w_{S}(A,v)

where 
- IN (E ) is the set of all attribute name and value pairs in the ∈ predicates of E (we ignore scoring ∉ predicates)
- w<sub>E</sub> (A, v) is the weight of the pair (A, v) in E 
- w<sub>S</sub> (A, v) is the weight of the pair (A, v) in S

Scores of different `Criteria` are defined as below :

- Score of a `DNFCriteria` is defined as the maximum of the scores of the conjunctions.
- Score of a `CNFCriteria` is defined as sum of the scores of the disjunctions.


#### Updating an already indexed Criteria

Update the already indexed criteria.

```java
engine.update("index_name", criteria)
```

PS :
- `update` is NOT limited to only already indexed `criteria`. If `criteria` is not already indexed, behavior will be akin to `add`.
- Successive `add` operations to index a given `criteria` (identified by `criteriaId`) are not be allowed.
- For changes in any Criteria thats already indexed to reflect in the index, `update` is the way to go.
- Post the `update` operation, for all practical purposes, only the newer version of `criteria` will be considered for searches.


#### Deleting an already indexed Criteria

Delete an already indexed criteria.

```java
engine.delete("index_name", criteria)
```

PS :
- `delete` is limited to only already indexed `criteria`.
- Post the `delete` operation, for all practical purposes, deleted `criteria` will not be considered for searches.


#### Support for scanning

Mustang provides support for scanning a list of `Criteria` against a `context` and arriving at the satisfying ones.

```java
List<Criteria> matchingCriterias = engine.scan(criterias, context);
```

#### Support for evaluating a specific criteria

A specific `Criteria` can also be evaluated against a given `context` to pull out the result.

```java
boolean result = evaluate(criteria, context);
```

#### Index Replacement

At times we may need to update/delete a bunch of `Criteria`s. Also, we may not know which all `Criteria`s have already been indexed that needs deletion. In such cases, it is recommended to go for building a new index ground-up and replace it with the existing required index.  So, one can build up a temporary index and replace this temporary index with the existing / old index. Index replacement is an atomic operation. Creation of a temporary index would need extra head room in the heap but wouldn't hold onto the extra memory post replacement.

```java
replace(oldIndex, newIndex);
```


#### Index Ratification

Ratification is a predictable way of identifying anomalies in search results for a given index. Its a very detailed process that looks out for discrepancies between the search results and the scan results for all possible `Query` combinations. As the size of the index grows, needless to say, this will take more time and hence should be used judiciously and sparingly. Suggested way is to invoke ratification when changes done onto an index (such as `add`,`update`,`delete`,`replace`) are SUSPECT.

```java
engine.ratify(indexName); // This triggers the ratification process in the background
RatificationResult result = engine.getRatificationResult(indexName); // Check back the results after a while
```


#### Note on backward compatibility

`2.0.0` and above are fully backward compatible with `1.x.y`. Necessary transformations are implicitly taken care of. So, all clients are recommended to upgrade to `2.0.0` and above.



