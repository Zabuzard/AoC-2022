import kotlin.math.abs

// AOC Day 9
fun main() {
    val lines =
        ClassLoader.getSystemResource("input.txt").readText().lines().filter(String::isNotBlank)

    val moveInstructions = lines.map(String::toMoveInstruction)

    val rope = Rope()
    val visitedTailPositions = mutableSetOf(rope.getTail())
    render(rope.knots)
    moveInstructions.forEach { (direction, steps) ->
        repeat(steps) {
            rope move direction
            visitedTailPositions.add(rope.getTail())
            render(rope.knots)
        }
    }

    println("The tail visited ${visitedTailPositions.size} unique positions")
}

private fun String.toMoveInstruction(): MoveInstruction {
    val (directionRaw, stepsRaw) = split(" ", limit = 2)

    val direction = when (directionRaw) {
        "U" -> Direction.UP
        "R" -> Direction.RIGHT
        "L" -> Direction.LEFT
        "D" -> Direction.DOWN
        else -> throw AssertionError("Unsupported direction: $directionRaw")
    }

    return MoveInstruction(direction, stepsRaw.toInt())
}

enum class Direction {
    UP,
    RIGHT,
    LEFT,
    DOWN
}

data class MoveInstruction(val direction: Direction, val steps: Int)

data class GridPos(var x: Int, var y: Int) {
    infix fun move(direction: Direction) {
        when (direction) {
            Direction.UP -> y--
            Direction.RIGHT -> x++
            Direction.DOWN -> y++
            Direction.LEFT -> x--
        }
    }
}

class Rope {
    val knots = List(KNOT_AMOUNT) { GridPos(0, 0) }

    infix fun move(direction: Direction) {
        knots.first() move direction

        knots.windowed(2, 1) { (parentKnot, currentKnot) ->
            val xDist = abs(parentKnot.x - currentKnot.x)
            val yDist = abs(parentKnot.y - currentKnot.y)

            if (xDist <= 1 && yDist <= 1) {
                // Close enough, dont catch up
                return@windowed
            }

            // Catch up in each direction that needs to
            if (xDist != 0) {
                currentKnot move if (parentKnot.x > currentKnot.x) Direction.RIGHT else Direction.LEFT
            }
            if (yDist != 0) {
                currentKnot move if (parentKnot.y > currentKnot.y) Direction.DOWN else Direction.UP
            }
        }
    }

    fun getTail() = knots.last().copy()
}

private const val KNOT_AMOUNT = 10
private const val GRID_SIZE = 50

private fun render(knots: List<GridPos>) {
    val grid = Array(GRID_SIZE) { CharArray(GRID_SIZE) { '.' } }

    knots.withIndex().reversed().map { (knotIndex, pos) ->
        val (gridRow, gridCol) = pos.toGridIndex()

        val label = if (knotIndex == 0) 'H' else knotIndex.digitToChar()
        grid[gridRow][gridCol] = label
    }

    for (row in grid.indices) {
        for (col in grid[row].indices) {
            print(grid[row][col])
        }
        println()
    }
}

private fun GridPos.toGridIndex() = Pair(y + GRID_SIZE / 2, x + GRID_SIZE / 2)
