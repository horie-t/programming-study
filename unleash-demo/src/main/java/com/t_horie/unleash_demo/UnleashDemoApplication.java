package com.t_horie.unleash_demo;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UnleashDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnleashDemoApplication.class, args);

		// UnleashClientを初期化
		UnleashConfig config = UnleashConfig.builder()
				.appName("unleash-onboarding-java")
				.instanceId("unleash-onboarding-instance")
				.unleashAPI("http://localhost:4242/api/")
				.apiKey("default:development.unleash-insecure-api-token") // in production use environment variable
				.build();

		Unleash unleash = new DefaultUnleash(config);

		while (true) {
			boolean featureEnabled = unleash.isEnabled("support-due-date-time");
			System.out.println("Feature enabled: " + featureEnabled);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
	}

}
