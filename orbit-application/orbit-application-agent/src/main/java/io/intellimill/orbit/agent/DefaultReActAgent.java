package io.intellimill.orbit.agent;

import io.intellimill.orbit.agent.config.AgentConfig;
import io.intellimill.orbit.agent.context.AgentContext;
import io.intellimill.orbit.agent.event.ThinkingEvent;
import io.intellimill.orbit.agent.memory.AgentMemory;
import io.intellimill.orbit.agent.prompt.AgentSystemPrompt;
import io.intellimill.orbit.agent.result.StepResult;
import io.intellimill.orbit.agent.state.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Sun Yuhan
 */
public class DefaultReActAgent extends ReActAgent {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AgentMemory memory;

	private final AgentContext agentContext;

	private final ChatClient chatClient;

	private final ToolCallingManager toolCallingManager;

	private Prompt prompt;

	private ChatResponse chatResponse;

	public DefaultReActAgent(AgentMemory memory, AgentConfig agentConfig, AgentContext agentContext,
			ChatClient chatClient, ToolCallingManager toolCallingManager) {
		super(agentConfig.getMaxSteps());
		this.memory = memory;
		this.agentContext = agentContext;
		this.chatClient = chatClient;
		this.toolCallingManager = toolCallingManager;
	}

	@Override
	protected boolean think() {
		String conversationId = this.agentContext.getConversationId();
		List<Message> messages = buildMemory(conversationId);

		ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
			.internalToolExecutionEnabled(false)
			.build();
		this.prompt = Prompt.builder().messages(messages).chatOptions(chatOptions).build();

		try {
			Flux<ChatResponse> response = chatClient.prompt(prompt)
				.toolCallbacks(this.agentContext.getTools())
				.stream()
				.chatResponse();

			processModelThinkResponse(response);
		}
		catch (Exception e) {
			logger.error("Exception occurred during thinking", e);
			AssistantMessage assistantMessage = new AssistantMessage(
					"Exception occurred while processing task:" + e.getMessage());
			this.memory.add(conversationId, assistantMessage);
			updateState(AgentState.COMPLETED);
			return false;
		}

		return true;
	}

	@Override
	protected StepResult act() {
		String conversationId = this.agentContext.getConversationId();
		if (CollectionUtils.isEmpty(this.chatResponse.getResult().getOutput().getToolCalls())) {
			updateState(AgentState.COMPLETED);
			String content = this.memory.getLast(conversationId).getText();
			return StepResult.builder().state(AgentState.COMPLETED).result(content).build();
		}

		ToolExecutionResult toolExecutionResult = this.toolCallingManager.executeToolCalls(this.prompt,
				this.chatResponse);

		// Save tool call memory
		List<Message> messages = toolExecutionResult.conversationHistory();
		this.memory.add(conversationId, messages);

		ToolResponseMessage toolResponseMessage = (ToolResponseMessage) messages.get(messages.size() - 1);
		List<ToolResponseMessage.ToolResponse> toolResponses = toolResponseMessage.getResponses();

		return null;
	}

	/**
	 * Build memory
	 * <p>
	 * Construct system prompt and add it to the beginning of memory. If the last entry in
	 * history is not a user message, append a user message. Finally, assemble and return
	 * all memory entries.
	 * </p>
	 * @param conversationId conversation id
	 * @return memory
	 */
	private List<Message> buildMemory(String conversationId) {
		Message lastMessage = this.memory.getLast(conversationId);
		SystemMessage systemMessage = SystemMessage.builder()
			.text(AgentSystemPrompt.PLAN_PROMPT.formatted(this.agentContext.getQuery()))
			.build();

		if (!(lastMessage instanceof UserMessage)) {
			UserMessage userMessage = UserMessage.builder().text(AgentSystemPrompt.NEXT_STEP_PROMPT).build();
			this.memory.add(conversationId, userMessage);
		}

		List<Message> messages = new ArrayList<>();
		messages.add(systemMessage);
		messages.addAll(this.memory.getAll(conversationId));
		return messages;
	}

	/**
	 * Handle model think response
	 * <p>
	 * Output model's reasoning and register expected tool calls
	 * </p>
	 * @param response Model response
	 */
	private void processModelThinkResponse(Flux<ChatResponse> response) {
		List<AssistantMessage.ToolCall> expectedCallsTools = new ArrayList<>();
		StringBuilder outputBuilder = new StringBuilder();

		response.doOnNext(chatResponse -> {
			Generation generation = chatResponse.getResult();
			String content = generation.getOutput().getText();
			this.agentContext.getEmitter().emit(new ThinkingEvent(content));
			outputBuilder.append(content);

			List<AssistantMessage.ToolCall> toolCalls = generation.getOutput().getToolCalls();
			if (!CollectionUtils.isEmpty(toolCalls)) {
				expectedCallsTools.addAll(toolCalls);
			}
		}).doOnComplete(() -> {
			AssistantMessage assistantMessage = new AssistantMessage(outputBuilder.toString(), Map.of(),
					expectedCallsTools);
			this.chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));
		}).blockLast();
	}

}
