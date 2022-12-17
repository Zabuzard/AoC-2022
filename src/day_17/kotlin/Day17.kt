import java.lang.IllegalArgumentException
import kotlin.math.max

// AOC Day 17
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
    val verbose = false

    val directions = lines[0].map {
        when (it) {
            '>' -> Direction.RIGHT
            '<' -> Direction.LEFT
            else -> throw IllegalArgumentException("Unknown direction: $it")
        }
    }.asSequence().repeatIndefinitely().iterator()

    val cave = Cave()

    val tilesToSpawn = sequenceOf(Tile.MINUS, Tile.PLUS, Tile.HALF_SQUARE, Tile.LINE, Tile.SQUARE)
        .repeatIndefinitely().iterator()

    for (i in 0 until 1_000_000_000_000L) {
        val tile = tilesToSpawn.next()
        cave.spawnTile(tile)
        if (verbose) cave.render()

        do {
            val hasMoved = cave.moveStep(directions.next())
        } while (hasMoved)
    }

    //cave.render()

    println("Height ${cave.height}, resting rocks ${cave.restingRocks}")
}

class Cave() {
    // Orientation is from bottom to top, origin (0,0) is bottom-left
    private val grid: MutableList<Array<Entity>> = ArrayDeque()
    private var currentMovingTile: Tile? = null
    private var currentMovingTileOrigin: Point? = null
    var height = 0L
        private set
    var restingRocks = 0L
        private set

    private var topRowIsKnownNotAir = false
    private var bottomRowY = 0L

    fun render() {
        for (row in grid.lastIndex downTo 0) {
            for (entity in grid[row]) {
                print(entity.symbol)
            }
            println()
        }
        println()
    }

    fun spawnTile(tile: Tile) {
        require(currentMovingTile == null && currentMovingTileOrigin == null)
        currentMovingTile = tile
        currentMovingTileOrigin = Point(SPAWN_OFFSET.x, height + SPAWN_OFFSET.y)

        repeat(SPAWN_OFFSET.y.toInt() + tile.yToOffsets.size) {
            grid += Array(CAVE_WIDTH) { Entity.AIR }
        }

        for ((yOffset, xOffsets) in tile.yToOffsets.withIndex()) {
            for (xOffset in xOffsets) {
                val point =
                    currentMovingTileOrigin!! offsetBy Point(xOffset.toLong(), yOffset.toLong())
                grid[point] = Entity.FALLING_ROCK
            }
        }

        topRowIsKnownNotAir = false
    }

    fun moveStep(jetStreamDirection: Direction): Boolean {
        if (currentMovingTile == null || currentMovingTileOrigin == null) {
            return false
        }

        pushByJetStream(jetStreamDirection)
        val didFallDown = fallDown()
        if (didFallDown) {
            removeTopRowIfEmpty()
            return true
        }

        // Tile came to rest
        restTile()
        currentMovingTile = null
        currentMovingTileOrigin = null

        removeRowsBelowTopDownView()
        return false
    }

    private fun pushByJetStream(direction: Direction) {
        requireNotNull(currentMovingTile)
        requireNotNull(currentMovingTileOrigin)

        // Check if movement is possible
        val borderPointOffsets = currentMovingTile!!.let {
            if (direction == Direction.LEFT) it.leftBorderPoints else it.rightBorderPoints
        }

        for (borderPointOffset in borderPointOffsets) {
            val borderPoint = currentMovingTileOrigin!! offsetBy borderPointOffset

            val nextPosition = borderPoint moveTo direction
            if (grid.getOrNull(nextPosition) != Entity.AIR) {
                // Either occupied or out of bounds, cannot move tile
                return
            }
        }

        // Move
        val startY = currentMovingTileOrigin!!.y
        val endY = startY + currentMovingTile!!.yToOffsets.lastIndex
        for (y in startY..endY) {
            var xIndices: IntProgression = 0 until CAVE_WIDTH
            if (direction == Direction.RIGHT) {
                xIndices = xIndices.reversed()
            }

            for (x in xIndices) {
                val current = Point(x.toLong(), y)
                if (grid[current] != Entity.FALLING_ROCK) {
                    continue
                }

                val next = current moveTo direction

                grid[current] = Entity.AIR
                grid[next] = Entity.FALLING_ROCK
            }
        }
        currentMovingTileOrigin = currentMovingTileOrigin!! moveTo direction
    }

    private fun fallDown(): Boolean {
        requireNotNull(currentMovingTile)
        requireNotNull(currentMovingTileOrigin)

        // Check if movement is possible
        val borderPointOffsets = currentMovingTile!!.bottomBorderPoints

        for (borderPointOffset in borderPointOffsets) {
            val borderPoint = currentMovingTileOrigin!! offsetBy borderPointOffset

            val nextPosition = borderPoint.moveDown()
            if (grid.getOrNull(nextPosition) != Entity.AIR) {
                // Either occupied or out of bounds, tile comes to rest
                return false
            }
        }

        // Move
        val startY = currentMovingTileOrigin!!.y
        val endY = startY + currentMovingTile!!.yToOffsets.lastIndex
        for (y in startY..endY) {
            for (x in 0 until CAVE_WIDTH) {
                val current = Point(x.toLong(), y)
                if (grid[current] != Entity.FALLING_ROCK) {
                    continue
                }

                val next = current.moveDown()

                grid[current] = Entity.AIR
                grid[next] = Entity.FALLING_ROCK
            }
        }
        currentMovingTileOrigin = currentMovingTileOrigin!!.moveDown()

        return true
    }

    private fun restTile() {
        for ((yOffset, xOffsets) in currentMovingTile!!.yToOffsets.withIndex()) {
            for (xOffset in xOffsets) {
                val point =
                    currentMovingTileOrigin!! offsetBy Point(xOffset.toLong(), yOffset.toLong())

                require(grid[point] == Entity.FALLING_ROCK)
                grid[point] = Entity.RESTING_ROCK
            }
        }

        val currentTileHeight = currentMovingTileOrigin!!.y + currentMovingTile!!.yToOffsets.size
        height = max(height, currentTileHeight)

        restingRocks++
    }

    private fun removeTopRowIfEmpty() {
        if (topRowIsKnownNotAir) {
            return
        }

        if (grid.last().all { it == Entity.AIR }) {
            grid.removeLast()
            return
        }

        topRowIsKnownNotAir = true
    }

    private fun removeRowsBelowTopDownView() {
        val lowestRowWithRocksFromTopDownView = (0 until CAVE_WIDTH).minOf { x ->
            // row with first rocks if viewed from top-down, for this x
            grid.indices.lastOrNull() { row -> grid[row][x] != Entity.AIR } ?: 0
        }

        val rowsToRemove = lowestRowWithRocksFromTopDownView - 3

        if (rowsToRemove <= 0) {
            return
        }

        // Remove all rows below that
        repeat(rowsToRemove) {
            grid.removeFirst()
        }

        bottomRowY += rowsToRemove
    }

    private operator fun <T> List<Array<T>>.get(point: Point) =
        this[(point.y - bottomRowY).toInt()][point.x.toInt()]

    private fun <T> List<Array<T>>.getOrNull(point: Point) =
        getOrNull((point.y - bottomRowY).toInt())?.getOrNull(point.x.toInt())

    private operator fun <T> List<Array<T>>.set(point: Point, entity: T) {
        this[(point.y - bottomRowY).toInt()][point.x.toInt()] = entity
    }
}

private const val CAVE_WIDTH = 7
private val SPAWN_OFFSET = Point(2, 3)

enum class Entity(val symbol: Char) {
    FALLING_ROCK('@'),
    RESTING_ROCK('#'),
    AIR('.')
}

enum class Tile(pattern: String) {
    MINUS("####"),
    PLUS(
        """
        .#.
        ###
        .#.
    """.trimIndent()
    ),
    HALF_SQUARE(
        """
        ..#
        ..#
        ###
    """.trimIndent()
    ),
    LINE(
        """
        #
        #
        #
        #
    """.trimIndent()
    ),
    SQUARE(
        """
        ##
        ##
    """.trimIndent()
    );

    // Origin (0,0) is bottom-left, orientation is from bottom to top
    val yToOffsets: List<List<Int>>
    val leftBorderPoints: List<Point>
    val rightBorderPoints: List<Point>
    val bottomBorderPoints: List<Point>

    init {
        yToOffsets = pattern.lines().asReversed().map {
            it.withIndex()
                .filter { (_, c) -> c == '#' }
                .map { (x, _) -> x }
        }

        leftBorderPoints = getBorderContour(Direction.LEFT)
        rightBorderPoints = getBorderContour(Direction.RIGHT)
        bottomBorderPoints = getBottomContour()
    }

    private fun getBorderContour(direction: Direction): List<Point> =
        yToOffsets.mapIndexed { y, xOffsets ->
            val x = if (direction == Direction.LEFT) xOffsets.min() else xOffsets.max()
            Point(x.toLong(), y.toLong())
        }

    private fun getBottomContour(): List<Point> {
        val allPoints =
            yToOffsets.flatMapIndexed { y, xOffsets ->
                xOffsets.map { x ->
                    Point(
                        x.toLong(),
                        y.toLong()
                    )
                }
            }
        return allPoints.groupBy(Point::x).values.map { it.minBy(Point::y) }
    }
}

enum class Direction {
    LEFT,
    RIGHT
}

data class Point(val x: Long, val y: Long) {
    infix fun moveTo(direction: Direction): Point {
        val xOffset = if (direction == Direction.LEFT) -1 else 1
        return Point(x + xOffset, y)
    }

    fun moveDown(): Point = Point(x, y - 1)

    infix fun offsetBy(offset: Point) =
        Point(x + offset.x, y + offset.y)
}

private fun <T> Sequence<T>.repeatIndefinitely(): Sequence<T> =
    generateSequence(this) { this }.flatten()
