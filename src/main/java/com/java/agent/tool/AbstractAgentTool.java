package com.java.agent.tool;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for agent tools
 */
public abstract class AbstractAgentTool implements AgentTool {

    @Override
    public List<ToolCallback> getToolCallbacks() {
        return Arrays.stream(ToolCallbacks.from(this)).toList();
    }

}
