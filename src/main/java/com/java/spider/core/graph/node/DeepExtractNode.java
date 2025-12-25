package com.java.spider.core.graph.node;

import com.java.spider.core.PageContent;
import com.java.spider.core.extractor.ContentExtractor;
import com.java.spider.core.fetcher.PageFetcher;
import com.java.spider.core.graph.GraphConfig;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DeepExtractNode extends BaseNode {

    private final List<PageFetcher> fetchers;
    private final ContentExtractor extractor;
    private final GraphConfig config;

    public DeepExtractNode(List<PageFetcher> fetchers, ContentExtractor extractor, GraphConfig config) {
        this.fetchers = fetchers;
        this.extractor = extractor;
        this.config = config;
    }

    @Override
    public Object execute(Object input) {
        log.info("Executing deep extract node");
        this.input = input;

        try {
            PageContent initialContent = (PageContent) input;
            int maxDepth = config.getMaxDepth() != null ? config.getMaxDepth() : 1;
            int maxPages = config.getMaxPages() != null ? config.getMaxPages() : 10;

            List<Map<String, Object>> results = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            visited.add(config.getUrl());

            // Extract from initial page
            Object initialData = extractor.extractContent(initialContent, config.getPrompt());
            results.add(Map.of("url", config.getUrl(), "depth", 0, "data", initialData));

            if (maxDepth > 0) {
                List<String> links = extractLinks(initialContent);
                crawl(links, results, visited, 1, maxDepth, maxPages);
            }

            this.output = results;
            this.success = true;
            return results;

        } catch (Exception e) {
            log.error("Deep extract failed", e);
            this.success = false;
            this.errorMessage = e.getMessage();
            return null;
        }
    }

    private List<String> extractLinks(PageContent content) {
        Document doc = Jsoup.parse(content.getHtml());
        return doc.select("a[href]").stream()
                .map(e -> e.absUrl("href"))
                .filter(url -> !url.isEmpty() && url.startsWith("http"))
                .filter(url -> config.getLinkPattern() == null || url.matches(config.getLinkPattern()))
                .distinct()
                .collect(Collectors.toList());
    }

    private void crawl(List<String> urls, List<Map<String, Object>> results,
                      Set<String> visited, int depth, int maxDepth, int maxPages) {
        if (depth > maxDepth || results.size() >= maxPages) {
            return;
        }

        for (String url : urls) {
            if (results.size() >= maxPages || visited.contains(url)) {
                continue;
            }

            visited.add(url);
            log.info("Crawling: {} (depth: {})", url, depth);

            try {
                if (config.getDelayMillis() != null && config.getDelayMillis() > 0) {
                    Thread.sleep(config.getDelayMillis());
                }

                GraphConfig pageConfig = GraphConfig.builder()
                        .url(url)
                        .prompt(config.getPrompt())
                        .enableJavaScript(config.getEnableJavaScript())
                        .timeout(config.getTimeout())
                        .build();

                PageFetcher fetcher = fetchers.stream()
                        .filter(f -> f.supports(pageConfig))
                        .findFirst()
                        .orElse(null);

                if (fetcher == null) continue;

                PageContent content = fetcher.fetch(pageConfig);
                if (!content.getSuccess()) continue;

                Object extracted = extractor.extractContent(content, config.getPrompt());
                results.add(Map.of("url", url, "depth", depth, "data", extracted));

            } catch (Exception e) {
                log.warn("Failed to crawl {}: {}", url, e.getMessage());
            }
        }
    }
}
