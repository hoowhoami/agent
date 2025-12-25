package com.java.spider.controller;

import com.java.spider.core.ScrapeRequest;
import com.java.spider.core.ScrapeResult;
import com.java.spider.core.graph.*;
import com.java.spider.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/spider")
@RestController
public class SpiderController {

    private final GraphBuilder graphBuilder;

    @PostMapping("/scrape")
    public ApiResponse<ScrapeResult> scrape(@RequestBody ScrapeRequest request) {
        try {
            BaseGraph graph = selectGraph(request);
            ScrapeResult result = graph.run();
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Scrape failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    private BaseGraph selectGraph(ScrapeRequest request) {
        GraphConfig config = graphBuilder.config()
                .url(request.getUrl())
                .prompt(request.getPrompt())
                .maxDepth(request.getMaxDepth() != null ? request.getMaxDepth() : 0)
                .maxPages(request.getMaxPages() != null ? request.getMaxPages() : 10)
                .timeout(request.getTimeout() != null ? request.getTimeout() : 30)
                .delayMillis(request.getDelayMillis() != null ? request.getDelayMillis() : 1000)
                .build();

        if (request.getEnableJavaScript() != null && request.getEnableJavaScript()) {
            config.setEnableJavaScript(true);
        }

        String type = request.getType();
        if (type == null) {
            type = inferGraphType(request);
        }

        return switch (type.toLowerCase()) {
            case "search" -> new SearchGraph(config, graphBuilder.getEngine(), graphBuilder.getNodeFactory());
            case "multi" -> new SmartMultiGraph(config, graphBuilder.getEngine(), graphBuilder.getNodeFactory(),
                    List.of(request.getUrl().split(",")));
            default -> new SmartGraph(config, graphBuilder.getEngine(), graphBuilder.getNodeFactory());
        };
    }

    private String inferGraphType(ScrapeRequest request) {
        String url = request.getUrl().toLowerCase();
        if (url.contains("google.com/search") || url.contains("bing.com/search")) {
            return "search";
        }
        if (request.getUrl().contains(",")) {
            return "multi";
        }
        return "smart";
    }
}
