package com.java.spider.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeRequest {

    private String type;
    private String url;
    private String prompt;

    @Builder.Default
    private Integer maxDepth = 0;

    @Builder.Default
    private Integer maxPages = 10;

    @Builder.Default
    private Integer timeout = 30;

    @Builder.Default
    private Integer delayMillis = 1000;

    private Boolean enableJavaScript;
}
