package com.java.agent.core;

import lombok.Data;

/**
 * Agent response
 *
 * @author whoami
 */
@Data
public class AgentResponse {

    private String conversationId;
    private String status;
    private String message;

    public static AgentResponse ok(String conversationId, String message) {
        AgentResponse response = new AgentResponse();
        response.conversationId = conversationId;
        response.status = "success";
        response.message = message;
        return response;
    }

}
