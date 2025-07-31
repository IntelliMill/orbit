package io.intellimill.orbit.agent.context;

import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sun Yuhan
 */
public class AgentContext {

	private List<ToolCallback> availableTools = new ArrayList<>();

	private String conversationId;

	private String query;

	private EventEmitter emitter;

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<ToolCallback> getTools() {
		return availableTools;
	}

	public void setTools(List<ToolCallback> availableTools) {
		this.availableTools = availableTools;
	}

	public EventEmitter getEmitter() {
		return emitter;
	}

	public void setEmitter(EventEmitter emitter) {
		this.emitter = emitter;
	}

}
