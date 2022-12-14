// AOC Day 14
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
    val isVerbose = false

    val cave = lines.toCave()
    cave.render()

    var sandCount = 0
    var fillingSteps = 0
    while (true) {
        fillingSteps++
        if (isVerbose) println("Filling cave with sand, step: $fillingSteps")
        cave.simulateStep()

        if (isVerbose) cave.render()
        if (isVerbose) println()

        val currentSandCount = cave.countSand()
        if (currentSandCount == sandCount) {
            println("Deactivating sand source after $fillingSteps steps.")
            cave.stopSandSource()
            cave.render()
            break
        }
        sandCount = currentSandCount
    }

    var waitingSteps = 0
    while (true) {
        waitingSteps++
        if (isVerbose) println("Waiting for sand to stop, step: $waitingSteps")
        val isMovement = cave.simulateStep()

        if (isVerbose) cave.render()
        if (isVerbose) println()

        if (!isMovement) {
            break
        }
    }

    println(
        "Simulation ended after filling the cave for $fillingSteps steps" +
                " and then waiting for $waitingSteps steps," +
                " cave has $sandCount units of sand."
    )
    cave.render()
}

private fun List<String>.toCave() =
    map { line ->
        line.split(" -> ")
            .map { it.split(",") }
            .map { Point(it[0].toInt(), it[1].toInt()) }
    }.map(::RockPath).let(::Cave)

data class Point(val x: Int, val y: Int)

data class RockPath(val points: List<Point>) {
    fun contour(): List<Point> {
        val contour = mutableListOf(points.first())

        for ((start, end) in points.windowed(2, 1)) {
            val line = if (start.x != end.x) {
                (start.x toward end.x).map { Point(it, start.y) }
            } else {
                (start.y toward end.y).map { Point(start.x, it) }
            }

            contour += line.drop(1)
        }

        return contour
    }

    private infix fun Int.toward(to: Int): IntProgression {
        val step = if (this > to) -1 else 1
        return IntProgression.fromClosedRange(this, to, step)
    }
}

data class Cave(private val rockPaths: List<RockPath>) {
    private val minX: Int
    private val maxX: Int
    private val minY: Int
    private val maxY: Int

    private val grid: Array<Array<Entity>>

    private var isSandSourceActive = true

    init {
        findSummaryBy { it.y }.let {
            minY = it.min
            maxY = it.max + 2
        }

        findSummaryBy { it.x }.let {
            val caveExpansion = (maxY - minY)
            minX = it.min - caveExpansion
            maxX = it.max + caveExpansion
        }

        grid = Array(maxY - minY + 1) { Array(maxX - minX + 1) { Entity.AIR } }
        rockPaths.flatMap(RockPath::contour).forEach { grid[it] = Entity.ROCK }

        grid[START] = Entity.SAND

        for (x in minX..maxX) {
            grid[Point(x, maxY - 1)] = Entity.AIR
            grid[Point(x, maxY)] = Entity.ROCK
        }
    }

    private fun findSummaryBy(selector: (Point) -> Int) =
        (rockPaths.flatMap { it.points } + START)
            .map(selector).stream()
            .mapToInt { it }
            .summaryStatistics()

    fun simulateStep(): Boolean {
        var somethingMoved: Boolean = false

        for (y in maxY downTo minY) {
            for (x in minX..maxX) {
                val moved = moveEntityAt(Point(x, y))
                if (moved) {
                    somethingMoved = true
                }
            }
        }

        return somethingMoved
    }

    fun stopSandSource() {
        isSandSourceActive = false
    }

    private fun moveEntityAt(point: Point): Boolean {
        if (grid[point] != Entity.SAND) {
            // Only sand is subject to physics
            return false
        }

        val nextPositionCandidates = with(point) {
            val nextY = y + 1
            listOf(
                Point(x, nextY),
                Point(x - 1, nextY),
                Point(x + 1, nextY)
            )
        }
        val nextPosition =
            nextPositionCandidates.firstOrNull { (grid.getOrNull(it) ?: Entity.AIR) == Entity.AIR }

        if (nextPosition == null) {
            // Do not move
            return false
        }

        // Move
        grid[point] = if (point == START && isSandSourceActive) Entity.SAND else Entity.AIR
        grid.setOrNothing(nextPosition, Entity.SAND)
        return true
    }

    fun countSand() = grid.flatten().count { it == Entity.SAND }

    fun render() {
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val point = Point(x, y)

                val symbol = if (point == START) {
                    '+'
                } else {
                    when (grid[point]) {
                        Entity.ROCK -> '#'
                        Entity.AIR -> ' '
                        Entity.SAND -> '.'
                    }
                }

                print(symbol)
            }
            println()
        }
    }

    private fun Point.toIndex(): Pair<Int, Int> = y - minY to x - minX

    private operator fun Array<Array<Entity>>.get(point: Point) =
        point.toIndex().let { (row, col) ->
            grid[row][col]
        }

    private operator fun Array<Array<Entity>>.set(point: Point, entity: Entity) =
        point.toIndex().let { (row, col) ->
            grid[row][col] = entity
        }

    private fun Array<Array<Entity>>.setOrNothing(point: Point, entity: Entity) =
        point.toIndex().let { (row, col) ->
            if (row in grid.indices && col in grid[row].indices) {
                grid[row][col] = entity
            }
        }

    private fun Array<Array<Entity>>.getOrNull(point: Point) =
        point.toIndex().let { (row, col) ->
            grid.getOrNull(row)?.getOrNull(col)
        }
}

enum class Entity {
    ROCK,
    AIR,
    SAND
}

private val START = Point(500, 0)

private fun <T> Array<Array<T>>.flattenWithIndex(): List<GridValue<T>> =
    withIndex().flatMap { (rowIndex, row) ->
        row.withIndex().map { (colIndex, value) ->
            GridValue(rowIndex, colIndex, value)
        }
    }

data class GridValue<out T>(val rowIndex: Int, val colIndex: Int, val value: T)
