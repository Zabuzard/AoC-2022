// AOC Day 13
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()

    val pairs = lines.chunked(3).map { it[0].toSignalPackages()!! to it[1].toSignalPackages()!! }

    val correctlyOrderedPairs = pairs.withIndex().filter { it.value.isRightOrder() }

    val indexSum = correctlyOrderedPairs.sumOf { it.index + 1 }
    println("Sum of indices of correctly ordered pairs is $indexSum")

    val dividers = listOf(
        "[[2]]".toSignalPackages()!!,
        "[[6]]".toSignalPackages()!!
    )

    val allPackages =
        pairs.map(Pair<SignalPackages, SignalPackages>::toList).flatten().toMutableList()
    allPackages += dividers

    allPackages.sort()

    val decoderKey = dividers.map { allPackages.indexOf(it) + 1 }.reduce(Int::times)

    println("The decoder key is $decoderKey")
}

private fun Pair<SignalPackages, SignalPackages>.isRightOrder() =
    first <= second

private fun String.toSignalPackages(): SignalPackages? {
    if (isEmpty()) {
        return null
    }
    if (get(0).isDigit()) {
        return SignalPackages.SignalPackage(toInt())
    }

    var listDepth = 0
    var startOfElement = 0
    val signalPackages = mutableListOf<SignalPackages>()

    for ((index, symbol) in withIndex()) {
        val wrapCurrent = {
            substring(startOfElement + 1 until index).toSignalPackages()
                ?.let { signalPackages += it }
        }

        if (symbol == '[') {
            listDepth++
        } else if (symbol == ']') {
            listDepth--
            // Found end of current
            if (listDepth == 0) {
                wrapCurrent()
            }
        }
        if (symbol == ',' && listDepth == 1) {
            wrapCurrent()
            startOfElement = index
        }
    }

    return SignalPackages.SignalPackageList(signalPackages)
}

sealed class SignalPackages : Comparable<SignalPackages> {
    data class SignalPackageList(val signalPackages: List<SignalPackages>) : SignalPackages() {
        override fun compareTo(other: SignalPackages): Int {
            val otherList = other as? SignalPackageList ?: SignalPackageList(listOf(other))

            for ((first, second) in signalPackages.zip(otherList.signalPackages)) {
                first.compareTo(second).let {
                    // Individual values have a difference
                    if (it != 0) {
                        return it
                    }
                }
            }

            // Either ran out of items, or same size
            return signalPackages.size - otherList.signalPackages.size
        }
    }

    data class SignalPackage(val packageValue: Int) : SignalPackages() {
        override fun compareTo(other: SignalPackages): Int {
            return when (other) {
                is SignalPackage -> packageValue.compareTo(other.packageValue)
                is SignalPackageList -> SignalPackageList(listOf(this)).compareTo(other)
            }
        }
    }
}
