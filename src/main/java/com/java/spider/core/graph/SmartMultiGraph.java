package com.java.spider.core.graph;

import com.java.spider.core.ScrapeResult;
import com.java.spider.core.graph.node.GraphNode;
import com.java.spider.core.graph.node.NodeFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SmartMultiGraph extends BaseGraph {

    private final List<String> urls;

    public SmartMultiGraph(GraphConfig config, GraphEngine engine, NodeFactory nodeFactory, List<String> urls) {
        super(config, engine, nodeFactory);
        this.urls = urls;
    }

    @Override
    public ScrapeResult run() {
        log.info("Running multi graph for {} URLs", urls.size());
        List<ScrapeResult> results = new ArrayList<>();

        for (String url : urls) {
            GraphConfig urlConfig = GraphConfig.builder()
                    .url(url)
                    .prompt(config.getPrompt())
                    .llmProvider(config.getLlmProvider())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature())
                    .maxTokens(config.getMaxTokens())
                    .timeout(config.getTimeout())
                    .enableJavaScript(config.getEnableJavaScript())
                    .headers(config.getHeaders())
                    .maxDepth(config.getMaxDepth())
                    .maxPages(config.getMaxPages())
                    .linkPattern(config.getLinkPattern())
                    .build();

            SmartGraph graph = new SmartGraph(urlConfig, engine, nodeFactory);
            ScrapeResult result = graph.run();
            results.add(result);
        }

        return aggregateResults(results);
    }

    private ScrapeResult aggregateResults(List<ScrapeResult> results) {
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) combined.append("\n\n---\n\n");
            combined.append("URL ").append(i + 1).append(": ").append(urls.get(i));
            combined.append("\n").append(results.get(i).getContent());
        }

        return ScrapeResult.builder()
                .success(true)
                .content(combined.toString())
                .build();
    }
}
