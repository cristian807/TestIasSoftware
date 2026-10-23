package com.ias_software.test.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ias_software.test.domain.service.UsageEvaluationService;

@Configuration
public class BeanConfig {

	@Bean
	public Clock clock() {

        return Clock.systemUTC();
	}

	@Bean
	public UsageEvaluationService usageEvaluationService() {

        return new UsageEvaluationService();
	}
}
