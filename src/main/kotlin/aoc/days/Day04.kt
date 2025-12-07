package aoc.days

/*
 * Day 4: Printing Department
 *
 * Grid of paper rolls (@). A roll is accessible by forklift if it has
 * fewer than 4 adjacent rolls (8 directions: orthogonal + diagonal).
 *
 * Part 1: Count how many rolls are currently accessible.
 *
 * Part 2: Simulate removing accessible rolls iteratively. Each removal
 * may make new rolls accessible. Count total rolls removed until none
 * are accessible.
 *
 * Example grid:
 *   ..@@.
 *   @@@.@    The center @ has 4 neighbors, so NOT accessible.
 *   ..@@.    Corner/edge rolls with < 4 neighbors ARE accessible.
 *
 * Part 2 simulation: Remove all accessible rolls simultaneously each round.
 * Removing rolls may expose previously blocked rolls (their neighbor count drops).
 * Repeat until no rolls have < 4 neighbors.
 */

import java.io.File

fun main() {
    val input = File("src/main/resources/inputs/day04.txt").readLines()

    println("Part 1: ${day04Part1(input)}")
    println("Part 2: ${day04Part2(input)}")
}

fun day04Part1(input: List<String>): Int {
    val grid = input.filter { it.isNotBlank() }
    val rows = grid.size
    val cols = grid[0].length

    var accessible = 0

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if (grid[r][c] == '@') {
                val neighbors = countAdjacentRolls(grid, r, c, rows, cols)
                if (neighbors < 4) {
                    accessible++
                }
            }
        }
    }

    return accessible
}

fun countAdjacentRolls(grid: List<String>, r: Int, c: Int, rows: Int, cols: Int): Int {
    var count = 0
    for (dr in -1..1) {
        for (dc in -1..1) {
            if (dr == 0 && dc == 0) continue
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until rows && nc in 0 until cols && grid[nr][nc] == '@') {
                count++
            }
        }
    }
    return count
}

fun countAdjacentRollsMutable(grid: Array<CharArray>, r: Int, c: Int, rows: Int, cols: Int): Int {
    var count = 0
    for (dr in -1..1) {
        for (dc in -1..1) {
            if (dr == 0 && dc == 0) continue
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until rows && nc in 0 until cols && grid[nr][nc] == '@') {
                count++
            }
        }
    }
    return count
}

fun day04Part2(input: List<String>): Int {
    val lines = input.filter { it.isNotBlank() }
    val rows = lines.size
    val cols = lines[0].length
    val grid = Array(rows) { r -> lines[r].toCharArray() }

    var totalRemoved = 0

    while (true) {
        // Find all currently accessible rolls
        val toRemove = mutableListOf<Pair<Int, Int>>()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (grid[r][c] == '@') {
                    val neighbors = countAdjacentRollsMutable(grid, r, c, rows, cols)
                    if (neighbors < 4) {
                        toRemove.add(r to c)
                    }
                }
            }
        }

        if (toRemove.isEmpty()) break

        // Remove all accessible rolls
        for ((r, c) in toRemove) {
            grid[r][c] = '.'
        }

        totalRemoved += toRemove.size
    }

    return totalRemoved
}
