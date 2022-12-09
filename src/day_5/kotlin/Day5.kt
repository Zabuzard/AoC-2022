// AOC Day 5
fun main() {
    val lines = ClassLoader.getSystemResource("input.txt").readText().lines()

    val stacksForCraneMover9000 = computeStacks(lines)
    val moves = computeMoves(lines)

    moves.forEach { it.executeAsCrateMover9000On(stacksForCraneMover9000) }

    val textForCraneMover9000 = getTopText(stacksForCraneMover9000)
    println("After the moves with Crane Mover 9000, the top items are $textForCraneMover9000")

    val stacksForCraneMover9001 = computeStacks(lines)

    moves.forEach { it.executeAsCrateMover9001On(stacksForCraneMover9001) }

    val textForCraneMover9001 = getTopText(stacksForCraneMover9001)
    println("After the moves with Crane Mover 9001, the top items are $textForCraneMover9001")
}

private fun computeMoves(lines: List<String>) =
    lines.dropWhile(String::isNotBlank).drop(1).map(String::toMove)

private fun String.toMove(): Move {
    val (amount, fromStack, toStack) = MOVE_PATTERN.matchEntire(this)!!
        .groupValues
        .drop(1)
        .map(String::toInt)
    return Move(amount, fromStack, toStack)
}

data class Move(val amount: Int, val fromStack: Int, val toStack: Int) {
    fun executeAsCrateMover9000On(stacks: List<ArrayDeque<Char>>) {
        repeat(amount) {
            val item = stacks[fromStack - 1].removeFirst()
            stacks[toStack - 1].addFirst(item)
        }
    }

    fun executeAsCrateMover9001On(stacks: List<ArrayDeque<Char>>) {
        val items = ArrayList<Char>(amount)
        repeat(amount) {
            items.add(stacks[fromStack - 1].removeFirst())
        }

        items.asReversed().forEach { stacks[toStack - 1].addFirst(it) }
    }
}

private val MOVE_PATTERN = Regex("move (\\d+) from (\\d+) to (\\d+)")

private fun computeStacks(lines: List<String>): List<ArrayDeque<Char>> {
    // Example:
    //     [D]
    // [N] [C]
    // [Z] [M] [P]
    val craneInputRaw = lines.takeWhile(String::isNotBlank).dropLast(1)
    // Example:
    // 0: _D_
    // 1: NC_
    // 2: ZMP
    val craneInputRawDirect = craneInputRaw.map { line -> (1..line.length step 4).map { line[it] } }
    val stackAmount = craneInputRawDirect.maxOf(List<Char>::size)

    // Example:
    // 0: NZ
    // 1: DCM
    // 2: P
    val stacks = ArrayList<ArrayDeque<Char>>(stackAmount)
    for (i in 0 until stackAmount) {
        val stack = ArrayDeque<Char>()
        craneInputRawDirect.map { if (i >= it.size) ' ' else it[i] }.dropWhile { ' ' == it }
            .forEach(stack::add)
        stacks.add(stack)
    }

    return stacks
}

private fun getTopText(stacks: List<ArrayDeque<Char>>) =
    stacks.map(ArrayDeque<Char>::first).joinToString(transform = Char::toString, separator = "")
