package com.java.spider.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeResult {

    private String url;
    private String content;
    private String rawHtml;
    private Map<String, Object> structuredData;
    private List<String> links;
    private Map<String, Object> metadata;
    private Boolean success;
    private String errorMessage;
    private Long duration;
    private LocalDateTime extractTime;
}
