/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.process.handler.impl;

import java.util.Objects;

import com.google.common.eventbus.Subscribe;
import com.phonepe.mustang.process.handler.ITaskHandler;
import com.phonepe.mustang.ratify.RatificationRequest;
import com.phonepe.mustang.ratify.RatificationResult;
import com.phonepe.mustang.ratify.Ratifier;

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
                    .fullFledged(message.isFullFledged())
                    .requestedAt(message.getRequestedAt())
                    .build()
                    .ratify();
            message.getIndexGroup()
                    .setRatificationResult(result);
        }
    }

}
