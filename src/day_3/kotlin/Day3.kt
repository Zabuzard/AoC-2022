// AOC Day 3
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
    
    val rucksacks = lines.map(String::toCharArray).map{
        val rucksackItems = it.map(::Item)
        val rucksackCompartmentsRaw = rucksackItems.chunked(rucksackItems.size / 2)

        val firstCompartment = Compartment(rucksackCompartmentsRaw[0])
        val secondCompartment = Compartment(rucksackCompartmentsRaw[1])
        Rucksack(firstCompartment, secondCompartment)
    }

    val sumPriority = rucksacks.map { findCommon(it.first, it.second) }.map(Item::priority).sum()

    println("The rucksacks common items priorities sum up to $sumPriority")

    val groups = rucksacks.chunked(3)
    val sumGroupPriority = groups.map(::findCommon).map(Item::priority).sum()

    println("The rucksacks group common items priorities sum up to $sumGroupPriority")
}

private fun findCommon(first: Compartment, second: Compartment) = first.items.intersect(second.items.toSet()).first()

private fun findCommon(rucksacks: List<Rucksack>) = rucksacks.map { it.first.items.plus(it.second.items).toMutableSet() }
    .reduce{ acc, it -> acc.apply { retainAll(it) }}.first()

data class Item(val type: Char) {
    fun priority() = if (type.isLowerCase()) type - 'a' + 1 else type - 'A' + 27
}

data class Compartment(val items: List<Item>)

data class Rucksack(val first: Compartment, val second: Compartment)
