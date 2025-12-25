package com.java.spider.core.fetcher;

import com.java.spider.core.PageContent;
import com.java.spider.core.graph.GraphConfig;
import lombok.extern.slf4j.Slf4j;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JavaScriptPageFetcher implements PageFetcher {

    @Override
    public boolean supports(GraphConfig config) {
        return config.getEnableJavaScript() != null && config.getEnableJavaScript();
    }

    @Override
    public PageContent fetch(GraphConfig config) {
        try (WebClient webClient = createWebClient(config)) {
            HtmlPage page = webClient.getPage(config.getUrl());
            webClient.waitForBackgroundJavaScript(10000);

            return PageContent.builder()
                    .url(config.getUrl())
                    .html(page.asXml())
                    .text(page.asNormalizedText())
                    .title(page.getTitleText())
                    .statusCode(page.getWebResponse().getStatusCode())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch with JS from {}", config.getUrl(), e);
            return PageContent.builder()
                    .url(config.getUrl())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private WebClient createWebClient(GraphConfig config) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setTimeout(config.getTimeout() != null ? config.getTimeout() * 1000 : 30000);

        if (config.getHeaders() != null) {
            config.getHeaders().forEach(webClient::addRequestHeader);
        }

        return webClient;
    }
}
