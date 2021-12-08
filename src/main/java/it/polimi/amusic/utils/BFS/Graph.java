package it.polimi.amusic.utils.BFS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    //Mappa contenente per ogni vertice la lista degli archi
    public Map<String, List<WeightedEdge>> adjMap = new HashMap<>();

    public Graph(List<WeightedEdge> weightedEdges) {

        weightedEdges.forEach(weightedEdge -> {
            //Per ogni arco aggiungo i vertici se sono assenti nella mappa
            adjMap.putIfAbsent(weightedEdge.src, new ArrayList<>());
            adjMap.putIfAbsent(weightedEdge.dest, new ArrayList<>());

            //Aggiungo gli archi in ambe le direzioni
            adjMap.get(weightedEdge.src)
                    .add(new WeightedEdge(weightedEdge.src, weightedEdge.dest, weightedEdge.weight));
            adjMap.get(weightedEdge.dest)
                    .add(new WeightedEdge(weightedEdge.dest, weightedEdge.src, weightedEdge.weight));
        });
    }
}
