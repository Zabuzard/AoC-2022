import java.lang.IllegalArgumentException

// AOC Day 2
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines().filter(String::isNotBlank)

    val roundsDirect = lines.map(String::asRoundDirect)
    val totalScoreDirect = roundsDirect.map(Round::score).sum()
    println("Playing the given strategy (1) results in a score of $totalScoreDirect")

    val roundsOutcome = lines.map(String::asRoundOutcome)
    val totalScoreOutcome = roundsOutcome.map(Round::score).sum()
    println("Playing the given strategy (2) results in a score of $totalScoreOutcome")
}

private fun String.asRoundDirect() =
    split(" ", limit = 2).let { Round(it.first().asOpponentChoice(), it.last().asPlayerChoice()) }

private fun String.asRoundOutcome() =
    split(" ", limit = 2).let {
        val opponentChoice = it.first().asOpponentChoice()
        Round(opponentChoice, it.last().asPlayerChoiceFromOutcome(opponentChoice))
    }

private fun String.asOpponentChoice() = when (this) {
    "A" -> Choice.ROCK
    "B" -> Choice.PAPER
    "C" -> Choice.SCISSORS
    else -> throw IllegalArgumentException("Opponent choice $this is not supported")
}

private fun String.asPlayerChoice() = when (this) {
    "X" -> Choice.ROCK
    "Y" -> Choice.PAPER
    "Z" -> Choice.SCISSORS
    else -> throw IllegalArgumentException("Player choice $this is not supported")
}

private fun String.asPlayerChoiceFromOutcome(opponentChoice: Choice): Choice {
    val desiredOutcome = when (this) {
        "X" -> Outcome.LOSS
        "Y" -> Outcome.DRAW
        "Z" -> Outcome.WIN
        else -> throw IllegalArgumentException("Desired outcome $this is not supported")
    }

    return with(opponentChoice) {
        when (desiredOutcome) {
            Outcome.WIN -> losesOver()
            Outcome.DRAW -> this
            Outcome.LOSS -> winsOver()
        }
    }
}

enum class Choice {
    ROCK,
    PAPER,
    SCISSORS;

    fun winsOver() = when (this) {
        ROCK -> SCISSORS
        PAPER -> ROCK
        SCISSORS -> PAPER
    }

    fun losesOver() = when (this) {
        ROCK -> PAPER
        PAPER -> SCISSORS
        SCISSORS -> ROCK
    }
}

class Round(private val opponentChoice: Choice, private val playerChoice: Choice) {
    fun score(): Int {
        val choiceScore = when (playerChoice) {
            Choice.ROCK -> 1
            Choice.PAPER -> 2
            Choice.SCISSORS -> 3
        }

        val outcomeScore = when (outcome()) {
            Outcome.WIN -> 6
            Outcome.DRAW -> 3
            Outcome.LOSS -> 0
        }

        return outcomeScore + choiceScore
    }

    private fun outcome(): Outcome {
        if (opponentChoice == playerChoice) {
            return Outcome.DRAW
        }

        return if (playerChoice.winsOver() == opponentChoice) Outcome.WIN else Outcome.LOSS
    }
}

enum class Outcome {
    WIN,
    DRAW,
    LOSS
}
