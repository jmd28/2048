import kotlin.math.log2
import kotlin.math.roundToInt

//<editor-fold desc="Some housekeeping">
typealias Grid = Array<IntArray>

// function composition operator ..
operator fun <T, R, V> ((T) -> R).rangeTo(other: (R) -> V): ((T) -> V) {
    return {
        other(this(it))
    }
}

fun Int.binaryLog() = log2(this.toDouble()).roundToInt()

//cycle colors based on binary log modulo no. colours
fun colours(x: Int): String = when ((x.binaryLog()-1)%12) {

    0    -> "\u001B[31m" //
    1    -> "\u001B[31;1m"
    2    -> "\u001B[32m"
    3    -> "\u001B[32;1m"
    4    -> "\u001B[33m"
    5    -> "\u001B[33;1m"
    6    -> "\u001B[34m"
    7    -> "\u001B[34;1m"
    8    -> "\u001B[35m"
    9    -> "\u001B[35;1m"
    10   -> "\u001B[36m" //2048
    11   -> "\u001B[36;1m"
    else -> "\u001B[36m"
}
const val ANSI_RESET = "\u001B[0m"
//</editor-fold>

fun main() {
    //setup
    (::addRandomTile..::addRandomTile..::run2048) (emptyGrid())
}

fun run2048(grid: Grid) {

    printgrid(grid)
    if (grid.gameOver()) {
        println("you're bad")
        return
    }

    //get a move
    val move = getMove()
    val oldGrid = grid.map { it.copyOf() }.toTypedArray()
    //make the move
    var newGrid = updategrid(grid, move)

    val noChange = oldGrid contentDeepEquals newGrid
    try {
        // add new tile if move changed gamestate
        if (!noChange)
            addRandomTile(newGrid)
    } catch (e: NoSuchElementException) {
//        grid full
    } finally {
        run2048(newGrid)
    }

}

//initialise an empty game grid
fun emptyGrid(): Grid = Array(4){IntArray(4){0} }

//<editor-fold desc="Game over checks">
fun Grid.gameOver(): Boolean {
    return IntArray(16){ it }
            .map { this.hasNeighbour(it%4, it/4) }
            .none { it }    //if no neighbours same for each piece
}

fun Grid.hasNeighbour(x_pos:Int, y_pos:Int): Boolean {
    val value = this[y_pos][x_pos]
    //ignore zeros
    if (value == 0) return true

    val neighbours = arrayOf(
            this.getOrNull(y_pos)?.getOrNull(x_pos-1),
            this.getOrNull(y_pos)?.getOrNull(x_pos+1),
            this.getOrNull(y_pos-1)?.getOrNull(x_pos),
            this.getOrNull(y_pos+1)?.getOrNull(x_pos)
    )
    return neighbours.any { it == value }
}
//</editor-fold>

//<editor-fold desc="Adding a new number in a random position">
fun generateNumber(): Int = if (Math.random() > 0.10) 2 else 4

fun addRandomTile(grid: Grid):Grid {
    val choice:Int = IntArray(16) { it }
        .filter { grid[it/4][it%4] == 0 }
        .random()
    grid[choice/4][choice%4] = generateNumber()
    return grid
}
//</editor-fold>

//<editor-fold desc="Get a move from stdin">
fun isMoveValid(move:String):Boolean {
    return arrayOf("w", "a", "s", "d").contains(move)
}

fun getMove():String {
    val move = readLine()!!
    return if (isMoveValid(move)) move else getMove()
}
//</editor-fold>

//Make move (merge same, squash in dir of move)
fun updategrid(grid:Array<IntArray>, move: String): Grid =
    when (move) {
        "w" -> updateRowsUp(grid)
        "a" -> updateRowsLeft(grid)
        "s" -> updateRowsDown(grid)
        "d" -> updateRowsRight(grid)
        else -> throw IllegalArgumentException("Expected move of w a s or d")
    }

//Print gamestate
fun printgrid(grid: Array<IntArray>) {
    //clear screen
    print("\u001b[H\u001b[2J")
    //draw score
    println("Score: ${grid.map { it.sum() }.sum()}")
    //draw board
    println("+----+----+----+----+")
    grid.forEach { row ->
        print("|")
        row.forEach {
            val num = "${colours(it)}${(if (it == 0) "" else it).toString().padStart(4)}${ANSI_RESET}"
            print("$num|")
        }
        println("\n+----+----+----+----+")
    }
    //print win message
    if (grid.any { it.contains(2048) }) println("good job you did it")

}

//<editor-fold desc="Updating the grid">
fun mergeRow(row: IntArray): IntArray {
//  eg. 2 0 2 2 -> 4 2 0 0
    for (i in 0..2) {
        val curr = row[i]
        val next = row[i+1]
        if (curr == next) {
            row[i] = curr + next
            row[i + 1] = 0
        }
    }
    return row
}

fun squashRow(row: IntArray): IntArray {
//  eg. 4 0 4 0 -> 4 4 0 0
    val squashed = IntArray(4) { 0 }
    row.filter { it != 0 }.toIntArray().copyInto(squashed)

    return squashed
}

//squash and merge
fun updateRow(row: IntArray): IntArray = (::squashRow..::mergeRow..::squashRow) (row)

fun updateRowsLeft(grid: Grid): Grid = grid.map { updateRow(it) }.toTypedArray()

fun updateRowsRight(grid: Grid): Grid = updateRowsLeft(
        grid.map { it.reversedArray()}.toTypedArray()
    ).map { it.reversedArray() }.toTypedArray()

fun updateRowsUp(grid: Grid): Grid {
    return (::rotateAntiClockwise
            ..::updateRowsLeft
            ..::rotateAntiClockwise
            ..::rotateAntiClockwise
            ..::rotateAntiClockwise) (grid)
}

fun updateRowsDown(grid: Grid): Grid {
    return (::rotateAntiClockwise
            ..::rotateAntiClockwise
            ..::rotateAntiClockwise
            ..::updateRowsLeft
            ..::rotateAntiClockwise) (grid)
}
//</editor-fold>

// used to rotate grid to enable use of same merging and squashing logic each time (in place)
fun rotateAntiClockwise(grid: Grid): Grid {
    for (x in 0 until 2) {
        for (y in x until 3 - x) {
            val temp: Int = grid[x][y]
            grid[x][y] = grid[y][3 - x]
            grid[y][3 - x] = grid[3 - x][3 - y]
            grid[3 - x][3 - y] = grid[3 - y][x]
            grid[3 - y][x] = temp
        }
    }
    return grid
}
