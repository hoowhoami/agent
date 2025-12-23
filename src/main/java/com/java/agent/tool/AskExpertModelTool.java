package com.java.agent.tool;

import com.java.agent.core.AgentExecutor;
import com.java.agent.core.AgentRequest;
import com.java.agent.core.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * Tool for consulting expert models
 */
@Component
@RequiredArgsConstructor
public class AskExpertModelTool extends AbstractAgentTool {
    private final AgentExecutor agentExecutor;

    @Tool(description = "Consult an expert model for specialized tasks")
    public String askExpert(String model, String question) {
        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setPrompt(question);
        agentRequest.setModel(model);

        AgentResponse response = agentExecutor.chat(agentRequest);
        return response.getContent();
    }
}
