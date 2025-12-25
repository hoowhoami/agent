package com.java.spider.core.graph;

import com.java.spider.enums.LLMProvider;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for graph execution
 */
@Data
@Builder
public class GraphConfig {

    private String url;
    private String prompt;
    private LLMProvider llmProvider;
    private String modelName;
    private Double temperature;
    private Integer maxTokens;
    private Integer timeout;
    private Boolean enableJavaScript;
    private Map<String, String> headers;
    private Integer maxDepth;
    private Integer maxPages;
    private String cssSelector;
    private String linkPattern;
    private Integer rateLimit;
    private Integer delayMillis;
    private Boolean enableCache;
    private Map<String, Object> metadata;

    public static class GraphConfigBuilder {
        public GraphConfigBuilder defaults() {
            this.temperature = 0.7;
            this.maxTokens = 2000;
            this.maxDepth = 10;
            this.maxPages = 100;
            this.timeout = 30;
            this.enableJavaScript = false;
            this.enableCache = true;
            return this;
        }
    }
}
