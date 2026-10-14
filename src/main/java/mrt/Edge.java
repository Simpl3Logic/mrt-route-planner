package mrt;

/** A directed edge; {@code minutes} covers in-vehicle time or a transfer walk. */
public record Edge(int to, int minutes) {
}
