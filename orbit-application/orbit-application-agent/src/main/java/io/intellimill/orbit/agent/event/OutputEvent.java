package io.intellimill.orbit.agent.event;

/**
 * @author Sun Yuhan
 */
public class OutputEvent extends Event {

	private final String content;

	public OutputEvent(String content) {
		this.content = content;
	}

	@Override
	public String getContent() {
		return this.content;
	}

}
