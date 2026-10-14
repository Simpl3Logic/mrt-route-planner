# MRT Route Planner
[![CI](https://github.com/Simpl3Logic/mrt-route-planner/actions/workflows/ci.yml/badge.svg)](https://github.com/Simpl3Logic/mrt-route-planner/actions/workflows/ci.yml)

Shortest travel time between metro stations, using Dijkstra's algorithm.
Originally my university capstone (Java Swing, Kaohsiung MRT); rewritten as a
small, tested, data-driven library + CLI.

## What changed from the capstone

| Capstone version | This version |
|---|---|
| Adjacency matrix, linear-scan min search: O(V²) | Adjacency list + binary heap: O((V + E) log V) |
| Copied a path `ArrayList` on every relaxation (up to O(V³)) | `parent[]` array, path rebuilt once at the end |
| Unreachable destination crashed (`index -1`) | Returns an empty route (`reachable() == false`) |
| `num_Vertices = 30` hard-coded | Size comes from the data files |
| Relative paths to `.txt` files in the working dir | CSV files loaded from the classpath |
| No tests | JUnit 5 tests, including a Floyd–Warshall cross-check over every station pair |

## Run

```bash
mvn test
mvn package
java -cp target/classes mrt.Main kaohsiung R3 O1
# R3 -> R4 -> R4A -> R5 -> R6 -> R7 -> R8 -> R9 -> R10 -> O5 -> O4 -> O2 -> O1
# Total: 30 min, 12 hops
```

## Data format

`src/main/resources/data/<city>/stations.csv`: `id,name,longitude,latitude`
`src/main/resources/data/<city>/edges.csv`: `from,to,minutes` (directed; list both directions
for a normal track, and add transfer walks as edges between interchange platforms)

To add a city, add a new folder with those two files and pass its name to `mrt.Main`.
No code changes needed.

## Design notes

- Graph is directed because travel times can differ by direction (e.g. dwell time at some stations).
- Heap uses lazy deletion (skip stale entries) instead of decrease-key; simpler with `PriorityQueue`.
- Early exit once the target is popped, which is correct because weights are non-negative
  (negative weights are rejected when the edge is added).
- Station names in the Kaohsiung sample currently equal their IDs; the original name file lost its
  encoding. Fill in the `name` column for readable output.

## Possible next steps

- Taipei Metro data (see TDX open data for stations and travel times)
- A* with a straight-line-distance heuristic
- Penalty minutes for transfers, and a minimum-transfers mode
