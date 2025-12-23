package com.java.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent request with conversation context
 * Enhanced to support Claude Code Agent features
 * @author whoami
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AgentRequest {

    /**
     * Conversation ID for multi-turn dialogue
     * Will be auto-generated if not provided
     */
    private String conversationId;

    /**
     * User prompt/question
     */
    private String prompt;

    /**
     * User action: null (new request), "confirm", "auto_confirm_session", "modify", "resume"
     */
    private String action;

    /**
     * Model to use: model name or pointer (main, task, reasoning, quick)
     */
    private String model;

    /**
     * Task ID to resume from (for TaskTool resume feature)
     */
    private String resumeTaskId;

    /**
     * Whether to run in background
     */
    @Builder.Default
    private boolean runInBackground = false;

    /**
     * Timeout in milliseconds
     */
    private Long timeout;

    /**
     * Whether to stream response
     */
    @Builder.Default
    private boolean stream = false;

    /**
     * Maximum number of tool calls
     */
    private Integer maxToolCalls;

    /**
     * Additional context or metadata
     */
    private String context;

    /**
     * Task description (for TaskTool)
     */
    private String description;
}
