import io.github.zabuzard.maglev.external.algorithms.ShortestPathComputationBuilder
import io.github.zabuzard.maglev.external.graph.simple.SimpleEdge
import io.github.zabuzard.maglev.external.graph.simple.SimpleGraph

// AOC Day 16
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
    val nameToValveData = lines.map(String::toValveData).associateBy(ValveData::name)

    // Time expanded graph
    val graph = SimpleGraph<ValveAtTime, SimpleEdge<ValveAtTime>>()
    nameToValveData.keys.forEach { name ->
        repeat(MAX_MINUTES + 1) {
            graph.addNode(ValveAtTime(name, it))
        }
    }

    // TODO Figure out how to properly invert the cost
    graph.nodes.forEach { source ->
        nameToValveData[source.name]!!.adjacentValves.forEach { destination ->
            // Do not open valve
            if (source.timeMinute < MAX_MINUTES) {
                graph.addEdge(
                    SimpleEdge(
                        source,
                        ValveAtTime(destination, source.timeMinute + 1),
                        1.0 + 10_000
                    )
                )
            }

            // Open valve
            if (source.timeMinute < MAX_MINUTES - 1) {
                val remainingTime = MAX_MINUTES - source.timeMinute
                val flowReleased = remainingTime * nameToValveData[source.name]!!.flowRate


                graph.addEdge(
                    SimpleEdge(
                        source,
                        ValveAtTime(destination, source.timeMinute + 2),
                        2.0 + (10_000 - flowReleased)
                    )
                )
            }
        }
    }

    val algo = ShortestPathComputationBuilder(graph)
        .build()

    val tree = algo.shortestPathReachable(ValveAtTime("AA", 0))
    val path = tree.leaves
        .filter { it.timeMinute == 30 }
        .map { tree.getPathTo(it).orElseThrow() }
        .minBy { it.totalCost }

    println(path)
    println()
    path.forEach{
        if (it.edge.destination.timeMinute - it.edge.source.timeMinute >= 2) {
            println("opening valve ${it.edge.source.name}")
        }
    }
}

private const val MAX_MINUTES = 30

private fun String.toValveData() =
    VALVE_DATA_PATTERN.matchEntire(this)!!.groupValues.let { (_, name, flowRate, adjacentValvesRaw) ->
        ValveData(name, flowRate.toInt(), adjacentValvesRaw.split(", "))
    }

private val VALVE_DATA_PATTERN =
    Regex("Valve (.+) has flow rate=(\\d+); tunnels? leads? to valves? (.+)")

data class ValveData(val name: String, val flowRate: Int, val adjacentValves: List<String>)

data class ValveAtTime(val name: String, val timeMinute: Int) {
    override fun toString(): String {
        return "$name($timeMinute)"
    }
}
