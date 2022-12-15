import java.util.*
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.asStream
import kotlin.system.measureTimeMillis

// AOC Day 15
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
    val verbose = false

    val cave: Cave
    measureTimeMillis {
        cave = lines.toCave()
    }.let { println("Parsing and optimizing cave in $it ms") }
    if (verbose) cave.render()

    val y = 2000000
    val cannotBeBeaconAtY: Int
    measureTimeMillis {
        cannotBeBeaconAtY = cave.cannotBeBeaconAtY(y)
    }.let { println("Checking y for beacon stuff in $it ms") }
    println("At y=$y, $cannotBeBeaconAtY positions cannot be beacons")

    val beaconPos: Point
    measureTimeMillis {
        beaconPos = cave.findBeaconIn(0..4_000_000)
    }.let { println("Found beacon pos in $it ms") }
    println("The beacon must be at $beaconPos, its tuning frequency is ${beaconPos.tuningFrequency()}")
}

private val SENSOR_MEASUREMENT_PATTERN =
    Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")

private fun List<String>.toCave() =
    map { SENSOR_MEASUREMENT_PATTERN.matchEntire(it)!!.groupValues }
        .map { (_, sensorX, sensorY, beaconX, beaconY) ->
            Point(
                sensorX.toInt(),
                sensorY.toInt()
            ) to Point(
                beaconX.toInt(),
                beaconY.toInt()
            )
        }
        .map { (sensor, closestBeacon) -> SensorMeasurement(sensor, closestBeacon) }
        .let(::Cave)

data class Point(val x: Int, val y: Int) {
    fun distanceTo(other: Point) =
        abs(x - other.x) + abs(y - other.y)

    fun tuningFrequency() = x * 4_000_000L + y
}

data class SensorMeasurement(val sensor: Point, val closestBeacon: Point) {
    fun sensorRangePerY(): Sequence<Pair<Int, IntRange>> {
        val distance = sensor.distanceTo(closestBeacon)

        return (0..distance).asSequence().flatMap { yOffset ->
            val xOffset = distance - yOffset
            val xRange = (sensor.x - xOffset)..(sensor.x + xOffset)
            sequenceOf(
                sensor.y + yOffset to xRange,
                sensor.y - yOffset to xRange,
            )
        }
    }
}

data class Cave(private val sensorMeasurements: List<SensorMeasurement>) {
    private val minX: Int
    private val maxX: Int
    private val minY: Int
    private val maxY: Int

    private val grid = mutableMapOf<Point, Entity>()
    private val sensorCoverageRangesY = mutableMapOf<Int, MutableList<IntRange>>()

    init {
        // Sensor measurements
        sensorMeasurements.map(SensorMeasurement::sensor).forEach { grid[it] = Entity.SENSOR }
        sensorMeasurements.map(SensorMeasurement::closestBeacon)
            .forEach { grid[it] = Entity.BEACON }

        // Coverage
        sensorMeasurements.stream().flatMap { it.sensorRangePerY().asStream() }
            .forEach { (y, xRange) ->
                sensorCoverageRangesY.getOrPut(y, ::mutableListOf).add(xRange)
            }
        sensorCoverageRangesY.keys.forEach {
            sensorCoverageRangesY[it] = sensorCoverageRangesY[it]!!.asCompressed().toMutableList()
        }

        // Limits
        sequenceOf(
            sensorMeasurements.map { it.closestBeacon }.map { it.y },
            sensorCoverageRangesY.keys
        ).flatten().asStream().mapToInt { it }.summaryStatistics().let {
            minY = it.min
            maxY = it.max
        }

        sequenceOf(
            sensorMeasurements.map { it.closestBeacon }.map { it.x },
            sensorCoverageRangesY.values.flatMap {
                it.flatMap { xRange ->
                    listOf(
                        xRange.first,
                        xRange.last
                    )
                }
            }
        ).flatten().asStream().mapToInt { it }.summaryStatistics().let {
            minX = it.min
            maxX = it.max
        }
    }

    fun render() {
        println("y: $minY .. $maxY")
        println("x: $minX .. $maxX")

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val point = Point(x, y)

                val symbol = when (grid[point]) {
                    Entity.SENSOR -> 'S'
                    Entity.BEACON -> '+'
                    Entity.NOTHING, null -> if (isCoveredBySensor(point)) '.' else ' '
                }

                print(symbol)
            }
            println()
        }
    }

    private fun isCoveredBySensor(point: Point) =
        sensorCoverageRangesY[point.y]?.any { point.x in it } ?: false

    fun cannotBeBeaconAtY(y: Int) =
        (minX..maxX).map { Point(it, y) }.count(::cannotBeBeacon)

    private fun cannotBeBeacon(point: Point) =
        when (grid[point]) {
            Entity.SENSOR -> true
            Entity.BEACON -> false
            Entity.NOTHING, null -> isCoveredBySensor(point)
        }

    private fun canBeNewBeacon(point: Point) =
        when (grid[point]) {
            Entity.SENSOR -> false
            Entity.BEACON -> false
            Entity.NOTHING, null -> !isCoveredBySensor(point)
        }

    fun findBeaconIn(range: IntRange): Point =
        IntStream.rangeClosed(range.first, range.last).parallel().boxed().flatMap { y ->
            val searchRanges = searchRanges(sensorCoverageRangesY[y] ?: emptyList())
            searchRanges.restrictToView(range).flatten().map { x -> Point(x, y) }.stream()
        }.filter(::canBeNewBeacon)
            .findAny()
            .orElseThrow()

    private fun searchRanges(sensorCoveredRange: List<IntRange>): List<IntRange> {
        if (sensorCoveredRange.isEmpty()) {
            return listOf(minX..maxX)
        }

        val searchRanges = mutableListOf(minX until sensorCoveredRange.first().first)

        searchRanges += sensorCoveredRange.zipWithNext { left, right ->
            left.last + 1 until right.first
        }

        searchRanges += sensorCoveredRange.last().last + 1..maxX

        return searchRanges
    }

    private fun List<IntRange>.asCompressed(): List<IntRange> {
        if (isEmpty()) {
            return emptyList()
        }

        val sortedRanges = PriorityQueue(Comparator.comparingInt(IntRange::first)).also {
            it.addAll(this)
        }

        val result = mutableListOf<IntRange>()

        var compressedRange = sortedRanges.poll()
        while (sortedRanges.isNotEmpty()) {
            val range = sortedRanges.poll()

            if (compressedRange.overlaps(range)) {
                compressedRange = compressedRange.merge(range)
            } else {
                result += compressedRange
                compressedRange = range
            }
        }
        result += compressedRange

        return result
    }

    private fun IntRange.overlaps(other: IntRange) =
        max(first, other.first) <= min(last, other.last) + 1

    private fun IntRange.merge(other: IntRange) =
        min(first, other.first)..max(last, other.last)

    private fun List<IntRange>.restrictToView(view: IntRange): List<IntRange> {
        val result = mutableListOf<IntRange>()

        for (range in this) {
            val restrictedRange = if (range.first in view && range.last in view) {
                range
            } else if (range.first in view && range.last !in view) {
                range.first..view.last
            } else if (range.first !in view && range.last in view) {
                view.first..range.last
            } else {
                null
            }

            if (restrictedRange != null) {
                result += restrictedRange
            }
        }

        return result
    }
}

enum class Entity {
    SENSOR,
    BEACON,
    NOTHING
}
