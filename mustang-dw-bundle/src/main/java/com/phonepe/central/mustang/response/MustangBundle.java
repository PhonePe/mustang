package com.phonepe.central.mustang.response;

import com.phonepe.central.mustang.resources.MustangDebugResource;
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
    private MustangEngine mustangEngine;

    @Override
    public void run(T config, Environment environment) {
        this.mustangEngine = MustangEngine.builder()
                .mapper(environment.getObjectMapper())
                .build();

        environment.jersey()
                .register(new MustangDebugResource(mustangEngine, environment.getObjectMapper()));

        environment.jersey()
                .register(new MustangExceptionMapper());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Nothing to initialise
    }

}
