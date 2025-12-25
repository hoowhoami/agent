package com.java.spider.core.graph;

import com.java.spider.core.graph.node.NodeFactory;
import com.java.spider.enums.LLMProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class GraphBuilder {

    private final GraphEngine engine;
    private final NodeFactory nodeFactory;

    public SmartGraph smart() {
        return new SmartGraph(GraphConfig.builder().defaults().build(), engine, nodeFactory);
    }

    public SmartGraph smart(String url, String prompt) {
        GraphConfig config = GraphConfig.builder()
                .defaults()
                .url(url)
                .prompt(prompt)
                .build();
        return new SmartGraph(config, engine, nodeFactory);
    }

    public SmartMultiGraph smartMulti(List<String> urls, String prompt) {
        GraphConfig config = GraphConfig.builder()
                .defaults()
                .prompt(prompt)
                .build();
        return new SmartMultiGraph(config, engine, nodeFactory, urls);
    }

    public SearchGraph search(String searchUrl, String prompt) {
        GraphConfig config = GraphConfig.builder()
                .defaults()
                .url(searchUrl)
                .prompt(prompt)
                .build();
        return new SearchGraph(config, engine, nodeFactory);
    }

    public GraphConfigBuilder config() {
        return new GraphConfigBuilder();
    }

    public static class GraphConfigBuilder {
        private final GraphConfig.GraphConfigBuilder builder;

        public GraphConfigBuilder() {
            this.builder = GraphConfig.builder().defaults();
        }

        public GraphConfigBuilder url(String url) {
            builder.url(url);
            return this;
        }

        public GraphConfigBuilder prompt(String prompt) {
            builder.prompt(prompt);
            return this;
        }

        public GraphConfigBuilder llm(LLMProvider provider) {
            builder.llmProvider(provider);
            return this;
        }

        public GraphConfigBuilder model(String modelName) {
            builder.modelName(modelName);
            return this;
        }

        public GraphConfigBuilder temperature(double temperature) {
            builder.temperature(temperature);
            return this;
        }

        public GraphConfigBuilder maxTokens(int maxTokens) {
            builder.maxTokens(maxTokens);
            return this;
        }

        public GraphConfigBuilder timeout(int timeout) {
            builder.timeout(timeout);
            return this;
        }

        public GraphConfigBuilder enableJavaScript() {
            builder.enableJavaScript(true);
            return this;
        }

        public GraphConfigBuilder maxDepth(int maxDepth) {
            builder.maxDepth(maxDepth);
            return this;
        }

        public GraphConfigBuilder maxPages(int maxPages) {
            builder.maxPages(maxPages);
            return this;
        }

        public GraphConfigBuilder cssSelector(String selector) {
            builder.cssSelector(selector);
            return this;
        }

        public GraphConfigBuilder linkPattern(String pattern) {
            builder.linkPattern(pattern);
            return this;
        }

        public GraphConfigBuilder rateLimit(int requestsPerSecond) {
            builder.rateLimit(requestsPerSecond);
            return this;
        }

        public GraphConfigBuilder disableCache() {
            builder.enableCache(false);
            return this;
        }

        public GraphConfigBuilder delayMillis(int delayMillis) {
            builder.delayMillis(delayMillis);
            return this;
        }

        public GraphConfig build() {
            return builder.build();
        }
    }
}
