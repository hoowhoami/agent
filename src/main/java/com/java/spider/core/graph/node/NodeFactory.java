package com.java.spider.core.graph.node;

import com.java.spider.core.extractor.ContentExtractor;
import com.java.spider.core.fetcher.PageFetcher;
import com.java.spider.core.graph.GraphConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NodeFactory {

    private final List<PageFetcher> fetchers;
    private final ContentExtractor extractor;

    public FetchNode createFetchNode() {
        return new FetchNode(fetchers);
    }

    public ExtractNode createExtractNode(GraphConfig config) {
        return new ExtractNode(extractor, config);
    }

    public DeepExtractNode createDeepExtractNode(GraphConfig config) {
        return new DeepExtractNode(fetchers, extractor, config);
    }
}
