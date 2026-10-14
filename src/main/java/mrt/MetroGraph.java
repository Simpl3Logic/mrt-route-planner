package mrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Directed, weighted metro network stored as an adjacency list.
 * Loaded from two CSV files, so a new city only needs new data files.
 */
public final class MetroGraph {
    private final List<Station> stations = new ArrayList<>();
    private final Map<String, Integer> indexById = new HashMap<>();
    private final List<List<Edge>> adjacency = new ArrayList<>();

    public int addStation(String id, String name, double longitude, double latitude) {
        if (indexById.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate station id: " + id);
        }
        int index = stations.size();
        stations.add(new Station(index, id, name, longitude, latitude));
        indexById.put(id, index);
        adjacency.add(new ArrayList<>());
        return index;
    }

    public void addEdge(String fromId, String toId, int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Dijkstra needs non-negative weights: " + fromId + "->" + toId);
        }
        adjacency.get(indexOf(fromId)).add(new Edge(indexOf(toId), minutes));
    }

    public int indexOf(String id) {
        Integer index = indexById.get(id);
        if (index == null) {
            throw new IllegalArgumentException("Unknown station id: " + id);
        }
        return index;
    }

    public int size() {
        return stations.size();
    }

    public Station station(int index) {
        return stations.get(index);
    }

    public List<Edge> edgesFrom(int index) {
        return adjacency.get(index);
    }

    /** Loads {@code /data/<city>/stations.csv} and {@code edges.csv} from the classpath. */
    public static MetroGraph loadFromResources(String city) throws IOException {
        MetroGraph graph = new MetroGraph();
        for (String[] row : readCsv("/data/" + city + "/stations.csv")) {
            graph.addStation(row[0], row[1], Double.parseDouble(row[2]), Double.parseDouble(row[3]));
        }
        for (String[] row : readCsv("/data/" + city + "/edges.csv")) {
            graph.addEdge(row[0], row[1], Integer.parseInt(row[2]));
        }
        return graph;
    }

    private static List<String[]> readCsv(String resource) throws IOException {
        InputStream in = MetroGraph.class.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("Resource not found: " + resource);
        }
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(line.trim().split(","));
                }
            }
        }
        return rows;
    }
}
