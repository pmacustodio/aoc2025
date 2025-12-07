package aoc.adventure.dungeons

/**
 * Day 6: Trash Compactor - Math Worksheet Puzzle
 *
 * Visualizes solving column-based math problems.
 */
class Day06DungeonPuzzle : DungeonPuzzle {

    override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val lines = input.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return 0

        val maxLen = lines.maxOf { it.length }
        val padded = lines.map { it.padEnd(maxLen) }
        val operatorRow = padded.last()
        val numberRows = padded.dropLast(1)

        val problems = findProblems(padded, maxLen)

        onStep(AnimationStep(
            "Found ${problems.size} math problems on worksheet",
            listOf("Reading numbers row-wise (horizontally)"),
            0
        ))

        var total = 0L
        for ((index, range) in problems.withIndex()) {
            val opStr = operatorRow.substring(range.first, minOf(range.last + 1, operatorRow.length)).trim()
            val operator = if ('*' in opStr) '*' else '+'

            val numbers = mutableListOf<Long>()
            for (row in numberRows) {
                val slice = if (range.last + 1 <= row.length) {
                    row.substring(range.first, range.last + 1)
                } else if (range.first < row.length) {
                    row.substring(range.first)
                } else ""
                val num = slice.trim()
                if (num.isNotEmpty() && num.all { it.isDigit() }) {
                    numbers.add(num.toLong())
                }
            }

            val result = if (operator == '*') {
                numbers.fold(1L) { acc, n -> acc * n }
            } else {
                numbers.sum()
            }

            total += result

            if (index < 10 || index % 20 == 0) {
                val expr = numbers.joinToString(" $operator ")
                onStep(AnimationStep(
                    "Problem ${index + 1}: $expr = $result",
                    renderWorksheet(lines, index, total),
                    total
                ))
            }
        }

        onStep(AnimationStep(
            "Grand total (Part 1): $total",
            listOf(
                "╔════════════════════════════════════╗",
                "║    WORKSHEET COMPLETE              ║",
                "╠════════════════════════════════════╣",
                "║  Problems: ${problems.size}".padEnd(37) + "║",
                "║  Grand Total: $total".padEnd(37) + "║",
                "╚════════════════════════════════════╝"
            ),
            total
        ))

        return total
    }

    override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val lines = input.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return 0

        val maxLen = lines.maxOf { it.length }
        val padded = lines.map { it.padEnd(maxLen) }
        val operatorRow = padded.last()
        val numberRows = padded.dropLast(1)

        val problems = findProblems(padded, maxLen)

        onStep(AnimationStep(
            "Part 2: Reading numbers column-wise (vertically)",
            listOf("Each column's digits form one number"),
            0
        ))

        var total = 0L
        for ((index, range) in problems.withIndex()) {
            val opStr = operatorRow.substring(range.first, minOf(range.last + 1, operatorRow.length)).trim()
            val operator = if ('*' in opStr) '*' else '+'

            val numbers = mutableListOf<Long>()
            for (col in range) {
                val digits = StringBuilder()
                for (row in numberRows) {
                    val ch = if (col < row.length) row[col] else ' '
                    if (ch.isDigit()) {
                        digits.append(ch)
                    }
                }
                if (digits.isNotEmpty()) {
                    numbers.add(digits.toString().toLong())
                }
            }

            val result = if (operator == '*') {
                numbers.fold(1L) { acc, n -> acc * n }
            } else {
                numbers.sum()
            }

            total += result

            if (index < 10 || index % 20 == 0) {
                val expr = numbers.joinToString(" $operator ")
                onStep(AnimationStep(
                    "Problem ${index + 1} (column-wise): $expr = $result",
                    listOf("Running total: $total"),
                    total
                ))
            }
        }

        onStep(AnimationStep(
            "Grand total (Part 2): $total",
            listOf(
                "╔════════════════════════════════════╗",
                "║    COLUMN-WISE COMPLETE            ║",
                "╠════════════════════════════════════╣",
                "║  Grand Total: $total".padEnd(37) + "║",
                "╚════════════════════════════════════╝"
            ),
            total
        ))

        return total
    }

    private fun findProblems(padded: List<String>, maxLen: Int): List<IntRange> {
        val separatorCols = mutableSetOf<Int>()
        for (col in 0 until maxLen) {
            if (padded.all { col >= it.length || it[col] == ' ' }) {
                separatorCols.add(col)
            }
        }

        val problems = mutableListOf<IntRange>()
        var start = -1
        for (col in 0 until maxLen) {
            if (col !in separatorCols) {
                if (start == -1) start = col
            } else {
                if (start != -1) {
                    problems.add(start until col)
                    start = -1
                }
            }
        }
        if (start != -1) {
            problems.add(start until maxLen)
        }

        return problems
    }

    private fun renderWorksheet(lines: List<String>, currentProblem: Int, total: Long): List<String> {
        val display = mutableListOf<String>()
        display.add("╔══════════════════════════════════════════╗")
        display.add("║  📋 MATH WORKSHEET                       ║")
        display.add("╠══════════════════════════════════════════╣")

        // Show first few lines of worksheet
        for (line in lines.take(5)) {
            val truncated = line.take(40)
            display.add("║  $truncated".padEnd(43) + "║")
        }
        if (lines.size > 5) {
            display.add("║  ...                                     ║")
        }

        display.add("╠══════════════════════════════════════════╣")
        display.add("║  Current problem: ${currentProblem + 1}".padEnd(43) + "║")
        display.add("║  Running total: $total".padEnd(43) + "║")
        display.add("╚══════════════════════════════════════════╝")

        return display
    }
}
