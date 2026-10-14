package mrt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Dijkstra with a binary min-heap: O((V + E) log V).
 * Paths are rebuilt from a {@code parent[]} array instead of copying a list per relaxation.
 */
public final class Dijkstra {
    private static final int INF = Integer.MAX_VALUE;

    private Dijkstra() {
    }

    public static RouteResult shortestPath(MetroGraph graph, String fromId, String toId) {
        int source = graph.indexOf(fromId);
        int target = graph.indexOf(toId);
        int n = graph.size();

        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        // Entries are {node, distance}; stale entries are skipped instead of decrease-key.
        PriorityQueue<int[]> heap = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        heap.add(new int[] {source, 0});

        while (!heap.isEmpty()) {
            int[] top = heap.poll();
            int u = top[0];
            if (top[1] > dist[u]) {
                continue; // stale entry
            }
            if (u == target) {
                break; // early exit: target is settled
            }
            for (Edge edge : graph.edgesFrom(u)) {
                int candidate = dist[u] + edge.minutes();
                if (candidate < dist[edge.to()]) {
                    dist[edge.to()] = candidate;
                    parent[edge.to()] = u;
                    heap.add(new int[] {edge.to(), candidate});
                }
            }
        }

        if (dist[target] == INF) {
            return new RouteResult(List.of(), -1);
        }
        List<Station> route = new ArrayList<>();
        for (int at = target; at != -1; at = parent[at]) {
            route.add(graph.station(at));
        }
        Collections.reverse(route);
        return new RouteResult(route, dist[target]);
    }
}
