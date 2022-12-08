// AOC Day 8
fun main() {
    val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()

    val forest = lines.map(String::toCharArray).map { it.map(Char::digitToInt).toList() }

    val visibleTrees = forest.indices2D()
        .count { (row, col) -> isTreeVisible(row, col, forest) }

    println("The amount of visible tree is $visibleTrees")

    println("Scenic score of tree at (1, 2) is ${scenicTreeScore(1, 2, forest)}.")
    println("Scenic score of tree at (3, 2) is ${scenicTreeScore(3, 2, forest)}.")

    val bestScenicTreeScore = forest.indices2D()
        .maxOf { (row, col) -> scenicTreeScore(row, col, forest) }

    println("The best scenic score of a tree is $bestScenicTreeScore")
}

enum class ViewDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}

fun scenicTreeScore(row: Int, col: Int, forest: List<List<Int>>): Int {
    val treeHeight = forest[row][col]

    return getAllViewsFrom(row, col, forest)
        .map { (view, direction) -> getVisibleTrees(view, treeHeight, direction) }
        .reduce(Int::times)
}

fun getVisibleTrees(view: List<Int>, treeHeight: Int, viewDirection: ViewDirection): Int {
    val viewInDirection =
        if (viewDirection == ViewDirection.LEFT_TO_RIGHT) view else view.asReversed()

    val smallerTrees = viewInDirection.takeWhile { it < treeHeight }.count()

    return if (smallerTrees < viewInDirection.size) {
        // There is a high tree blocking the view, but it can be seen as well
        smallerTrees + 1
    } else smallerTrees
}

fun isTreeVisible(row: Int, col: Int, forest: List<List<Int>>): Boolean {
    val treeHeight = forest[row][col]

    return getAllViewsFrom(row, col, forest)
        .any { (view, _) -> isTreeVisible(view, treeHeight) }
}

fun isTreeVisible(view: List<Int>, treeHeight: Int) =
    view.all { it < treeHeight }

fun getAllViewsFrom(
    rowIndex: Int,
    colIndex: Int,
    forest: List<List<Int>>
): List<Pair<List<Int>, ViewDirection>> {
    val row = forest[rowIndex]
    val col = forest.col(colIndex)

    val leftRow = row.viewToLeft(colIndex) to ViewDirection.RIGHT_TO_LEFT
    val rightRow = row.viewToRight(colIndex) to ViewDirection.LEFT_TO_RIGHT
    val upCol = col.viewToLeft(rowIndex) to ViewDirection.RIGHT_TO_LEFT
    val downCol = col.viewToRight(rowIndex) to ViewDirection.LEFT_TO_RIGHT

    return listOf(leftRow, rightRow, upCol, downCol)
}

private fun <T> List<List<T>>.col(col: Int) =
    map { it[col] }.toList()

private fun <T> List<T>.viewToLeft(cutIndex: Int) =
    slice(0 until cutIndex)

private fun <T> List<T>.viewToRight(cutIndex: Int) =
    slice(cutIndex + 1..lastIndex)

private fun List<List<*>>.indices2D() =
    indices
        .flatMap { row ->
            get(0).indices.map { col -> Pair(row, col) }
        }
