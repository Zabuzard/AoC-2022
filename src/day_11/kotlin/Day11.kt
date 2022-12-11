import java.lang.IllegalArgumentException
import java.util.Queue

// AOC Day 11
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
    val isVerbose = false

    val monkeys = lines.chunked(7).map(List<String>::toMonkey)
    val indexToMonkey = monkeys.associateBy(Monkey::monkeyIndex)
    val indexToInspectedItems = mutableMapOf<Int, Int>()

    val gcd = monkeys.map(Monkey::divisibleBy).reduce(Long::times)

    for (round in 1..10_000) {
        println("Round $round")
        for (monkey in monkeys) {
            if (isVerbose) println("Monkey ${monkey.monkeyIndex}:")
            while (monkey.items.isNotEmpty()) {
                var item = monkey.items.poll()
                indexToInspectedItems[monkey.monkeyIndex] =
                    (indexToInspectedItems[monkey.monkeyIndex] ?: 0).inc()
                if (isVerbose) println("  Monkey inspects an item with a worry level of $item.")

                item = monkey.operation(item)
                if (isVerbose) println("    Worry level is changed to $item.")

                // Part 1
                // item /= 3
                // if (isVerbose) println("    Monkey gets bored with item. Worry level is divided by 3 to $item.")

                val monkeyTargetIndex = if (item % monkey.divisibleBy == 0L) {
                    monkey.trueMonkeyTarget.also {
                        if (isVerbose) println("    Current worry level is divisible by ${monkey.divisibleBy}.")
                    }
                } else {
                    monkey.falseMonkeyTarget.also {
                        if (isVerbose) println("    Current worry level is not divisible by ${monkey.divisibleBy}.")
                    }
                }

                item %= gcd

                indexToMonkey[monkeyTargetIndex]!!.items.add(item)
                if (isVerbose) println("    Item with worry level $item is thrown to monkey $monkeyTargetIndex.")
            }
        }

        if (isVerbose) println("After round $round, the monkeys are holding items with these worry levels:")
        if (isVerbose) monkeys.map { "  Monkey ${it.monkeyIndex}: ${it.items}" }.forEach(::println)
    }

    println("Results:")
    monkeys.map { "  Monkey ${it.monkeyIndex} inspected items ${indexToInspectedItems[it.monkeyIndex]} times." }
        .forEach(::println)

    val monkeyBusiness = indexToInspectedItems.values.sortedDescending().take(2).map(Int::toLong).reduce(Long::times)
    println("The level of monkey business is $monkeyBusiness.")
}

private fun List<String>.toMonkey() =
    Monkey(
        monkeyIndex = "Monkey (\\d+):".toRegex().matchEntire(get(0))!!.groupValues[1].toInt(),
        items = java.util.ArrayDeque(get(1).split(": ")[1].split(", ").map(String::toLong)),
        operation = get(2).split("Operation: ")[1].toOperation(),
        divisibleBy = get(3).split("divisible by ")[1].toLong(),
        trueMonkeyTarget = get(4).split("throw to monkey ")[1].toInt(),
        falseMonkeyTarget = get(5).split("throw to monkey ")[1].toInt()
    )

private fun String.toOperation(): (Long) -> Long =
    OPERATION_PATTERN.matchEntire(this)!!.groupValues.let {
        { old ->
            val left = if (it[1] == "old") old else it[1].toLong()
            val right = if (it[3] == "old") old else it[3].toLong()

            when (it[2]) {
                "+" -> left + right
                "*" -> left * right
                else -> throw IllegalArgumentException("Unsupported operation: $this")
            }
        }
    }

private val OPERATION_PATTERN = Regex("new = (\\d+|old) ([+*]) (\\d+|old)")

data class Monkey(
    val monkeyIndex: Int,
    val items: Queue<Long>,
    val operation: (Long) -> Long,
    val divisibleBy: Long,
    val trueMonkeyTarget: Int,
    val falseMonkeyTarget: Int
)
