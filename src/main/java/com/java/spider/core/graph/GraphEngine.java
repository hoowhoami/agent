package com.java.spider.core.graph;

import com.java.spider.core.ScrapeResult;
import com.java.spider.core.graph.node.GraphNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class GraphEngine {

    public ScrapeResult executeGraph(GraphNode rootNode) {
        long startTime = System.currentTimeMillis();

        try {
            executeNode(rootNode);

            return ScrapeResult.builder()
                    .success(rootNode.getSuccess())
                    .content(extractContent(rootNode))
                    .structuredData(extractStructuredData(rootNode))
                    .links(extractLinks(rootNode))
                    .duration(System.currentTimeMillis() - startTime)
                    .extractTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Graph execution failed", e);
            return ScrapeResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .extractTime(LocalDateTime.now())
                    .build();
        }
    }

    private void executeNode(GraphNode node) {
        Object result = node.execute(node.getInput());
        if (node.getChildren() != null) {
            node.getChildren().forEach(child -> {
                child.setInput(result);
                executeNode(child);
            });
        }
    }

    private String extractContent(GraphNode node) {
        if (node.getOutput() instanceof String) {
            return (String) node.getOutput();
        }
        if (node.getChildren() != null) {
            for (GraphNode child : node.getChildren()) {
                String content = extractContent(child);
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractStructuredData(GraphNode node) {
        if (node.getOutput() instanceof Map) {
            return (Map<String, Object>) node.getOutput();
        }
        if (node.getChildren() != null) {
            for (GraphNode child : node.getChildren()) {
                var data = extractStructuredData(child);
                if (data != null && !data.isEmpty()) {
                    return data;
                }
            }
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractLinks(GraphNode node) {
        if (node.getOutput() instanceof List) {
            return (List<String>) node.getOutput();
        }
        if (node.getChildren() != null) {
            for (GraphNode child : node.getChildren()) {
                List<String> links = extractLinks(child);
                if (links != null && !links.isEmpty()) {
                    return links;
                }
            }
        }
        return new ArrayList<>();
    }
}
