package com.java.spider.core.graph;

import com.java.spider.core.ScrapeResult;
import com.java.spider.core.graph.node.GraphNode;
import com.java.spider.core.graph.node.NodeFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
public abstract class BaseGraph {

    protected final GraphConfig config;
    protected final GraphEngine engine;
    protected final NodeFactory nodeFactory;

    protected BaseGraph(GraphConfig config, GraphEngine engine, NodeFactory nodeFactory) {
        this.config = config;
        this.engine = engine;
        this.nodeFactory = nodeFactory;
    }

    protected GraphNode buildGraph() {
        return buildStandardGraph();
    }

    protected GraphNode buildStandardGraph() {
        GraphNode fetchNode = GraphNode.builder()
                .id(UUID.randomUUID().toString())
                .name("Fetch Page")
                .node(nodeFactory.createFetchNode())
                .input(config)
                .build();

        GraphNode extractNode = GraphNode.builder()
                .id(UUID.randomUUID().toString())
                .name(config.getMaxDepth() != null && config.getMaxDepth() > 0 ? "Deep Extract" : "Extract Content")
                .node(config.getMaxDepth() != null && config.getMaxDepth() > 0
                        ? nodeFactory.createDeepExtractNode(config)
                        : nodeFactory.createExtractNode(config))
                .build();

        fetchNode.addChild(extractNode);
        return fetchNode;
    }

    public ScrapeResult run() {
        log.info("Running graph: {}", this.getClass().getSimpleName());
        GraphNode graph = buildGraph();
        return engine.executeGraph(graph);
    }

    public ScrapeResult run(Map<String, Object> input) {
        log.info("Running graph with custom input: {}", this.getClass().getSimpleName());
        GraphNode graph = buildGraph();
        graph.setInput(input);
        return engine.executeGraph(graph);
    }
}
