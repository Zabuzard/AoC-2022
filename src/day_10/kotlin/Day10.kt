import kotlin.math.abs

// AOC Day 10
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()

    val instructionIter = lines.map(String::toInstruction).iterator()

    val register = Register(1)
    var cycle = 0
    val signalStrengths = mutableListOf<Int>()
    var screenX = 0
    var screenY = 0

    while (instructionIter.hasNext()) {
        val instruction = instructionIter.next()
        repeat(instruction.cycles) {
            cycle++
            if (isInterestingCycle(cycle)) {
                // printSimulationDetails(cycle, register)
                signalStrengths.add(signalStrength(cycle, register))
            }

            val screenSymbol = if (abs(screenX - register.value) <= 1) '#' else '.'
            print(screenSymbol)

            screenX++
            if (cycle % 40 == 0) {
                println()
                screenX = 0
                screenY++
            }
        }
        instruction.execute(register)
    }
    printSimulationDetails(cycle, register)

    val sumSignalStrengthOfFirstMeasurements = signalStrengths.take(6).sum()
    println("The sum of the signal strengths of the first 6 measurements are $sumSignalStrengthOfFirstMeasurements")
}

fun printSimulationDetails(cycle: Int, register: Register) {
    println(
        "Value during cycle $cycle is ${register.value}, signal strength is ${
            signalStrength(
                cycle,
                register
            )
        }"
    )
}

fun isInterestingCycle(cycle: Int) = (cycle - 20) % 40 == 0

fun signalStrength(cycle: Int, register: Register) = cycle * register.value

data class Register(var value: Int)

private fun String.toInstruction() =
    if (startsWith("noop")) {
        Noop()
    } else {
        AddX(split(" ", limit = 2)[1].toInt())
    }

interface Instruction {
    val cycles: Int
    fun execute(register: Register)
}

class AddX(private val value: Int) : Instruction {
    override val cycles = 2

    override fun execute(register: Register) {
        register.value += value
    }
}

class Noop : Instruction {
    override val cycles = 1

    override fun execute(register: Register) {
        // NOOP does not do anything
    }
}
