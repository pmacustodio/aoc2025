package aoc.days

/*
 * Day 6: Trash Compactor
 *
 * Parse a "worksheet" where math problems are arranged in columns.
 * Problems are separated by columns of all spaces.
 * The operator (+ or *) is on the last row.
 *
 * Part 1: Numbers are read row-wise (horizontally).
 *   Each row within a problem block contains one number.
 *
 * Part 2: Numbers are read column-wise (vertically, top-to-bottom).
 *   Each column's digits (ignoring spaces) concatenate to form one number.
 */

import java.io.File

fun main() {
    val input = File("src/main/resources/inputs/day06.txt").readLines()

    println("Part 1: ${day06Part1(input)}")
    println("Part 2: ${day06Part2(input)}")
}

fun day06Part1(input: List<String>): Long {
    val lines = input.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return 0

    // Pad all lines to same length
    val maxLen = lines.maxOf { it.length }
    val padded = lines.map { it.padEnd(maxLen) }

    val operatorRow = padded.last()
    val numberRows = padded.dropLast(1)

    // Find separator columns (all spaces in that column)
    val separatorCols = mutableSetOf<Int>()
    for (col in 0 until maxLen) {
        if (padded.all { col >= it.length || it[col] == ' ' }) {
            separatorCols.add(col)
        }
    }

    // Find problem column ranges (non-separator columns grouped together)
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

    // Solve each problem
    var total = 0L
    for (range in problems) {
        // Extract operator from last row
        val opStr = operatorRow.substring(range.first, minOf(range.last + 1, operatorRow.length)).trim()
        val operator = if ('*' in opStr) '*' else '+'

        // Extract numbers from each row
        val numbers = mutableListOf<Long>()
        for (row in numberRows) {
            val slice = if (range.last + 1 <= row.length) {
                row.substring(range.first, range.last + 1)
            } else if (range.first < row.length) {
                row.substring(range.first)
            } else {
                ""
            }
            val num = slice.trim()
            if (num.isNotEmpty() && num.all { it.isDigit() }) {
                numbers.add(num.toLong())
            }
        }

        // Compute result
        val result = if (operator == '*') {
            numbers.fold(1L) { acc, n -> acc * n }
        } else {
            numbers.sum()
        }

        total += result
    }

    return total
}

fun day06Part2(input: List<String>): Long {
    val lines = input.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return 0

    // Pad all lines to same length
    val maxLen = lines.maxOf { it.length }
    val padded = lines.map { it.padEnd(maxLen) }

    val operatorRow = padded.last()
    val numberRows = padded.dropLast(1)

    // Find separator columns (all spaces in that column)
    val separatorCols = mutableSetOf<Int>()
    for (col in 0 until maxLen) {
        if (padded.all { col >= it.length || it[col] == ' ' }) {
            separatorCols.add(col)
        }
    }

    // Find problem column ranges (non-separator columns grouped together)
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

    // Solve each problem
    var total = 0L
    for (range in problems) {
        // Extract operator from last row
        val opStr = operatorRow.substring(range.first, minOf(range.last + 1, operatorRow.length)).trim()
        val operator = if ('*' in opStr) '*' else '+'

        // Extract numbers column-wise: each column forms one number (digits top-to-bottom)
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

        // Compute result
        val result = if (operator == '*') {
            numbers.fold(1L) { acc, n -> acc * n }
        } else {
            numbers.sum()
        }

        total += result
    }

    return total
}
