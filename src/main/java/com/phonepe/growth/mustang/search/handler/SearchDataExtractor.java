package com.phonepe.growth.mustang.search.handler;

import java.util.Map;
import java.util.concurrent.Future;

import com.phonepe.growth.mustang.exception.MustangException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchDataExtractor {

    public static Map<String, Double> extract(final Future<Map<String, Double>> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw MustangException.propagate(e);
        }
    }

}