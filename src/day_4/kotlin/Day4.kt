// AOC Day 4
fun main() {
    val lines = ClassLoader.getSystemResource("input.txt").readText().lines()
    val tasks = lines.map(String::toElfTask)

    val tasksThatContainOther = tasks.count(ElfTask::doesFullyContainRange)
    println("Tasks that contain the other range: $tasksThatContainOther")

    val tasksThatOverlap = tasks.count(ElfTask::doesOverlap)
    println("Tasks that overlap: $tasksThatOverlap")
}

private fun String.toElfTask() = this.split(",", limit = 2).map {
    val (startSection, endSection) = it.split("-", limit = 2).map(String::toInt)
    startSection..endSection
}.let { (first, second) -> ElfTask(first, second) }

private fun IntRange.contains(other: IntRange) = contains(other.first) and contains(other.last)
private fun IntRange.overlaps(other: IntRange) = contains(other.first) or contains(other.last)

data class ElfTask(val first: IntRange, val second: IntRange) {
    fun doesFullyContainRange() = first.contains(second) or second.contains(first)
    fun doesOverlap() = first.overlaps(second) or second.overlaps(first)
}
