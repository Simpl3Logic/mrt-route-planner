package mrt;

/** A metro station. {@code index} is its position in the graph's arrays. */
public record Station(int index, String id, String name, double longitude, double latitude) {
}
