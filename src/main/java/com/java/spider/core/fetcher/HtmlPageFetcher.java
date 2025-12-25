package com.java.spider.core.fetcher;

import com.java.spider.core.PageContent;
import com.java.spider.core.graph.GraphConfig;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class HtmlPageFetcher implements PageFetcher {

    private final WebClient webClient;

    public HtmlPageFetcher() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Override
    public boolean supports(GraphConfig config) {
        return config.getEnableJavaScript() == null || !config.getEnableJavaScript();
    }

    @Override
    public PageContent fetch(GraphConfig config) {
        try {
            String html = fetchHtmlContent(config);
            return parseHtml(config.getUrl(), html);
        } catch (Exception e) {
            log.error("Failed to fetch HTML from {}", config.getUrl(), e);
            return PageContent.builder()
                    .url(config.getUrl())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private String fetchHtmlContent(GraphConfig config) {
        WebClient.RequestHeadersSpec<?> spec = webClient.get()
                .uri(config.getUrl())
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        if (config.getHeaders() != null) {
            config.getHeaders().forEach(spec::header);
        }

        return spec.retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(config.getTimeout() != null ? config.getTimeout() * 1000L : 30000L))
                .onErrorResume(e -> {
                    log.error("Error fetching URL: {}", config.getUrl(), e);
                    return Mono.just("");
                })
                .block();
    }

    private PageContent parseHtml(String url, String html) {
        Document document = Jsoup.parse(html, url);
        return PageContent.builder()
                .url(url)
                .html(html)
                .text(document.text())
                .title(document.title())
                .statusCode(200)
                .success(true)
                .build();
    }
}
