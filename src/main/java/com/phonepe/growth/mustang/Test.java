package com.phonepe.growth.mustang;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.growth.mustang.ratify.RatificationResult;

public class Test {
    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.ref.matchingFences.FE1633083938636")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.ref.matchingFences.FE1633083938637")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        String str = "{   \"app\": \"dummy\",   \"disbursementFor\": \"OF2110270119465155147102\",   \"userId\": \"U1611110853428089119028\",   \"transactionId\": \"BD2110272044476420000904\",   \"globalPaymentId\": \"BD2110272044476420000904\",   \"ref\": {        } }";

        final RequestContext context = RequestContext.builder()
                .node(mapper.readValue(str, JsonNode.class))
                .build();
        // Search query for same criteria
        final Set<String> searchResults = engine.search("testsearch", context);

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");

        System.out.println(searchResults);
        //System.out.println(engine.scan(Arrays.asList(c1, c2), context));
        System.out.println(ratificationResult);

    }

}
