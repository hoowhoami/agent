package com.java.agent.core;

import com.java.agent.config.SystemPromptConfig;
import com.java.agent.model.ModelManager;
import com.java.agent.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Agent executor with automatic tool execution and multi-model support
 * Enhanced with streaming, background tasks, and advanced context management
 *
 * @author whoami
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AgentExecutor {

    private final ChatClient.Builder builder;
    private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;
    private final List<AgentTool> tools;
    private final ModelManager modelManager;

    // Task management for background agents
    private final ConcurrentHashMap<String, AgentTask> backgroundTasks = new ConcurrentHashMap<>();

    /**
     * Handle chat with automatic tool execution (blocking mode)
     */
    public AgentResponse chat(AgentRequest request) {
        String conversationId = getOrGenerateConversationId(request);
        List<ToolCallback> toolCallbacks = getToolCallbacks();
        String model = resolveModel(request.getModel());

        log.info("Processing chat request for conversation: {} with model: {}", conversationId, model);

        ChatClient.Builder chatBuilder = builder;
        if (model != null) {
            chatBuilder = builder.clone();
        }

        String response = chatBuilder.build()
                .prompt()
                .system(SystemPromptConfig.SYSTEM_PROMPT)
                .user(request.getPrompt())
                .toolCallbacks(toolCallbacks)
                .advisors(messageChatMemoryAdvisor)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return AgentResponse.ok(conversationId, response);
    }

    /**
     * Handle chat with streaming response
     */
    public Flux<String> chatStream(AgentRequest request) {
        String conversationId = getOrGenerateConversationId(request);
        List<ToolCallback> toolCallbacks = getToolCallbacks();
        String model = resolveModel(request.getModel());

        log.info("Processing streaming chat request for conversation: {} with model: {}", conversationId, model);

        ChatClient.Builder chatBuilder = builder;
        if (model != null) {
            chatBuilder = builder.clone();
        }

        return chatBuilder.build()
                .prompt()
                .system(SystemPromptConfig.SYSTEM_PROMPT)
                .user(request.getPrompt())
                .toolCallbacks(toolCallbacks)
                .advisors(messageChatMemoryAdvisor)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    /**
     * Start a background agent task
     */
    public String startBackgroundTask(AgentRequest request) {
        String taskId = UUID.randomUUID().toString();
        AgentTask task = new AgentTask(taskId, request);
        backgroundTasks.put(taskId, task);

        // Execute in background thread
        new Thread(() -> {
            try {
                task.setStatus(AgentTask.Status.RUNNING);
                AgentResponse response = chat(request);
                task.setResult(response.getContent());
                task.setStatus(AgentTask.Status.COMPLETED);
            } catch (Exception e) {
                log.error("Background task failed: {}", taskId, e);
                task.setError(e.getMessage());
                task.setStatus(AgentTask.Status.FAILED);
            }
        }).start();

        return taskId;
    }

    /**
     * Get background task result
     */
    public AgentTask getBackgroundTask(String taskId) {
        return backgroundTasks.get(taskId);
    }

    /**
     * Resume a previous conversation with new input
     */
    public AgentResponse resume(String conversationId, String newPrompt) {
        AgentRequest request = AgentRequest.builder()
                .conversationId(conversationId)
                .prompt(newPrompt)
                .action("resume")
                .build();
        return chat(request);
    }

    private String getOrGenerateConversationId(AgentRequest request) {
        String conversationId = request.getConversationId();
        if (StringUtils.isBlank(conversationId)) {
            conversationId = UUID.randomUUID().toString();
            log.info("Generated new conversation ID: {}", conversationId);
        }
        return conversationId;
    }

    private List<ToolCallback> getToolCallbacks() {
        return tools.stream()
                .flatMap(tool -> tool.getToolCallbacks().stream())
                .toList();
    }

    private String resolveModel(String modelParam) {
        if (StringUtils.isBlank(modelParam)) {
            return modelManager.getMainModel();
        }
        return modelManager.resolveModelProfile(modelParam) != null
            ? modelManager.resolveModelProfile(modelParam).getModelName()
            : modelParam;
    }
}
