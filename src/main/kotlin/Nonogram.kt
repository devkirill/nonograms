import com.google.gson.Gson
import java.awt.image.BufferedImage
import java.io.File

fun main(args: Array<String>) {
//    val c = SimpleString(listOf(8, 3), 15, 1)
//    println(c.epic())
//    return

    val gson = Gson()

    val n = gson.fromJson(getResourceAsText(args.first()), Nonogram::class.java) as Nonogram

    n.init()

    var img: BufferedImage

    var idx = 0

//    var m = Matrix(n) { matrix ->
//        img = BufferedImage(matrix.cols.size * 20 + 1, matrix.rows.size * 20 + 1, BufferedImage.TYPE_INT_RGB)
//        val g = img.createGraphics()
//        for (y in 0 until matrix.rows.size)
//            for (x in 0 until matrix.cols.size) {
//                val col  = matrix.cols[x]
//                val c = col.painted.mapIndexed { it, l -> it to l[y] }.filter { it.second }
//                g.color = Color.GRAY
//                when {
//                    c.isEmpty() -> {
//                        g.background = Color.MAGENTA
//                        g.color = g.background
//                        g.fillRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                        g.drawRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                    }
//                    c.size > 1 -> {
//                        g.background = Color.WHITE
//                        g.color = g.background
//                        g.fillRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                        g.drawRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                    }
//                    c[0].first == 0 -> {
//                        g.background = Color.WHITE
//                        g.color = g.background
//                        g.fillRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                        g.drawRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                        g.color = Color.BLACK
//                        g.drawLine(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                        g.drawLine(x * 20, (y + 1) * 20, (x + 1) * 20,y * 20)
//                    }
//                    else -> {
//                        g.background = Color(matrix.nonogram.colors[c[0].first - 1].toInt(16))
//                        g.color = g.background
//                        g.fillRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                        g.drawRect(x * 20, y * 20, (x + 1) * 20, (y + 1) * 20)
//                    }
//                }
//            }
//        ImageIO.write(img, "png", File("${idx++}.png"))
//    }

//    val ss = m.cols[7]
//
//    println(ss)
//    println(ss.epic())

    do {
        val before = n.toString()
        n.solve()
        println(n)
        println()
        val after = n.toString()
    } while (before != after)
}

fun getResourceAsText(path: String): String {
    return if (path.startsWith("classpath:"))
        object {}.javaClass.getResource(path.substring(10)).readText()
    else
        File(path).readText()
}

data class Nonogram(
    val colors: List<String>,
    val rows: List<List<Int>>,
    val cols: List<List<Int>>
) {
    lateinit var data: List<List<MutableList<Boolean>>>
    var colorCount: Int = 0

    fun init() {
        data = cols.map { rows.map { (colors.map { true } + true).toMutableList() } }
        colorCount = colors.size
    }

    private fun color(c: Int) = c % colorCount + 1
    private fun len(c: Int) = c / colorCount

    fun solveCol(col: Int) {
        val cellCount = rows.size
        val groups = cols[col]
        val groupCount = groups.size

        val canPainted: List<MutableList<Boolean>> =
            (0..colorCount).map { (0 until cellCount).map { false }.toMutableList() }

        fun canInsertColor(color: Int, cell: Int, length: Int): Boolean =
            cell + length <= cellCount && (cell until cell + length)
                .all { data[col][it][color] }

        fun canInsertWhite(cell: Int): Boolean =
            cell >= cellCount || data[col][cell][0]

        fun epicWin(group: Int = 0, cell: Int = 0): Boolean {
            if (cell >= cellCount)
                return group == groupCount

            var win = false
            if (group < groupCount) {
                val color = color(groups[group])
                val len = len(groups[group])
                if (canInsertColor(color, cell, len)) {
                    if (group + 1 < groupCount && color != color(groups[group + 1])) {
                        if (epicWin(group + 1, cell + len)) {
                            win = true
                            val p = canPainted[color]
                            (cell until cell + len).forEach { p[it] = true }
                        }
                    } else {
                        if (canInsertWhite(cell + len) && epicWin(group + 1, cell + len + 1)) {
                            win = true
                            val p = canPainted[color]
                            (cell until cell + len).forEach { p[it] = true }
                            if (cell + len < cellCount)
                                canPainted[0][cell + len] = true
                        }
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

        for (row in (0 until cellCount))
            for (color in (0..colorCount))
                data[col][row][color] = data[col][row][color] && canPainted[color][row]
    }

    fun solveRow(row: Int) {
        val cellCount = cols.size
        val groups = rows[row]
        val groupCount = groups.size

        val canPainted: List<MutableList<Boolean>> =
            (0..colorCount).map { (0 until cellCount).map { false }.toMutableList() }

        fun canInsertColor(color: Int, cell: Int, length: Int): Boolean =
            cell + length <= cellCount && (cell until cell + length)
                .all { data[it][row][color] }

        fun canInsertWhite(cell: Int): Boolean =
            cell >= cellCount || data[cell][row][0]

        fun epicWin(group: Int = 0, cell: Int = 0): Boolean {
            if (cell >= cellCount)
                return group == groupCount

            var win = false
            if (group < groupCount) {
                val color = color(groups[group])
                val len = len(groups[group])
                if (canInsertColor(color, cell, len)) {
                    if (group + 1 < groupCount && color != color(groups[group + 1])) {
                        if (epicWin(group + 1, cell + len)) {
                            win = true
                            val p = canPainted[color]
                            (cell until cell + len).forEach { p[it] = true }
                        }
                    } else {
                        if (canInsertWhite(cell + len) && epicWin(group + 1, cell + len + 1)) {
                            win = true
                            val p = canPainted[color]
                            (cell until cell + len).forEach { p[it] = true }
                            if (cell + len < cellCount)
                                canPainted[0][cell + len] = true
                        }
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

        for (col in (0 until cellCount))
            for (color in (0..colorCount))
                data[col][row][color] = data[col][row][color] && canPainted[color][col]
    }

    fun solve() {
        for (i in 0 until cols.size)
            solveCol(i)
        for (i in 0 until rows.size)
            solveRow(i)
    }

    override fun toString(): String =
        rows.mapIndexed { row, _ ->
            cols.mapIndexed { col, _ ->
                val colorList = data[col][row]
                val l = (0 until colorList.size).filter { colorList[it] }
                when {
                    l.isEmpty() -> '-'
                    l.size > 1 -> '?'
                    l[0] == 0 -> ' '
                    else -> 'A' + (l[0] - 1)
                }
            }.joinToString("")
        }.joinToString("\n")
}