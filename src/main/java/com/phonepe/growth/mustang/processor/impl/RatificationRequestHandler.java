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
package com.phonepe.growth.mustang.processor.impl;

import com.google.common.eventbus.Subscribe;
import com.phonepe.growth.mustang.processor.IMessageHandler;
import com.phonepe.growth.mustang.ratify.RatificationResult;
import com.phonepe.growth.mustang.ratify.Ratifier;

public class RatificationRequestHandler implements IMessageHandler<RatificationRequest> {

    @Override
    @Subscribe
    public void handle(final RatificationRequest message) {
        // Override previous result
        message.getIndexGroup()
                .setRatificationResult(RatificationResult.builder()
                        .requestedAt(message.getRequestedAt())
                        .build());

        final RatificationResult result = Ratifier.builder()
                .mapper(message.getMapper())
                .indexGroup(message.getIndexGroup())
                .build()
                .ratify();
        message.getIndexGroup()
                .setRatificationResult(result);
    }

}
