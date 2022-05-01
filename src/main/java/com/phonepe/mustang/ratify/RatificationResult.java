/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.ratify;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RatificationResult {
    private final Boolean status;
    private final Combinations combinations;
    private final Set<RatificationDetail> anamolyDetails;
    private final long timeTakenMs;
    private final long requestedAt;
    private final boolean fullFledgedRun;
    private final long ratifiedAt;

    @Data
    @Builder
    public static class Combinations {
        private final int totalCount;
        private final int cpCount;
        private final int ssCount;

    }
}
