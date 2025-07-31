package io.intellimill.orbit.agent.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AgentMemory implementation based on Spring AI's
 * {@code org.springframework.ai.chat.memory.ChatMemory}
 *
 * @author Sun Yuhan
 */
@Component
public class DefaultAgentMemory implements AgentMemory {

	private final ChatMemory chatMemory;

	public DefaultAgentMemory(ChatMemory chatMemory) {
		this.chatMemory = chatMemory;
	}

	@Override
	public void add(String conversationId, Message message) {
		this.chatMemory.add(conversationId, message);
	}

	@Override
	public void add(String conversationId, List<Message> messages) {
		this.chatMemory.add(conversationId, messages);
	}

	@Override
	public List<Message> getAll(String conversationId) {
		return this.chatMemory.get(conversationId);
	}

	@Override
	public Message getLast(String conversationId) {
		List<Message> messages = this.getAll(conversationId);
		return messages.isEmpty() ? null : messages.get(messages.size() - 1);
	}

	@Override
	public void clear(String conversationId) {
		this.chatMemory.clear(conversationId);
	}

}
