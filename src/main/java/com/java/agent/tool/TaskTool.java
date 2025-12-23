package com.java.agent.tool;

import com.java.agent.agent.AgentConfig;
import com.java.agent.agent.AgentLoader;
import com.java.agent.core.AgentExecutor;
import com.java.agent.core.AgentRequest;
import com.java.agent.core.AgentResponse;
import com.java.agent.core.AgentTask;
import com.java.agent.model.ModelManager;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Task tool for delegating to sub-agents
 * Enhanced with resume, background execution, and advanced features
 */
@Component
@RequiredArgsConstructor
public class TaskTool extends AbstractAgentTool {
    private final AgentLoader agentLoader;
    private final ModelManager modelManager;
    private final AgentExecutor agentExecutor;

    @Tool(description = """
            Launch a new agent to handle complex, multi-step tasks autonomously.

            Available agent types:
            - general-purpose: General-purpose agent for researching complex questions
            - Explore: Fast agent specialized for exploring codebases
            - Plan: Software architect agent for designing implementation plans

            Parameters:
            - subagentType: The type of agent to launch (required)
            - prompt: The task for the agent to perform (required)
            - description: A short (3-5 word) description of the task (required)
            - model: Optional model to use (sonnet, opus, haiku). Defaults to agent's preferred model
            - resume: Optional agent ID to resume from previous execution
            - runInBackground: Set to true to run this agent in the background
            """)
    public String executeTask(
            String subagentType,
            String prompt,
            String description,
            String model,
            String resume,
            Boolean runInBackground) {

        // Get agent configuration
        AgentConfig agent = agentLoader.getAgent(subagentType);
        if (agent == null) {
            return "Error: Agent type '" + subagentType + "' not found. Available types: general-purpose, Explore, Plan";
        }

        // Resolve model
        String resolvedModel = resolveModel(agent, model);

        // Build agent request
        AgentRequest.AgentRequestBuilder requestBuilder = AgentRequest.builder()
                .prompt(buildPrompt(agent, prompt))
                .model(resolvedModel)
                .description(description);

        // Handle resume
        if (resume != null && !resume.isEmpty()) {
            requestBuilder.resumeTaskId(resume)
                    .action("resume");
        }

        // Handle background execution
        if (Boolean.TRUE.equals(runInBackground)) {
            requestBuilder.runInBackground(true);
            String taskId = agentExecutor.startBackgroundTask(requestBuilder.build());
            return "Task started in background. Task ID: " + taskId +
                   "\nUse TaskOutput tool with task_id='" + taskId + "' to retrieve results.";
        }

        // Execute synchronously
        AgentRequest agentRequest = requestBuilder.build();
        AgentResponse response = agentExecutor.chat(agentRequest);

        return formatResponse(response, description);
    }

    @Tool(description = """
            Retrieve output from a running or completed task (background shell, agent, or remote session).

            Parameters:
            - taskId: The task ID to get output from (required)
            - block: Whether to wait for completion (default: true)
            - timeout: Max wait time in milliseconds (default: 30000)
            """)
    public String getTaskOutput(String taskId, Boolean block, Long timeout) {
        AgentTask task = agentExecutor.getBackgroundTask(taskId);

        if (task == null) {
            return "Error: Task not found: " + taskId;
        }

        boolean shouldBlock = block == null || block;
        long maxWait = timeout != null ? timeout : 30000;
        long startTime = System.currentTimeMillis();

        // Wait for completion if blocking
        if (shouldBlock) {
            while (task.isRunning() && (System.currentTimeMillis() - startTime) < maxWait) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Build status response
        StringBuilder response = new StringBuilder();
        response.append("Task ID: ").append(taskId).append("\n");
        response.append("Status: ").append(task.getStatus()).append("\n");
        response.append("Duration: ").append(task.getDuration()).append("ms\n");

        if (task.isCompleted()) {
            response.append("\n--- Result ---\n");
            response.append(task.getResult());
        } else if (task.isFailed()) {
            response.append("\n--- Error ---\n");
            response.append(task.getError());
        } else if (task.isRunning()) {
            response.append("\nTask is still running...");
        }

        return response.toString();
    }

    private String resolveModel(AgentConfig agent, String modelParam) {
        // Priority: explicit param > agent config > task model
        if (modelParam != null && !modelParam.isEmpty()) {
            return modelManager.resolveModelProfile(modelParam) != null
                    ? modelManager.resolveModelProfile(modelParam).getModelName()
                    : modelParam;
        }

        if (agent.getModelName() != null) {
            return agent.getModelName();
        }

        return modelManager.getTaskModel();
    }

    private String buildPrompt(AgentConfig agent, String userPrompt) {
        StringBuilder fullPrompt = new StringBuilder();

        // Add agent's system prompt
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isEmpty()) {
            fullPrompt.append(agent.getSystemPrompt()).append("\n\n");
        }

        // Add context about available tools
        if (agent.getTools() != null && !agent.getTools().isEmpty()) {
            fullPrompt.append("Available tools: ").append(String.join(", ", agent.getTools())).append("\n\n");
        }

        // Add user prompt
        fullPrompt.append(userPrompt);

        return fullPrompt.toString();
    }

    private String formatResponse(AgentResponse response, String description) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("=== Task: ").append(description).append(" ===\n\n");
        formatted.append(response.getContent());
        formatted.append("\n\n=== Task completed ===");
        return formatted.toString();
    }
}

