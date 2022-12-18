// AOC Day 18
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()

    val points = lines.map(String::toPoint).toSet()
    val lavaBlob = LavaBlob(points)

    // Each neighbor not in the blob is air, and hence exposed a surface
    val surface = lavaBlob.lavaPoints.flatMap { it.neighbors() }.count { it !in lavaBlob }
    println("Surface area: $surface")

    val containedAirPoints = lavaBlob.containedAirPoints()
    val outsideSurface = lavaBlob.lavaPoints.flatMap { it.neighbors() }.count { it !in lavaBlob && it !in containedAirPoints }
    println("Outside surface area: $outsideSurface")
}

data class LavaBlob(val lavaPoints: Set<Point>) {
    private val xLimits: IntRange
    private val yLimits: IntRange
    private val zLimits: IntRange

    init {
        xLimits = limitBy(Point::x)
        yLimits = limitBy(Point::y)
        zLimits = limitBy(Point::z)
    }

    private fun limitBy(selector: (Point) -> Int) =
        lavaPoints.map(selector).stream().mapToInt { it }.summaryStatistics().let {
            it.min..it.max
        }

    operator fun contains(point: Point) = point in lavaPoints

    fun containedAirPoints(): Set<Point> {
        val cloud = cloud()
        val result = cloud.toMutableSet()

        tailrec fun Point.removeContained(
            start: Point = this,
            containedAirPoints: Set<Point> = setOf(start)
        ) {
            if (this !in cloud) result.removeAll(containedAirPoints + this)
            else neighbors().filter { it !in lavaPoints && it !in containedAirPoints }.forEach {
                return it.removeContained(start, containedAirPoints + this)
            }
        }

        cloud.forEach { it.removeContained() }

        return result
    }

    private fun cloud() =
        xLimits.flatMap { x ->
            yLimits.flatMap { y ->
                zLimits.map { z ->
                    Point(x, y, z)
                }
            }
        }.toSet()
}

private fun String.toPoint() =
    split(",", limit = 3)
        .map(String::toInt)
        .let { Point(it[0], it[1], it[2]) }

data class Point(val x: Int, val y: Int, val z: Int) {
    fun neighbors() = listOf(
        Point(x + 1, y, z),
        Point(x - 1, y, z),
        Point(x, y + 1, z),
        Point(x, y - 1, z),
        Point(x, y, z + 1),
        Point(x, y, z - 1)
    )
}
