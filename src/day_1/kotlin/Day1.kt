// AOC Day 1
fun main() {
    val lines = ClassLoader.getSystemResource("input.txt").readText().lines()
    val elves = parseElves(lines)

    val mostCaloriesElf = elves.maxBy { it.total }

    println("The elf with the most calories carries ${mostCaloriesElf.total} calories in total")

    val top3CaloriesSum =
        elves.sortedByDescending { it.total }.take(3).sumOf { it.total }
    println("The top 3 elves carry $top3CaloriesSum calories in total")
}

private fun parseElves(lines: List<String>): List<Elf> {
    val elves = ArrayList<Elf>()
    var currentElf = Elf()
    for (line in lines) {
        if (line.isBlank()) {
            // Wrap up
            elves.add(currentElf)
            currentElf = Elf()
            continue
        }

        val calories = line.toInt()
        currentElf.calories.add(calories)
    }

    // Wrap up trailing
    if (currentElf.calories.isNotEmpty()) {
        elves.add(currentElf)
    }

    return elves
}

data class Elf(val calories: MutableList<Int> = ArrayList()) {
    val total: Int by lazy { calories.sum() }
}
