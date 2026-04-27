/*
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonepe.mustang;

import com.phonepe.mustang.resources.MustangDebugResource;
import com.phonepe.mustang.resources.MustangScanResource;
import com.phonepe.mustang.resources.MustangSearchResource;
import com.phonepe.mustang.response.MustangExceptionMapper;
import com.phonepe.mustang.service.impl.DebugServiceImpl;
import com.phonepe.mustang.service.impl.ScanServiceImpl;
import com.phonepe.mustang.service.impl.SearchServiceImpl;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MustangBundle<T extends Configuration> implements ConfiguredBundle<T> {
    public static final String MUSTANG_PERMISSION = "mustang_operator";
    private MustangEngine mustangEngine;

    @Override
    public void run(T config, Environment environment) {
        this.mustangEngine = MustangEngine.builder()
                .mapper(environment.getObjectMapper())
                .build();

        environment.jersey()
                .register(new MustangDebugResource(DebugServiceImpl.builder()
                        .engine(mustangEngine)
                        .build()));
        environment.jersey()
                .register(new MustangScanResource(ScanServiceImpl.builder()
                        .engine(mustangEngine)
                        .build()));
        environment.jersey()
                .register(new MustangSearchResource(SearchServiceImpl.builder()
                        .engine(mustangEngine)
                        .build()));

        environment.jersey()
                .register(new MustangExceptionMapper());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Nothing to initialise
    }

    public abstract MustangConfig getMustangConfig(T config);

}
