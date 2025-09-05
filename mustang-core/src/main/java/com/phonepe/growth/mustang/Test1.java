package com.phonepe.growth.mustang;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.detail.impl.EqualityDetail;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class Test1 {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException {

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.paymentSources[?(@.type == 'CREDIT_CARD')].cardIssuer")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("AMEX",
                                                "MAESTRO",
                                                "BAJAJ",
                                                "DISCOVER",
                                                "JCB",
                                                "MAESTRO_16",
                                                "MASTER_CARD",
                                                "RUPAY",
                                                "DINERS_CLUB",
                                                "MAESTRO_16_OPT"))
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("F"))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .build())
                .build();
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();
        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3, 4))
                                .build())
                        .build())
                .build();
        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA", "NY"))
                                .build())
                        .build())
                .build();
        
        
        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        
        Criteria c7 = mapper.readValue("{\"form\":\"DNF\",\"id\":\"CUSTOM\",\"conjunctions\":[{\"type\":\"AND\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.data.accounting_events#data.transaction.modes[*].type\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"AMEX\",\"UPI_FULFILMENT\"]},\"weight\":1}]}]}", Criteria.class);
        String searchContext = "{\"metaData\":{\"channel\":{\"type\":\"LUCY\",\"source\":{\"type\":\"SINGLE_TOPIC\",\"facet\":{\"topicName\":\"prd_lucy.debeziumDataTopic__v4__galeranexusdbcr__nexus__accounting_events_zfs\",\"dbName\":\"nexus\",\"tableName\":\"accounting_events\",\"eventIngestionTime\":1734941053825,\"partitionId\":1,\"offset\":16270}}}},\"data\":{\"accounting_events#transaction_id\":\"NX24122313340313450152641\",\"accounting_events#partition_id\":52,\"accounting_events#reconciled\":0,\"accounting_events#event_type\":\"MERCHANT_FULFILMENT\",\"accounting_events#reference_id\":\"NX24122313340313450152641\",\"accounting_events#data\":{\"header\":{\"eventType\":\"MERCHANT_FULFILMENT\",\"merchantSettlementDisabled\":false,\"transactionId\":\"T2412231334031578919232\",\"externalTransactionId\":\"NX24122313340313450152641\",\"paymentId\":\"T2412231334031578919232\",\"transactionDate\":1734941046000,\"state\":null,\"entryFor\":null,\"transactionType\":null,\"compositeEvent\":true,\"originalTransactionId\":null,\"originalTransactionDate\":0},\"transaction\":{\"type\":\"MERCHANT_FULFILMENT\",\"amount\":29900,\"mcc\":\"4814\",\"merchant\":\"JIOINAPPDIRECT1\",\"modes\":[{\"type\":\"UPI_FULFILMENT\",\"state\":null,\"amount\":29900,\"cardNetwork\":null}],\"merchantBizType\":\"LARGE\"}},\"accounting_events#updated_at\":1734941048219,\"accounting_events#shard_id\":7,\"accounting_events#created_at\":1734941048219,\"accounting_events#id\":2690422641,\"accounting_events#payment_reference_id\":\"T2412231334031578919232\"}}";
        final RequestContext requestContext = RequestContext.builder()
                .node(mapper.readValue(searchContext, JsonNode.class))
                .build();

        final RequestContext requestContext1 = RequestContext.builder()
                .node(mapper.readValue(searchContext, JsonNode.class))
                .build();
        
        
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        //engine.add("testsearch", c1);
        // engine.add("testsearch", c2);
        // engine.add("testsearch", c3);
        // engine.add("testsearch", c4);
        // engine.add("testsearch", c5);
        // engine.add("testsearch", c6);
        
        engine.add("testsearch", c7);

        /**
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("s", "CA");// C2 value
        testQuery.put("g", "M");// C3 value
        // Search query for same criteria

        String query = "{\"paymentSources\":[{\"type\":\"CREDIT_CARD\",\"cardIssuer\":\"AMEX\"},{\"type\":\"DEBIT_CARD\",\"cardIssuer\":\"AMEX\"}]}";

        String ocf = "{\"metaData\":{\"channel\":{\"type\":\"LUCY\",\"source\":{\"type\":\"SINGLE_TOPIC\",\"facet\":{\"topicName\":\"prd_lucy.debeziumDataTopic__v4__galeranexusdbcr__nexus__accounting_events_zfs\",\"dbName\":\"nexus\",\"tableName\":\"accounting_events\",\"eventIngestionTime\":1734941053825,\"partitionId\":1,\"offset\":16270}}}},\"data\":{\"accounting_events.transaction_id\":\"NX24122313340313450152641\",\"accounting_events.partition_id\":52,\"accounting_events.metadata\":{\"purposeCode\":\"10\",\"skipRecon\":false,\"skipSend\":false,\"convenienceFeeAmount\":null,\"convenienceFeeGST\":null,\"platformFeeAmount\":null,\"platformFeeGST\":null},\"accounting_events.reconciled\":0,\"accounting_events.event_type\":\"MERCHANT_FULFILMENT\",\"accounting_events.reference_id\":\"NX24122313340313450152641\",\"accounting_events.data\":{\"header\":{\"eventType\":\"MERCHANT_FULFILMENT\",\"merchantSettlementDisabled\":false,\"transactionId\":\"T2412231334031578919232\",\"externalTransactionId\":\"NX24122313340313450152641\",\"paymentId\":\"T2412231334031578919232\",\"transactionDate\":1734941046000,\"state\":null,\"entryFor\":null,\"transactionType\":null,\"compositeEvent\":true,\"originalTransactionId\":null,\"originalTransactionDate\":0},\"transaction\":{\"type\":\"MERCHANT_FULFILMENT\",\"amount\":29900,\"mcc\":\"4814\",\"merchant\":\"JIOINAPPDIRECT1\",\"modes\":[{\"type\":\"UPI_FULFILMENT\",\"state\":null,\"amount\":29900,\"cardNetwork\":null}],\"merchantBizType\":\"LARGE\"},\"context\":{\"serviceType\":\"PREPAID_RECHARGE\",\"providerName\":\"JIOINAPPDIRECT1\",\"providerId\":\"JIO\",\"authenticator1\":\"REGULAR\",\"authenticator2\":null,\"productId\":null,\"userId\":\"U1611140559251156443532\",\"stateCode\":\"JH\",\"terminalInstrument\":null,\"cfTotalAmount\":0,\"pfTotalAmount\":0,\"feeType\":null,\"terminalInstrumentList\":null,\"associatedEvent\":[\"MERCHANT_FULFILMENT\",\"PLATFORM_CHARGE\"],\"paymentTags\":[\"MULTI_SAC_REQUEST\"],\"tenant\":\"NEXUS\",\"tenantMapping\":[{\"eventType\":\"MERCHANT_FULFILMENT\",\"tenant\":\"NEXUS\"},{\"eventType\":\"PLATFORM_CHARGE\",\"tenant\":\"PEGASUS\"}],\"version\":null}},\"accounting_events.updated_at\":1734941048219,\"accounting_events.shard_id\":7,\"accounting_events.created_at\":1734941048219,\"accounting_events.id\":2690422641,\"accounting_events.payment_reference_id\":\"T2412231334031578919232\"}}";
        OrbisCommonFormat ocfObj = mapper.readValue(ocf, OrbisCommonFormat.class);
        JsonNode ocfNode = mapper.convertValue(ocfObj, JsonNode.class);
        System.out.println(ocfNode.toPrettyString());

        JsonNode valueToTree = mapper.valueToTree(query);
        JsonNode readTree = mapper.readTree(query);
        JsonNode readValue = mapper.readValue(query, JsonNode.class);
        JsonNode convertValue = mapper.convertValue(query, JsonNode.class);

        System.out.println(valueToTree.toPrettyString());
        System.out.println(readTree.toPrettyString());
        System.out.println(readValue.toPrettyString());
        System.out.println(convertValue.toPrettyString());

        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        // .node(mapper.valueToTree(query))
                        // .node(mapper.readTree(query))
                        .node(mapper.readValue(query, JsonNode.class))
                        // .node(mapper.convertValue(query, JsonNode.class))
                        .build());
        System.out.println(searchResults);
        engine.ratify("testsearch");
        System.out.println(engine.getRatificationResult("testsearch"));
        
        */
        
        
        
        final Set<String> searchResults = engine.search("testsearch",
                requestContext);
        System.out.println(searchResults);
        //engine.ratify("testsearch");
        //System.out.println(engine.getRatificationResult("testsearch"));
        final Set<String> scanResults = engine.scan("testsearch",
                requestContext1);
        System.out.println(scanResults);
        
        

    }

}
