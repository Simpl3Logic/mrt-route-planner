package mrt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class DijkstraTest {

    private static List<String> ids(RouteResult r) {
        return r.route().stream().map(Station::id).toList();
    }

    private static MetroGraph tiny() {
        MetroGraph g = new MetroGraph();
        for (String id : new String[] {"A", "B", "C", "D", "X"}) {
            g.addStation(id, id, 0, 0);
        }
        g.addEdge("A", "B", 1);
        g.addEdge("B", "C", 1);
        g.addEdge("A", "C", 5);   // direct but slower
        g.addEdge("C", "D", 1);
        return g;
    }

    @Test
    void prefersCheaperIndirectRoute() {
        RouteResult r = Dijkstra.shortestPath(tiny(), "A", "D");
        assertEquals(List.of("A", "B", "C", "D"), ids(r));
        assertEquals(3, r.totalMinutes());
    }

    @Test
    void sameStationIsZeroMinutes() {
        RouteResult r = Dijkstra.shortestPath(tiny(), "A", "A");
        assertEquals(List.of("A"), ids(r));
        assertEquals(0, r.totalMinutes());
    }

    @Test
    void unreachableDestinationDoesNotCrash() {
        RouteResult r = Dijkstra.shortestPath(tiny(), "A", "X");
        assertFalse(r.reachable());
        assertEquals(-1, r.totalMinutes());
    }

    @Test
    void edgesAreDirected() {
        assertFalse(Dijkstra.shortestPath(tiny(), "D", "A").reachable());
    }

    @Test
    void unknownStationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.shortestPath(tiny(), "A", "ZZZ"));
    }

    @Test
    void negativeWeightIsRejected() {
        MetroGraph g = tiny();
        assertThrows(IllegalArgumentException.class, () -> g.addEdge("A", "B", -1));
    }

    @Test
    void kaohsiungRedLineEndToEnd() throws IOException {
        MetroGraph g = MetroGraph.loadFromResources("kaohsiung");
        RouteResult r = Dijkstra.shortestPath(g, "R3", "R17");
        assertTrue(r.reachable());
        assertEquals("R3", r.route().get(0).id());
        assertEquals("R17", r.route().get(r.route().size() - 1).id());
    }

    @Test
    void kaohsiungTransferAtFormosaBoulevard() throws IOException {
        MetroGraph g = MetroGraph.loadFromResources("kaohsiung");
        // O1 -> R17 must change lines through the O5/R10 interchange.
        RouteResult r = Dijkstra.shortestPath(g, "O1", "R17");
        assertTrue(ids(r).containsAll(List.of("O5", "R10")));
    }

    @Test
    void matchesBruteForceOnEveryPair() throws IOException {
        MetroGraph g = MetroGraph.loadFromResources("kaohsiung");
        int n = g.size();
        long[][] d = new long[n][n]; // Floyd-Warshall as an independent oracle
        long inf = Long.MAX_VALUE / 4;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = i == j ? 0 : inf;
            }
            for (Edge e : g.edgesFrom(i)) {
                d[i][e.to()] = Math.min(d[i][e.to()], e.minutes());
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                RouteResult r = Dijkstra.shortestPath(g, g.station(i).id(), g.station(j).id());
                long expected = d[i][j] >= inf ? -1 : d[i][j];
                assertEquals(expected, r.totalMinutes(), g.station(i).id() + "->" + g.station(j).id());
            }
        }
    }
}
