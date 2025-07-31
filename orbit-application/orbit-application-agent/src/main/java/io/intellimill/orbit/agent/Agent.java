package io.intellimill.orbit.agent;

import io.intellimill.orbit.agent.result.StepResult;
import io.intellimill.orbit.agent.state.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base agent
 * <p>
 * For the principles of Agent, refer to the paper:
 * <a href="https://arxiv.org/abs/2210.03629">ReAct: Synergizing Reasoning and Acting in
 * Language Models</a>
 * </p>
 *
 * @author Sun Yuhan
 */
public abstract class Agent {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AgentState agentState;

	private final int maxSteps;

	private int currentStep;

	public Agent(int maxSteps) {
		this.maxSteps = maxSteps;
		this.agentState = AgentState.PENDING;
		this.currentStep = 0;
	}

	public abstract StepResult step();

	/**
	 * Agent main loop
	 * @return Agent execution result
	 */
	public String run() {
		LocalDateTime startTime = LocalDateTime.now();
		List<String> results = new ArrayList<>();

		try {
			while (currentStep < maxSteps && agentState != AgentState.COMPLETED) {
				currentStep++;

				logger.info("Executing step: ( {} / {} )", currentStep, maxSteps);

				results.add(step().getResult());
			}

			if (currentStep >= maxSteps) {
				results.add("Execution terminated: maximum steps reached: " + maxSteps);
			}
		}
		catch (Exception e) {
			logger.error("Exception occurred during Agent execution", e);
			this.agentState = AgentState.FAILED;
		}

		long elapsedTimeMillis = Duration.between(startTime, LocalDateTime.now()).toMillis();

		logger.info("Agent execution elapsed time: {} ms", elapsedTimeMillis);

		return results.isEmpty() ? "" : results.get(results.size() - 1);
	}

	protected void updateState(AgentState agentState) {
		this.agentState = agentState;
	}

}
