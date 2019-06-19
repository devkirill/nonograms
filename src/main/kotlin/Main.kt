import java.util.*

// https://habr.com/ru/post/418069/

val input = Scanner(System.`in`)

fun main(args: Array<String>) {
    input.nextInt()
    val len = (0 until input.nextInt()).map { input.nextInt() }.toList()
    val groupSize = len.size
    val s = input.next()
    val cellSize = s.length

    val black = s.map { it == 'X' }.toMutableList()
    val white = s.map { it == '.' }.toMutableList()
    val canBlack = (0 until black.size).map { false }.toMutableList()
    val canWhite = (0 until white.size + 1).map { false }.toMutableList()
    fun canInsertBlack(cell: Int, length: Int): Boolean =
        cell + length <= cellSize && (cell until cell + length).none { white[it] }

    fun canInsertWhite(cell: Int): Boolean =
        cell >= cellSize || !black[cell]

    val calc = mutableMapOf<Int, MutableMap<Int, Boolean>>()

    fun epicWin(group: Int, cell: Int): Boolean {
        if (cell >= cellSize)
            return group == groupSize

        val res = calc[group]?.get(cell)
        if (res != null)
            return res

        var win = false
        val canInsert = group < groupSize
                && canInsertBlack(cell, len[group])
                && canInsertWhite(cell + len[group])

        if (canInsert
            && epicWin(group + 1, cell + len[group] + 1)
        ) {
                win = true
                (cell until cell + len[group]).forEach { canBlack[it] = true }
                canWhite[cell + len[group]] = true
        }

        if (canInsertWhite(cell) && epicWin(group, cell + 1)) {
                win = true
                canWhite[cell] = true
        }

        calc.computeIfAbsent(group) { mutableMapOf() }[cell] = win
        return win
    }

    if (!epicWin(0, 0)) {
        println("Impossible")
    }

    println((0 until cellSize).map {
        when {
            canBlack[it] && canWhite[it] -> '?'
            canBlack[it] -> 'X'
            canWhite[it] -> '.'
            else -> ' '
        }
    }.joinToString(""))
}