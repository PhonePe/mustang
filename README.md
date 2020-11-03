# Mustang

Mustang solves the problem of efficiently indexing Boolean expressions (both Disjunctive Normal Form (DNF) 
and Conjunctive Normal Form (CNF)) a high-dimensional multi-valued attribute space. The goal is to rapidly find the set of 
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
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
## Overview

Mustang allows indexing Boolean Expressions through a notion called `Criteria`, which can be either of the two types :

- DNF : Disjunctive Normal Form, which is a disjunction of conjunctions

	`(A ∈ {a1, a2} ∧ B ∉ {b1, b2} ∧ C ∈ {c1}) ∨ (A ∈ {a1, a3} ∧ D ∉ {d1})`

- CNF : Conjunctive Normal Form, which is a conjunction of disjunctions

	`(A ∈ {a1, a2} ∨ B ∉ {b1, b2} ∨ C ∈ {c1}) ∧ (A ∈ {a1, a3} ∨ D ∉ {d1})`


`Composition` is a set of `Predicate`(s). Depending upon how the constituent results are considered, it could be either :

- `Conjunction` is satisfied only when all constituent predicates evaluate to true.
- `Disjunction` is satisfied when any of the constituent predicates evaluate to true.



`Predicate` is a conditional that supports the below operators.

- `INCLUDED(∈)` is satisfied when any one of the given values match.
- `EXCLUDED(∉)` is satisfied when none of the given values match.

Further, Mustang allows for logical grouping of `Criteria`(s) when indexing through identification by a name.
`Criteria` of any form can be indexed into an index-group. And searches are always directed to a specific index-group.


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
			                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2", "A3")).build())
			                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet(4,5,6)).build())
			        .build())
			  .build();
```

#### Defining CNF criteria

``` java
Criteria cnf = CNFCriteria.builder()
					.id("C2") // id we would get back should this criteria match a given assignment
					.disjunction(Disjunction.builder()
			                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
			                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
			                .predicate(IncludedPredicate.builder().lhs("$.n")
			                        .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003)).build())
			                .predicate(IncludedPredicate.builder().lhs("$.p").values(Sets.newHashSet(true)).build())
		            .build())
              .build();
```

#### Indexing criteria

Index a single criteria

```java
engine.index("index_name", criteria)
```

OR 

Multiple criteria(s) at once.

```java
engine.index("index_name", Arrays.asList(criteria1, criteria2, ...));
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

which returns a set of id(s) of all matching criteria(s).