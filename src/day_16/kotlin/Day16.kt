import io.github.zabuzard.maglev.external.algorithms.ShortestPathComputationBuilder
import io.github.zabuzard.maglev.external.graph.Edge
import io.github.zabuzard.maglev.external.graph.simple.ReversedConsumer
import io.github.zabuzard.maglev.external.graph.simple.ReversedProvider
import io.github.zabuzard.maglev.external.graph.simple.SimpleGraph

// AOC Day 16
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
    val nameToValveData = lines.map(String::toValveData).associateBy(ValveData::name)

    // Time expanded graph
    val graph = SimpleGraph<ValveAtTime, NegativeSupportEdge<ValveAtTime>>()
    nameToValveData.keys.forEach { name ->
        repeat(MAX_MINUTES + 1) {
            graph.addNode(ValveAtTime(name, it))
        }
    }

    // Its a "longest path" problem. Since the graph is a DAG,
    // we can instead solve "shortest-path" on the negative graph.
    graph.nodes.forEach { source ->
        // Waiting
        if (source.timeMinute < MAX_MINUTES) {
            graph.addEdge(
                NegativeSupportEdge(
                    source,
                    ValveAtTime(source.name, source.timeMinute + 1),
                    0.0
                )
            )
        }

        nameToValveData[source.name]!!.adjacentValves.forEach { destination ->
            // Do not open valve
            if (source.timeMinute < MAX_MINUTES) {
                graph.addEdge(
                    NegativeSupportEdge(
                        source,
                        ValveAtTime(destination, source.timeMinute + 1),
                        0.0
                    )
                )
            }

            // Open valve
            if (source.timeMinute < MAX_MINUTES - 1) {
                val remainingTime = MAX_MINUTES - source.timeMinute
                val flowReleased = remainingTime * nameToValveData[source.name]!!.flowRate

                graph.addEdge(
                    NegativeSupportEdge(
                        source,
                        ValveAtTime(destination, source.timeMinute + 2),
                        -1.0 * flowReleased
                    )
                )
            }
        }
    }

    println(graph)

    val algo = ShortestPathComputationBuilder(graph)
        .resetOrdinaryDijkstra()
        .build()

    val tree = algo.shortestPathReachable(ValveAtTime("AA", 0))

    val path = tree.leaves
        .filter { it.timeMinute == 30 }
        .map { tree.getPathTo(it).orElseThrow() }
        .minBy { it.totalCost }

    println("Total cost: ${path.totalCost}")
    path.forEach { println(it.edge) }
    println()
    val openValves = path.filter {
        it.edge.destination.timeMinute - it.edge.source.timeMinute >= 2
    }.map { it.edge.source.name }

    // TODO Teach it to not open valves twice...
    println("Open valves: $openValves")
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

data class NegativeSupportEdge<N>(
    private val source: N,
    private val destination: N,
    private val cost: Double
) : Edge<N>, ReversedConsumer {
    private lateinit var reversedProvider: ReversedProvider

    override fun getCost(): Double = cost

    override fun getDestination(): N =
        if (reversedProvider.isReversed) source else destination

    override fun getSource(): N =
        if (reversedProvider.isReversed) destination else source

    override fun setReversedProvider(provider: ReversedProvider) {
        reversedProvider = provider
    }

    override fun toString(): String {
        return "$source -($cost)-> $destination"
    }
}
