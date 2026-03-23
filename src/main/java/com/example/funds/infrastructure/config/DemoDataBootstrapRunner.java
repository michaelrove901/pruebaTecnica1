package com.example.funds.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.bootstrap.demo-data", havingValue = "true")
public class DemoDataBootstrapRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataBootstrapRunner.class);

    private final DemoDataSeeder demoDataSeeder;

    public DemoDataBootstrapRunner(DemoDataSeeder demoDataSeeder) {
        this.demoDataSeeder = demoDataSeeder;
    }

    @Override
    public void run(ApplicationArguments args) {
        demoDataSeeder.seedDemoDataIfMissing();
        demoDataSeeder.logDemoCredentials();
        LOGGER.info("Demo data bootstrap finished because app.bootstrap.demo-data=true");
    }
}
