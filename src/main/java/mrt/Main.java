package mrt;

import java.io.IOException;
import java.util.stream.Collectors;

/** Usage: {@code java -jar mrt-route-planner.jar <city> <fromId> <toId>} */
public final class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: <city> <fromStationId> <toStationId>   e.g. kaohsiung O1 R17");
            System.exit(2);
        }
        MetroGraph graph = MetroGraph.loadFromResources(args[0]);
        RouteResult result;
        try {
            result = Dijkstra.shortestPath(graph, args[1], args[2]);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
            return;
        }
        if (!result.reachable()) {
            System.out.println("No route from " + args[1] + " to " + args[2]);
            return;
        }
        System.out.println(result.route().stream().map(Station::name).collect(Collectors.joining(" -> ")));
        System.out.println("Total: " + result.totalMinutes() + " min, " + (result.route().size() - 1) + " hops");
    }
}
