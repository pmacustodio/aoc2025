package aoc.days

/*
 * Day 1: Secret Entrance
 *
 * A dial (0-99) starts at position 50. We process rotations (L/R + distance).
 * - L = rotate left (toward lower numbers, wrapping 0→99)
 * - R = rotate right (toward higher numbers, wrapping 99→0)
 *
 * Part 1: Count how many rotations END with the dial pointing at 0.
 * Simple modular arithmetic: track position and check if it equals 0 after each move.
 *
 * Part 2: Count how many times the dial PASSES THROUGH 0 (including landing on it).
 * Key insight: calculate crossings mathematically rather than simulating each click.
 *
 * For Left rotation from position P with distance D:
 *   - We hit 0 at step P, then every 100 steps after (P, P+100, P+200, ...)
 *   - Count = (D - P + 100) / 100  (integer division)
 *   - Special case when P=0: count = D / 100
 *
 * For Right rotation from position P with distance D:
 *   - We hit 0 at step (100-P), then every 100 steps after
 *   - Count = (D + P) / 100  (integer division)
 *
 * Example: From 50, R1000 → (1000 + 50) / 100 = 10 crossings
 */

import java.io.File

fun main() {
    val input = File("src/main/resources/inputs/day01.txt").readLines()
    
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}

fun part1(input: List<String>): Int {
    var position = 50
    var zeroCount = 0

    for (line in input) {
        if (line.isBlank()) continue

        val direction = line[0]
        val distance = line.substring(1).toInt()

        position = when (direction) {
            'L' -> (position - distance).mod(100)
            'R' -> (position + distance).mod(100)
            else -> position
        }

        if (position == 0) zeroCount++
    }

    return zeroCount
}

fun part2(input: List<String>): Int {
    var position = 50
    var zeroCount = 0

    for (line in input) {
        if (line.isBlank()) continue

        val direction = line[0]
        val distance = line.substring(1).toInt()

        zeroCount += when (direction) {
            'L' -> if (position == 0) distance / 100 else (distance - position + 100) / 100
            'R' -> (distance + position) / 100
            else -> 0
        }

        position = when (direction) {
            'L' -> (position - distance).mod(100)
            'R' -> (position + distance).mod(100)
            else -> position
        }
    }

    return zeroCount
}
