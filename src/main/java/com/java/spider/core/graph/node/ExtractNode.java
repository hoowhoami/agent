package com.java.spider.core.graph.node;

import com.java.spider.core.PageContent;
import com.java.spider.core.extractor.ContentExtractor;
import com.java.spider.core.graph.GraphConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtractNode extends BaseNode {

    private final ContentExtractor extractor;
    private final GraphConfig config;

    public ExtractNode(ContentExtractor extractor, GraphConfig config) {
        this.extractor = extractor;
        this.config = config;
    }

    public ExtractNode(ContentExtractor extractor) {
        this.extractor = extractor;
        this.config = null;
    }

    @Override
    public Object execute(Object input) {
        log.info("Executing extract node");
        this.input = input;

        try {
            PageContent content = (PageContent) input;
            String prompt = config != null ? config.getPrompt() : "Extract main content";
            String extracted = extractor.extractContent(content, prompt);

            this.output = extracted;
            this.success = true;
            return extracted;

        } catch (Exception e) {
            log.error("Extract failed", e);
            this.success = false;
            this.errorMessage = e.getMessage();
            return null;
        }
    }
}
