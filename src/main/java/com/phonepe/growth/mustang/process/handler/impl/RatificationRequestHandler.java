/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.process.handler.impl;

import java.util.Objects;

import com.google.common.eventbus.Subscribe;
import com.phonepe.growth.mustang.process.handler.ITaskHandler;
import com.phonepe.growth.mustang.ratify.RatificationRequest;
import com.phonepe.growth.mustang.ratify.RatificationResult;
import com.phonepe.growth.mustang.ratify.Ratifier;

public class RatificationRequestHandler implements ITaskHandler<RatificationRequest> {

    @Override
    @Subscribe
    public void handle(final RatificationRequest message) {
        final RatificationResult currentResult = message.getIndexGroup()
                .getRatificationResult();
        // To guard against repeat invocations before the previous one gets over.
        if (Objects.isNull(currentResult)
                || (currentResult.getRatifiedAt() > 0 && currentResult.getRatifiedAt() < message.getRequestedAt())) {
            // Override previous result
            message.getIndexGroup()
                    .setRatificationResult(RatificationResult.builder()
                            .requestedAt(message.getRequestedAt())
                            .build());

            final RatificationResult result = Ratifier.builder()
                    .mapper(message.getMapper())
                    .indexGroup(message.getIndexGroup())
                    .requestedAt(message.getRequestedAt())
                    .build()
                    .ratify();
            message.getIndexGroup()
                    .setRatificationResult(result);
        }
    }

}
