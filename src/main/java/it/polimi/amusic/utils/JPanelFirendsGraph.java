package it.polimi.amusic.utils;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JPanelFirendsGraph extends JFrame {

    public JPanelFirendsGraph(Map<String, List<String>> friends) {

        super("Friend Graph!");

        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        Map<String, Object> vertex = new HashMap<>();
        graph.getModel().beginUpdate();
        try {
            friends.forEach((key, value) -> {
                vertex.putIfAbsent(key, graph.insertVertex(parent, key, key, 20, 20, 80, 30));
                value.forEach(s -> {
                    vertex.putIfAbsent(key, graph.insertVertex(parent, s, s, 20, 20, 80, 30));
                    graph.insertEdge(parent, null, "Edge", vertex.get(key), vertex.get(s));
                });
            });
        } finally {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        getContentPane().add(graphComponent);
    }

    public static void printGraph(Map<String, List<String>> friends) {
        JPanelFirendsGraph frame = new JPanelFirendsGraph(friends);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

}
