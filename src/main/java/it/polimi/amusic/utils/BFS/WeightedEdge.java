package it.polimi.amusic.utils.BFS;

public class WeightedEdge {

    //Vertice di partenza
    public final String src;
    //Vertice di destinazione
    public final String dest;
    //Peso dell'arco
    public final double weight;

    public WeightedEdge(String src, String dest, double weight) {
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }
}
