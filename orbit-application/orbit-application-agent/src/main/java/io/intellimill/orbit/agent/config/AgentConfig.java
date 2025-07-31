package io.intellimill.orbit.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sun Yuhan
 */
@Configuration
@ConfigurationProperties(prefix = "orbit")
public class AgentConfig {

	private Integer maxSteps;

	public Integer getMaxSteps() {
		return maxSteps;
	}

	public void setMaxSteps(Integer maxSteps) {
		this.maxSteps = maxSteps;
	}

}
