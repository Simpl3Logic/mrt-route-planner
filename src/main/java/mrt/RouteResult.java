package mrt;

import java.util.List;

/** Result of a shortest-path query. An unreachable destination yields an empty route. */
public record RouteResult(List<Station> route, int totalMinutes) {
    public boolean reachable() {
        return !route.isEmpty();
    }
}
