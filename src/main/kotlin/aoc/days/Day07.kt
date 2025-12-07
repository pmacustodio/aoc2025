package aoc.days

/*
 * Day 7: Laboratories
 *
 * A tachyon beam enters at 'S' and moves downward.
 * When it hits a splitter ('^'), it stops and two new beams
 * are emitted from the left and right of the splitter.
 *
 * Part 1: Count how many times the beam is split.
 */

import java.io.File

fun main() {
    // Verify with example first (expected: 21)
    val example = listOf(
        ".......S.......",
        "...............",
        ".......^.......",
        "...............",
        "......^.^......",
        "...............",
        ".....^.^.^.....",
        "...............",
        "....^.^...^....",
        "...............",
        "...^.^...^.^...",
        "...............",
        "..^...^.....^..",
        "...............",
        ".^.^.^.^.^...^.",
        "..............."
    )
    val exampleResult = day07Part1(example)
    println("Example: $exampleResult (expected 21)")

    val input = File("src/main/resources/inputs/day07.txt").readLines()

    println("Part 1: ${day07Part1(input)}")
    println("Part 2: ${day07Part2(input)}")
}

fun day07Part1(input: List<String>): Long {
    val grid = input.filter { it.isNotBlank() }
    if (grid.isEmpty()) return 0

    val height = grid.size
    val width = grid.maxOf { it.length }

    // Find the starting position 'S'
    var startCol = -1
    for (line in grid) {
        val col = line.indexOf('S')
        if (col >= 0) {
            startCol = col
            break
        }
    }

    if (startCol == -1) return 0

    // Track active beams by column position
    // Start with one beam at the S position
    var activeBeams = mutableSetOf(startCol)
    var splitCount = 0L

    // Process row by row, starting from the row after S
    for (row in 1 until height) {
        val line = grid[row]
        val newBeams = mutableSetOf<Int>()

        for (col in activeBeams) {
            val char = if (col < line.length) line[col] else '.'

            when (char) {
                '^' -> {
                    // Splitter: beam stops, two new beams emitted left and right
                    splitCount++
                    val leftCol = col - 1
                    val rightCol = col + 1
                    if (leftCol >= 0) newBeams.add(leftCol)
                    if (rightCol < width) newBeams.add(rightCol)
                }
                '.', ' ' -> {
                    // Empty space: beam continues downward
                    newBeams.add(col)
                }
                // Any other character: beam continues
                else -> newBeams.add(col)
            }
        }

        activeBeams = newBeams

        // If no beams left, stop
        if (activeBeams.isEmpty()) break
    }

    return splitCount
}

@Suppress("UNUSED_PARAMETER")
fun day07Part2(input: List<String>): Long {
    // Part 2 not yet revealed
    return 0
}
