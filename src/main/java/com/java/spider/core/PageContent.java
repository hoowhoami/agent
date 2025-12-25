package com.java.spider.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageContent {

    private String url;
    private String html;
    private String text;
    private String title;
    private Integer statusCode;
    private Map<String, String> headers;
    private String contentType;
    private Boolean success;
    private String errorMessage;
}
