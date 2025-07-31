package io.intellimill.orbit.agent.memory;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @author Sun Yuhan
 */
public interface AgentMemory {

	void add(String conversationId, Message message);

	void add(String conversationId, List<Message> messages);

	List<Message> getAll(String conversationId);

	Message getLast(String conversationId);

	void clear(String conversationId);

}
