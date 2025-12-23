package com.java.agent.core;

import lombok.Data;

/**
 * Represents a background agent task
 *
 * @author whoami
 */
@Data
public class AgentTask {

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    private final String taskId;
    private final AgentRequest request;
    private Status status;
    private String result;
    private String error;
    private long startTime;
    private long endTime;

    public AgentTask(String taskId, AgentRequest request) {
        this.taskId = taskId;
        this.request = request;
        this.status = Status.PENDING;
        this.startTime = System.currentTimeMillis();
    }

    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.COMPLETED || status == Status.FAILED) {
            this.endTime = System.currentTimeMillis();
        }
    }

    public long getDuration() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    public boolean isFailed() {
        return status == Status.FAILED;
    }
}
