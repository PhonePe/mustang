package com.phonepe.growth.mustang;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.search.ranking.RankingStrategy;

public class Test3 {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        MustangEngine engine = new MustangEngine(mapper, RankingStrategy.EXPLICIT_WEIGHTS);

        Criteria c1 = mapper.readValue(
                """
                       {"form":"DNF","id":"BPA2505191339151842806157#HERMES_MEC_V3#transactionAmount","conjunctions":[{"type":"AND","predicates":[{"type":"INCLUDED","lhs":"$.metaData.channel.source.facet.dbName","detail":{"caveat":"EQUALITY","values":["hermes"]},"weight":1},{"type":"INCLUDED","lhs":"$.metaData.channel.source.facet.topicName","detail":{"caveat":"REGEX","regex":"%hermes%"},"weight":1},{"type":"INCLUDED","lhs":"$.data.accounting_events#data.transaction.modes[*].type","preOperation":{"type":"IDENTITY"},"detail":{"caveat":"EQUALITY","values":["UPI_FULFILMENT","WALLET_FULFILMENT"]},"weight":1}]}]}
                        """,
                Criteria.class);

        Criteria c2 = mapper.readValue(
                """
                        {"form":"DNF","id":"BPA2501201553285550773614#PAYABLE_3_NEXUS_MEC_V6#transactionAmount","conjunctions":[{"type":"AND","predicates":[{"type":"INCLUDED","lhs":"$.metaData.channel.source.facet.dbName","detail":{"caveat":"EQUALITY","values":["nexus"]},"weight":1},{"type":"INCLUDED","lhs":"$.metaData.channel.source.facet.topicName","detail":{"caveat":"REGEX","regex":"%nexus%"},"weight":1},{"type":"INCLUDED","lhs":"$.data.accounting_events#data.transaction.modes[*].type","preOperation":{"type":"IDENTITY"},"detail":{"caveat":"EQUALITY","values":["UPI_FULFILMENT","WALLET_FULFILMENT"]},"weight":1}]}]}
                        """,
                Criteria.class);

        engine.add("test", c1);
        engine.add("test", c2);

        final RequestContext requestContext = RequestContext.builder()
                .node(mapper.readValue(
                        """
                                {"metaData":{"channel":{"type":"LUCY","source":{"type":"SINGLE_TOPIC","facet":{"topicName":"prd_lucy.debeziumDataTopic__v4__galerahermesdb__hermes__accounting_events_zfs","dbName":"hermes","tableName":"accounting_events","eventIngestionTime":1750514845982,"partitionId":25,"offset":168934856}}}},"data":{"accounting_events#transaction_id":"T2506211937029543374154","accounting_events#partition_id":25,"accounting_events#event_type":"MERCHANT_FULFILMENT","accounting_events#reconciliation_state":"IN_PROGRESS","accounting_events#data":{"header":{"eventType":"MERCHANT_FULFILMENT","transactionId":"T2506211937029543374154","paymentId":"T2506211937029543374154","externalTransactionId":"9593332855IDvFYxrPFU","transactionDate":1750514817623,"compositeEvent":true},"transaction":{"type":"MERCHANT_FULFILMENT","amount":17400,"mcc":"5691","merchant":"MYNTRA","modes":[{"type":"UPI_FULFILMENT","amount":7400},{"type":"WALLET_FULFILMENT","amount":10000}],"merchantBizType":"LARGE"},"context":{"serviceType":"TRANSACTION","authenticator2":"CONTAINER","providerId":"MYNTRA"}},"accounting_events#updated_at":1750514843088,"accounting_events#shard_id":4,"accounting_events#meta_info":{"offerAdjustments":[]},"accounting_events#created_at":1750514843088,"accounting_events#id":989388348,"accounting_events#farm_id":"NBX"}}
                                """,
                        JsonNode.class))
                .build();
        final Set<String> searchResults = engine.search("test", requestContext);
        System.out.println(searchResults);

        engine.ratify("test");
        System.out.println(engine.getRatificationResult("test"));
        System.out.println(engine.scan("test", requestContext));

    }

}
