# Mustang

	Mustang solves the problem of efficiently indexing Disjunctive Normal Form (DNF) and Conjunctive Normal Form (CNF) 
Boolean expressions over a high-dimensional multi-valued attribute space. The goal is to rapidly find the set of 
Boolean expressions that evaluate to true for a given assignment of values to attributes. A solution to this problem 
has applications in online advertising (where a Boolean expression represents an advertiser’s user targeting 
requirements, and an assignment of values to attributes represents the characteristics of a user visiting an online 
page) and in general any publish/subscribe system (where a Boolean expression represents a subscription, and an 
assignment of values to attributes represents an event). All existing solutions that we are aware of can only index a 
specialized subset of conjunctive and/or disjunctive expressions, and cannot efficiently handle general DNF and CNF 
expressions (including NOTs) over multi-valued attributes.

	Mustang presents a novel solution based on the inverted list data structure that enables us to index arbitrarily 
complex DNF and CNF Boolean expressions over multi-valued attributes. An interesting aspect of our solution is that, 
by virtue of leveraging inverted lists traditionally used for ranked information retrieval, we can efficiently return 
the top-N matching Boolean expressions. This capability enables applications such as ranked publish/subscribe systems, 
where only the top subscriptions that match an event are desired. For example, in online advertising there is a limit 
on the number of advertisements that can be shown on a given page and only the “best” advertisements can be displayed.



## Add Maven Dependency

```xml
<dependency>
  <groupId>com.phonepe.growth</groupId>
  <artifactId>mustang</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
