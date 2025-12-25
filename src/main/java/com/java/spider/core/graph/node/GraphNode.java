package com.java.spider.core.graph.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphNode {

    private String id;
    private String name;
    private BaseNode node;
    private Object input;
    private Object output;

    @Builder.Default
    private List<GraphNode> children = new ArrayList<>();

    private java.util.Map<String, Object> metadata;
    private Boolean success;
    private String errorMessage;

    public Object execute(Object input) {
        if (node != null) {
            Object result = node.execute(input);
            this.output = result;
            this.success = node.getSuccess();
            this.errorMessage = node.getErrorMessage();
            return result;
        }
        return null;
    }

    public GraphNode addChild(GraphNode child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        return this;
    }
}
