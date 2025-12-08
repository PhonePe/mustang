package com.phonepe.central.mustang;

import com.phonepe.central.mustang.resources.MustangDebugResource;
import com.phonepe.central.mustang.resources.MustangScanResource;
import com.phonepe.central.mustang.resources.MustangSearchResource;
import com.phonepe.central.mustang.response.MustangExceptionMapper;
import com.phonepe.central.mustang.service.impl.DebugServiceImpl;
import com.phonepe.central.mustang.service.impl.ScanServiceImpl;
import com.phonepe.central.mustang.service.impl.SearchServiceImpl;
import com.phonepe.growth.mustang.MustangEngine;

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
