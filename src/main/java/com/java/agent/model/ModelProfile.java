package com.java.agent.model;

import lombok.Data;

/**
 * Model profile configuration
 */
@Data
public class ModelProfile {
    private String name;
    private String modelName;
    private String provider;
    private String apiKey;
    private String baseUrl;
    private int contextLength;
    private boolean isActive;
    private long createdAt;
    private Long lastUsed;
}
