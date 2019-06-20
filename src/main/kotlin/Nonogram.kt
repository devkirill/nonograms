import com.google.gson.Gson
import java.io.File

fun main(args: Array<String>) {
//    val c = SimpleString(listOf(8, 3), 15, 1)
//    println(c.epic())
//    return

    val gson = Gson()

    val n = gson.fromJson(getResourceAsText(args.first()), Nonogram::class.java) as Nonogram

    var m = Matrix(n)

//    val ss = m.cols[7]
//
//    println(ss)
//    println(ss.epic())

    do {
        val before = m.toString()
        m = m.epic()
        println(m)
        println()
        val after = m.toString()
    } while (before != after)
}

fun getResourceAsText(path: String): String {
    return if (path.startsWith("classpath:"))
        object {}.javaClass.getResource(path.substring(10)).readText()
    else
        File(path).readText()
}

data class Nonogram(val colors: List<String>, val rows: List<List<Int>>, val cols: List<List<Int>>)

data class Matrix(val nonogram: Nonogram, val cols: List<SimpleString>, val rows: List<SimpleString>) {
    constructor(nonogram: Nonogram) : this(
        nonogram,
        nonogram.cols.map { SimpleString(it, nonogram.rows.size, nonogram.colors.size) },
        nonogram.rows.map { SimpleString(it, nonogram.cols.size, nonogram.colors.size) })

    fun epic(): Matrix {
        val newCols = cols.map { it.epic() }

        println()

        val newRows = rows.mapIndexed { r, sr ->
            val paint = sr.painted
            newCols.forEachIndexed { c, sc ->
                sc.painted.forEachIndexed { color, l ->
                    if (l[r])
                        paint[color][c] = true
                }
            }
            SimpleString(sr.groups, paint)
        }
            .map { it.epic() }
        val newNewCols = newCols.mapIndexed { c, sc ->
            val paint = sc.painted
            newRows.forEachIndexed { r, sr ->
                sr.painted.forEachIndexed { color, l ->
                    if (l[c])
                        paint[color][r] = true
                }
            }
            SimpleString(sc.groups, paint)
        }

        return Matrix(nonogram, newNewCols, newRows)
    }

    override fun toString(): String = rows.joinToString("\n")
}

data class SimpleString(
    val groups: List<Int>,
    val painted: List<MutableList<Boolean>>
) {
    constructor(groups: List<Int>, size: Int, colors: Int) : this(
        groups,
        (0..colors).map { (0 until size).map { false }.toMutableList() }
    )

    private val cellCount = painted.map { it.size }.max()!!
    private val groupCount = groups.size
    private val colorCount = painted.size - 1

    private fun color(group: Int) = groups[group] % colorCount + 1
    private fun len(group: Int) = groups[group] / colorCount

    fun epic(): SimpleString {
        val from = this.toString()

        val canPainted: List<MutableList<Boolean>> =
            (0..colorCount).map { (0 until cellCount).map { false }.toMutableList() }

        fun canInsertColor(color: Int, cell: Int, length: Int): Boolean =
            cell + length <= cellCount && (cell until cell + length)
                .all { painted.filterIndexed { i, l -> i != color }.none { l -> l[it] } }

        fun canInsertWhite(cell: Int): Boolean =
            cell >= cellCount || painted.drop(1).none { it[cell] }

        fun epicWin(group: Int = 0, cell: Int = 0): Boolean {
            if (cell >= cellCount)
                return group == groupCount

//            val res = calc[group]?.get(cell)
//            if (res != null)
//                return res

            var win = false
            if (group < groupCount && canInsertColor(color(group), cell, len(group))) {
                if (group + 1 < groupCount && color(group) != color(group + 1)) {
                    if (epicWin(group + 1, cell + len(group))) {
                        win = true
                        val p = canPainted[color(group)]
                        (cell until cell + len(group)).forEach { p[it] = true }
                    }
                } else {
                    if (canInsertWhite(cell + len(group)) && epicWin(group + 1, cell + len(group) + 1)) {
                        win = true
                        val p = canPainted[color(group)]
                        (cell until cell + len(group)).forEach { p[it] = true }
                        if (cell + len(group) < cellCount)
                            canPainted[0][cell + len(group)] = true
                    }
                }
            }

            if (canInsertWhite(cell) && epicWin(group, cell + 1)) {
                win = true
                canPainted[0][cell] = true
            }

            return win
        }

        epicWin(0, 0)

        (0 until cellCount)
            .forEach { ind ->
                val l = canPainted.mapIndexed { it, l -> it to l[ind] }.filter { it.second }
                if (l.size == 1)
                    painted[l[0].first][ind] = true
            }

        val res = SimpleString(
            groups,
            painted
        )
        println("$from -> $res")

        return res
    }

    override fun toString(): String {
        return (0 until painted[0].size)
            .map { i ->
                val l = painted.mapIndexed { it, l -> it to l[i] }.filter { it.second }
                when {
                    l.size > 1 || l.isEmpty() -> '?'
                    l[0].first == 0 -> ' '
                    else -> 'A' + (l[0].first - 1)
                }
            }
            .joinToString("")
    }
}

// 8 3 ???XXXXX???????