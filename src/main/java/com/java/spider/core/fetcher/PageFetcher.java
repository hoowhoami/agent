package com.java.spider.core.fetcher;

import com.java.spider.core.PageContent;
import com.java.spider.core.graph.GraphConfig;

/**
 * Page fetcher interface
 */
public interface PageFetcher {

    PageContent fetch(GraphConfig config);

    boolean supports(GraphConfig config);
}
