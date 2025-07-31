package io.intellimill.orbit.agent.result;

import io.intellimill.orbit.agent.state.AgentState;

/**
 * @author Sun Yuhan
 */
public class StepResult {

	private final AgentState state;

	private final String result;

	private StepResult(AgentState state, String result) {
		this.state = state;
		this.result = result;
	}

	public AgentState getState() {
		return state;
	}

	public String getResult() {
		return result;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private AgentState state;

		private String result;

		public Builder state(AgentState state) {
			this.state = state;
			return this;
		}

		public Builder result(String result) {
			this.result = result;
			return this;
		}

		public StepResult build() {
			return new StepResult(state, result);
		}

	}

}
