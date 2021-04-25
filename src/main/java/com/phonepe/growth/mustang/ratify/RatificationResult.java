package com.phonepe.growth.mustang.ratify;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RatificationResult {
    private boolean ratified;
    private String indexName;
    private int combinations;
    private Set<RatificationDetail> anamolyDetails;
    private long timeTaken;

    @Override
    public String toString() {
        if (ratified) {
            return String.format("***[[ Successfully ratified the index {%s} across %s combinations in %s ms]]***",
                    indexName,
                    combinations,
                    timeTaken);
        }
        return String.format("** [[ Ratification identified below anamolies for index {%s} in %s ms ]] **\n%s",
                indexName,
                timeTaken,
                anamolyDetails);
    }

}
