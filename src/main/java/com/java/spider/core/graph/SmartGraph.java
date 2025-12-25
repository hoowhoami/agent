package com.java.spider.core.graph;

import com.java.spider.core.graph.node.GraphNode;
import com.java.spider.core.graph.node.NodeFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class SmartGraph extends BaseGraph {

    public SmartGraph(GraphConfig config, GraphEngine engine, NodeFactory nodeFactory) {
        super(config, engine, nodeFactory);
    }

}
