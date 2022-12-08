// AOC Day 8
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()

    val forest = lines.map(String::toCharArray).map { it.map(Char::digitToInt).toList() }

    val visibleTrees = forest.flattenWithIndex()
        .count { (row, col, _) -> isTreeVisible(row, col, forest) }

    println("The amount of visible tree is $visibleTrees")

    println("Scenic score of tree at (1, 2) is ${scenicTreeScore(1, 2, forest)}.")
    println("Scenic score of tree at (3, 2) is ${scenicTreeScore(3, 2, forest)}.")

    val bestScenicTreeScore = forest.flattenWithIndex()
        .maxOf { (row, col, _) -> scenicTreeScore(row, col, forest) }

    println("The best scenic score of a tree is $bestScenicTreeScore")
}

fun scenicTreeScore(row: Int, col: Int, forest: List<List<Int>>): Int {
    val treeHeight = forest[row][col]

    return getAllViewsFrom(row, col, forest)
        .map { getVisibleTrees(it, treeHeight) }
        .reduce(Int::times)
}

fun getVisibleTrees(view: List<Int>, treeHeight: Int): Int {
    val smallerTrees = view.takeWhile { it < treeHeight }.count()

    return if (smallerTrees < view.size) {
        // There is a high tree blocking the view, but it can be seen as well
        smallerTrees + 1
    } else smallerTrees
}

fun isTreeVisible(row: Int, col: Int, forest: List<List<Int>>): Boolean {
    val treeHeight = forest[row][col]

    return getAllViewsFrom(row, col, forest)
        .any { isTreeVisible(it, treeHeight) }
}

fun isTreeVisible(view: List<Int>, treeHeight: Int) =
    view.all { it < treeHeight }

fun getAllViewsFrom(
    rowIndex: Int,
    colIndex: Int,
    forest: List<List<Int>>
): List<List<Int>> {
    val row = forest[rowIndex]
    val col = forest.col(colIndex)

    val leftRow = row.viewToLeft(colIndex)
    val rightRow = row.viewToRight(colIndex)
    val upCol = col.viewToLeft(rowIndex)
    val downCol = col.viewToRight(rowIndex)

    return listOf(leftRow, rightRow, upCol, downCol)
}

private fun <T> List<List<T>>.col(col: Int) =
    map { it[col] }.toList()

private fun <T> List<T>.viewToLeft(cutIndex: Int) =
    slice(0 until cutIndex).asReversed()

private fun <T> List<T>.viewToRight(cutIndex: Int) =
    slice(cutIndex + 1..lastIndex)

private fun <T> Iterable<Iterable<T>>.flattenWithIndex(): List<GridValue<T>> =
    withIndex().flatMap { (rowIndex, row) ->
        row.withIndex().map { (colIndex, value) ->
            GridValue(rowIndex, colIndex, value)
        }
    }

data class GridValue<out T>(val rowIndex: Int, val colIndex: Int, val value: T)
