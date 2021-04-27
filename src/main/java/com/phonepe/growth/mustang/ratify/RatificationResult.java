package com.phonepe.growth.mustang.ratify;

import java.util.Set;

import com.google.common.collect.Iterables;

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
            return String.format("===[[ Successfully ratified the index - %s across %s combinations in %s ms]]===",
                    indexName,
                    combinations,
                    timeTaken);
        }
        return String.format(
                "===[[ Ratification identified - %s anamolies out of %s combinations for index - %s in %s ms ]]===%n%s",
                anamolyDetails.size(),
                combinations,
                indexName,
                timeTaken,
                Iterables.toString(anamolyDetails));
    }

}
