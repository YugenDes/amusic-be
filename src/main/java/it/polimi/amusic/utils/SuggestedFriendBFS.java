package it.polimi.amusic.utils;

import it.polimi.amusic.utils.BFS.Graph;
import it.polimi.amusic.utils.BFS.Vertex;
import it.polimi.amusic.utils.BFS.WeightedEdge;

import java.util.*;

/**
 * La complessita temporale dell'esecuzione é O(V + E),
 * dove V è l'insieme dei vertici del grafo ed E è l'insieme degli archi che collegano i vertici.
 */
public class SuggestedFriendBFS {

    public static Vertex findMaxCost(Graph graph, String src) {
        //Coda per il BFS
        Queue<Vertex> queue = new ArrayDeque<>();
        //Creo il set dei vertici
        Set<String> vertices = new HashSet<>();
        //Inserendo quello di partenza
        vertices.add(src);

        final Vertex start = new Vertex(src, 1, vertices);
        //accodo il primo vertice
        queue.add(start);

        // salvo il costo massimo e il relativo vertice
        double maxCost = 0;
        Vertex maxCostVertex = start;

        //ciclo fin quando la coda non é vuota
        while (!queue.isEmpty()) {
            // estraggo il vertice
            Vertex vertex = queue.poll();

            String v = vertex.vertex;
            double cost = vertex.weightPath;
            vertices = new HashSet<>(vertex.s);


            //Se il costo del percorso é maggiore a quello massimo salvato con la prima condizione
            //Salto il primo vertice con la seconda condizione
            //Valuto i vertici solo a profonditá > 2 poiche quelli a profondita 2 sono i stessi amici del primo vertice
            //Aggiorno il costo
            if (cost > maxCost && !vertex.vertex.equals(src) && vertex.s.size() > 2) {
                maxCost = Math.max(maxCost, cost);
                maxCostVertex = vertex;
            }


            // per ogni vertice adiacente
            for (WeightedEdge weightedEdge : graph.adjMap.get(v)) {
                //controllo se é presente un ciclo
                if (!vertices.contains(weightedEdge.dest)) {
                    //aggiungo il vertice al 'path'
                    Set<String> s = new HashSet<>(vertices);
                    s.add(weightedEdge.dest);

                    //Se il costo del prossimo vertice é maggiore del massimo costo trovato
                    //Aggiungo alla coda il vertice
                    //Cosi facendo poto l'albero di ricerca
                    if (cost * weightedEdge.weight > maxCost)
                        //Il costo viene aggiornato tramite la moltiplicazione
                        //Perche si riferisce alla probabilitá dipendente delle partecipazione congiunte di piu amici
                        queue.add(new Vertex(weightedEdge.dest, cost * weightedEdge.weight, s));
                }
            }
        }
        return maxCostVertex;
    }

    public void test() {
        List<WeightedEdge> weightedEdges = Arrays.asList(
                new WeightedEdge("Andrea", "Cristina", 0.5d),
                new WeightedEdge("Andrea", "Gabriel", 0.3d),
                new WeightedEdge("Cristina", "Marco", 0.2d),
                new WeightedEdge("Cristina", "Francesco", 1),
                new WeightedEdge("Cristina", "Dario", 0.6),
                new WeightedEdge("Gabriel", "Ignazio", 1),
                new WeightedEdge("Gabriel", "Dario", 0.33),
                new WeightedEdge("Gabriel", "Francesco", 0.67)
        );

        Graph graph = new Graph(weightedEdges);

        String src = "Andrea";


        final Vertex maxCost = findMaxCost(graph, src);
        System.out.println(maxCost.vertex);
        maxCost.s.forEach(System.out::println);
    }


}

