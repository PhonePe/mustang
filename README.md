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
## Usage

### Initializing Mustang Engine

```java
ObjectMapper mapper = new ObjectMapper();
MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
``` 

### Defining criteria
Mustang Engine supports DNF and CNF criteria, Crieteria is combination of predicates,
as part of Mustang we support two type of predicate as of now, Include and Exlude predicate

A Boolean Expression is either a DNF (i.e., disjunctive normal form) or CNF
(i.e., conjunctive normal form) expression of the basic ∈ and ∉
predicates.
  
#### Defining Include predicate 
```java
IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build()
```

#### Defining Exclude predicate 
```java
ExcludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")
```

#### Defining DNF criteria
DNF expression is of the form:
```
DNF_crieteria = (A ∈ {a1, a2} ∧ B ∉ {b1, b2} ∧ C ∈ {c1})∨ (A ∈ {a1, a3} ∧
D ∉ {d1})
```
In summary DNF is disjunctive combination of the conjuctions. To define a DNF criteria 
``` java
Criteria C1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2", "A3")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet("4", "5", "6")).build())
                .build()).build();
```

#### Defining CNF criteria
CNF expression is of the form:
```
CNF_crieteria = age ∈ {2, 3}∧(state ∈ {CA} ∨ gender ∈ {F})
```
In summary CNF is Conjuctive combination of the disjunctions.
To define a CNF criteria 
``` java
Criteria c1 = CNFCriteria.builder().id("C1").disjunction(Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n")
                        .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003)).build())
                .predicate(IncludedPredicate.builder().lhs("$.p").values(Sets.newHashSet(true)).build()).build())
                .build();
```

#### Indexing criteria
```java
engine.index("index_name", criteria)
```
or 
```java
engine.index("index_name", Arrays.asList(criteria1, criteria2, criteria3));
```

#### Defining assignment
An assignment is a set of attribute name and value pairs {A1 = v1, A2 = v2, . . .}
Ex such as: 
`{gender = F, state = CA}`

To define an assignment and search on index 
```java
Map<String, Object> testQuery = Maps.newHashMap();
testQuery.put("a", "A1");
testQuery.put("b", "B3");
testQuery.put("n", 5);
testQuery.put("p", true);

EvaluationContext context = EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build();

```

#### Searching over Index

To search a context over a index 

``` java
Set<String> searchResults = engine.search("index_name",context);
```
This will return set of Id's of all the matching criteria