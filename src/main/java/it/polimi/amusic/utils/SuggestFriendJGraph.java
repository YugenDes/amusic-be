package it.polimi.amusic.utils;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.KatzCentrality;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

public class SuggestFriendJGraph {

    public void run() {
        String andrea = "Andrea";
        String cristina = "Cristina";
        String gabriel = "Gabriel";
        String marco = "Marco";
        String dario = "Dario";
        String francesco = "Francesco";
        String ignazio = "Ignazio";
        Map<String,Double> presenze = new HashMap();
        List<String> nomi = Arrays.asList(andrea, cristina, gabriel, marco, dario, francesco, ignazio);
        List<String> amici = Arrays.asList(andrea, gabriel, cristina, francesco);
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        graph.addVertex(cristina);
        graph.addVertex(gabriel);
        graph.addVertex(marco);
        graph.addVertex(francesco);
        graph.addVertex(dario);
        graph.addVertex(ignazio);
        presenze.put(cristina,5/10d);
        presenze.put(gabriel,3/10d);
        presenze.put(marco,4/10d);
        presenze.put(francesco,8/10d);
        presenze.put(dario,2/10d);
        presenze.put(ignazio,1/10d);
        graph.addEdge(cristina, marco);
        graph.addEdge(cristina, gabriel);
        graph.addEdge(marco, gabriel);
        graph.addEdge(marco, dario);
        graph.addEdge(francesco, ignazio);
        graph.addEdge(francesco, gabriel);

        final KatzCentrality katzCentrality = new KatzCentrality(graph,0.85,2,0.1);
        Map<String, Double> scores = katzCentrality.getScores();
        scores.entrySet().forEach(System.out::println);

    }

    public static void main(String[] args) {
        new SuggestFriendJGraph().run();
    }


}
