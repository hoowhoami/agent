package com.java.spider.core.graph.node;

import com.java.spider.core.PageContent;
import com.java.spider.core.fetcher.PageFetcher;
import com.java.spider.core.graph.GraphConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FetchNode extends BaseNode {

    private final List<PageFetcher> fetchers;

    public FetchNode(List<PageFetcher> fetchers) {
        this.fetchers = fetchers;
    }

    @Override
    public Object execute(Object input) {
        log.info("Executing fetch node");
        this.input = input;

        try {
            GraphConfig config = (GraphConfig) input;
            PageFetcher fetcher = fetchers.stream()
                    .filter(f -> f.supports(config))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No fetcher available"));

            PageContent content = fetcher.fetch(config);
            this.output = content;
            this.success = content.getSuccess();
            return content;

        } catch (Exception e) {
            log.error("Fetch failed", e);
            this.success = false;
            this.errorMessage = e.getMessage();
            return null;
        }
    }
}
