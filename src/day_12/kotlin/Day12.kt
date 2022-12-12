import io.github.zabuzard.maglev.external.algorithms.ShortestPathComputationBuilder
import io.github.zabuzard.maglev.external.graph.simple.SimpleEdge
import io.github.zabuzard.maglev.external.graph.simple.SimpleGraph

// AOC Day 12
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()

    val mountain = lines.toMountain()

    val graph = SimpleGraph<GridValue<Int>, SimpleEdge<GridValue<Int>>>()
    mountain.heights.flattenWithIndex().forEach(graph::addNode)
    for (row in mountain.heights.indices) {
        for (col in mountain.heights[0].indices) {
            val node = mountain.getValueAt(row, col)!!
            val currentHeight = node.value

            val neighbors = listOfNotNull(
                mountain.getValueAt(row - 1, col), // left
                mountain.getValueAt(row + 1, col), // right
                mountain.getValueAt(row, col + 1), // up
                mountain.getValueAt(row, col - 1), // down
            ).filter { it.value - currentHeight <= 1 } // walk down or at most one up

            neighbors.map { SimpleEdge(node, it, 1.0) }.forEach(graph::addEdge)
        }
    }

    val algo = ShortestPathComputationBuilder(graph).build()
    val cost = algo.shortestPathCost(mountain.start, mountain.end).orElseThrow()!!

    println("The fewest steps are: $cost")

    val lowStarts = mountain.heights.flattenWithIndex().filter { it.value == 0 }
    val costOfLowStarts = algo.shortestPathCost(lowStarts, mountain.end)
    println("The fewest steps for all low starts are: $costOfLowStarts")
}

private fun List<String>.toMountain(): Mountain {
    val charGrid = map { it.toCharArray().toList() }

    val heights = charGrid.map { row ->
        row.map {
            when (it) {
                'S' -> 'a'
                'E' -> 'z'
                else -> it
            }
        }
            .map { it - 'a' }
    }

    val flattened = charGrid.flattenWithIndex()
    val start = flattened.find { it.value == 'S' }!!.let { GridValue(it.rowIndex, it.colIndex, 0) }
    val end =
        flattened.find { it.value == 'E' }!!.let { GridValue(it.rowIndex, it.colIndex, 'z' - 'a') }

    return Mountain(heights, start, end)
}

data class Mountain(
    val heights: List<List<Int>>,
    val start: GridValue<Int>,
    val end: GridValue<Int>
) {
    fun getValueAt(row: Int, col: Int) =
        heights.getOrNull(row)?.getOrNull(col)?.let { GridValue(row, col, it) }
}

private fun <T> Iterable<Iterable<T>>.flattenWithIndex(): List<GridValue<T>> =
    withIndex().flatMap { (rowIndex, row) ->
        row.withIndex().map { (colIndex, value) ->
            GridValue(rowIndex, colIndex, value)
        }
    }

data class GridValue<out T>(val rowIndex: Int, val colIndex: Int, val value: T)
