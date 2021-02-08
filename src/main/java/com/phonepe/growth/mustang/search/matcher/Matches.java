package com.phonepe.growth.mustang.search.matcher;

import java.util.Map;
import java.util.concurrent.Future;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Matches {
    private final Future<Map<String, Double>> probables;

}
