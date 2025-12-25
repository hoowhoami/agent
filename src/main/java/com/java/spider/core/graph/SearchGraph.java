package com.java.spider.core.graph;

import com.java.spider.core.ScrapeResult;
import com.java.spider.core.graph.node.GraphNode;
import com.java.spider.core.graph.node.NodeFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SearchGraph extends BaseGraph {

    public SearchGraph(GraphConfig config, GraphEngine engine, NodeFactory nodeFactory) {
        super(config, engine, nodeFactory);
    }

    @Override
    protected GraphNode buildGraph() {
        GraphNode fetchNode = GraphNode.builder()
                .id(UUID.randomUUID().toString())
                .name("Fetch Search Results")
                .node(nodeFactory.createFetchNode())
                .input(config)
                .build();

        GraphNode extractLinksNode = GraphNode.builder()
                .id(UUID.randomUUID().toString())
                .name("Extract Search Result Links")
                .node(nodeFactory.createExtractNode(config))
                .build();

        fetchNode.addChild(extractLinksNode);
        return fetchNode;
    }

    @Override
    public ScrapeResult run() {
        log.info("Running search graph");

        // 1. 获取搜索结果页面的链接
        GraphNode graph = buildGraph();
        ScrapeResult searchResult = engine.executeGraph(graph);

        if (!searchResult.getSuccess() || searchResult.getLinks() == null || searchResult.getLinks().isEmpty()) {
            return searchResult;
        }

        // 2. 对每个搜索结果链接进行爬取
        List<ScrapeResult> results = new ArrayList<>();
        int maxResults = config.getMaxPages() != null ? config.getMaxPages() : 10;

        for (int i = 0; i < Math.min(searchResult.getLinks().size(), maxResults); i++) {
            String url = searchResult.getLinks().get(i);
            log.info("Crawling search result {}: {}", i + 1, url);

            GraphConfig resultConfig = GraphConfig.builder()
                    .url(url)
                    .prompt(config.getPrompt())
                    .llmProvider(config.getLlmProvider())
                    .modelName(config.getModelName())
                    .temperature(config.getTemperature())
                    .maxTokens(config.getMaxTokens())
                    .timeout(config.getTimeout())
                    .enableJavaScript(config.getEnableJavaScript())
                    .build();

            SmartGraph resultGraph = new SmartGraph(resultConfig, engine, nodeFactory);
            ScrapeResult result = resultGraph.run();
            results.add(result);
        }

        return aggregateResults(results, searchResult.getLinks());
    }

    private ScrapeResult aggregateResults(List<ScrapeResult> results, List<String> urls) {
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) combined.append("\n\n---\n\n");
            combined.append("Result ").append(i + 1).append(": ").append(urls.get(i));
            combined.append("\n").append(results.get(i).getContent());
        }

        return ScrapeResult.builder()
                .success(true)
                .content(combined.toString())
                .build();
    }
}
