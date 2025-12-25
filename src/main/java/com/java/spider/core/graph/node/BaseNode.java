package com.java.spider.core.graph.node;

import lombok.Data;

@Data
public abstract class BaseNode {

    protected String id;
    protected String name;
    protected Object input;
    protected Object output;
    protected Boolean success;
    protected String errorMessage;

    public abstract Object execute(Object input);

}
