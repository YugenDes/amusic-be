package it.polimi.amusic.utils.BFS;

import java.util.Set;

public class Vertex {
    //L'etichetta del vertice
    public String vertex;

    //il costo totale del path fino ad esso calcolato
    public double weightPath;

    //Set di vertici visitati 'path'
    public Set<String> s;

    public Vertex(String vertex, double weightPath, Set<String> s) {
        this.vertex = vertex;
        this.weightPath = weightPath;
        this.s = s;
    }
}
