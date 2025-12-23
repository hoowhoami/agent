package com.java.agent.agent;

import lombok.Data;
import java.util.List;

/**
 * Agent configuration
 */
@Data
public class AgentConfig {
    private String agentType;
    private String whenToUse;
    private List<String> tools;
    private String systemPrompt;
    private String location;
    private String modelName;
}
