package com.java.spider.core.graph.node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelayNode extends BaseNode {

    private final int delayMillis;

    public DelayNode(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    @Override
    public Object execute(Object input) {
        this.input = input;
        try {
            log.info("Delaying for {} ms", delayMillis);
            Thread.sleep(delayMillis);
            this.output = input;
            this.success = true;
            return input;
        } catch (InterruptedException e) {
            log.error("Delay interrupted", e);
            Thread.currentThread().interrupt();
            this.success = false;
            this.errorMessage = e.getMessage();
            return input;
        }
    }
}
