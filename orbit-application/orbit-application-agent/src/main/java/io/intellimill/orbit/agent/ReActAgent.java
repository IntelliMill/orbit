package io.intellimill.orbit.agent;

import io.intellimill.orbit.agent.result.StepResult;
import io.intellimill.orbit.agent.state.AgentState;

/**
 * ReActAgent
 *
 * @author Sun Yuhan
 */
public abstract class ReActAgent extends Agent {

	public ReActAgent(int maxSteps) {
		super(maxSteps);
	}

	protected abstract boolean think();

	protected abstract StepResult act();

	@Override
	public StepResult step() {
		return think() ? act()
				: StepResult.builder()
					.state(AgentState.RUNNING)
					.result("Thinking completed, no action required")
					.build();
	}

}
