package it.polimi.amusic.utils;

import java.util.*;

public class SuggestFriendsDFS<T> {

    private HashMap<T, ArrayList<T>> graph = new HashMap<>(); //graph
    private List<Set<T>> groups = new ArrayList<>();

    //Time O(1), Space O(1)
    public void addFriendship(T start, T end) {
        graph.putIfAbsent(start, new ArrayList<T>());
        graph.get(start).add(end);
        graph.putIfAbsent(end, new ArrayList<T>());
        graph.get(end).add(start);
    }

    //DFS wrapper, Time O(V+E), Space O(V)
    //V is total number of people, E is number of connections
    private void findGroups(int depth) {
        Map<T, Boolean> visited = new HashMap<>();
        for (T t : graph.keySet())
            visited.put(t, false);
        for (T t : graph.keySet()) {
            if (!visited.get(t)) {
                Set<T> group = new HashSet<>();
                dfs(t, visited, group);
                groups.add(group);
            }
        }
    }

    //DFS + memoization, Time O(V+E), Space O(V)
    private void dfs(T v, Map<T, Boolean> visited, Set<T> group) {
        visited.put(v, true);
        group.add(v);
        for (T x : graph.get(v)) {
            if (!visited.get(x))
                dfs(x, visited, group);
        }
    }

    //Time O(V+E), Space O(V)
    public Set<T> getSuggestedFriends(T a, int depth) {
        if (groups.isEmpty())
            findGroups(depth);
        Set<T> res = new HashSet<>();
        for (Set<T> t : groups) {
            if (t.contains(a)) {
                res = t;
                break;
            }
        }
        if (res.size() > 0)
            res.remove(a);
        return res;
    }
}
