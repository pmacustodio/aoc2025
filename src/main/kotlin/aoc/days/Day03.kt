package aoc.days

/*
 * Day 3: Lobby
 *
 * Each line is a bank of batteries (digits 1-9). Pick exactly k batteries
 * (in order) to form the largest k-digit number.
 *
 * Part 1: k=2. Sum of maximum joltage from each bank.
 * Part 2: k=12. Sum of maximum joltage from each bank.
 *
 * Greedy algorithm: For each of the k positions in the result, determine
 * the valid range of input positions we can pick from. We need to leave
 * enough digits after our choice to fill the remaining positions.
 *
 * Example: picking digit 1 of 12 from a 100-char bank:
 * - We need 11 more digits after, so we can pick from positions 0 to 88
 *   (leaving positions 89-99 = 11 digits)
 * - Take the max in that range, say position 5 has a '9'
 * - For digit 2, we pick from positions 6 to 89, and so on
 */

import java.io.File

fun main() {
    val input = File("src/main/resources/inputs/day03.txt").readLines()

    println("Part 1: ${day03Part1(input)}")
    println("Part 2: ${day03Part2(input)}")
}

fun day03Part1(input: List<String>): Int {
    return input.filter { it.isNotBlank() }.sumOf { maxJoltage(it) }
}

fun maxJoltage(bank: String): Int {
    val digits = bank.map { it.digitToInt() }
    val n = digits.size

    // Precompute suffix maximums: suffixMax[i] = max of digits[i..n-1]
    val suffixMax = IntArray(n)
    suffixMax[n - 1] = digits[n - 1]
    for (i in n - 2 downTo 0) {
        suffixMax[i] = maxOf(digits[i], suffixMax[i + 1])
    }

    // For each first position, compute best pair and track maximum
    var maxJolt = 0
    for (i in 0 until n - 1) {
        val jolt = digits[i] * 10 + suffixMax[i + 1]
        maxJolt = maxOf(maxJolt, jolt)
    }

    return maxJolt
}

fun day03Part2(input: List<String>): Long {
    return input.filter { it.isNotBlank() }.sumOf { maxJoltageK(it, 12) }
}

fun maxJoltageK(bank: String, k: Int): Long {
    val digits = bank.map { it.digitToInt() }
    val n = digits.size

    // Greedy: for each of k positions, pick the largest digit that leaves enough remaining
    val result = StringBuilder()
    var start = 0

    for (remaining in k downTo 1) {
        // We need (remaining - 1) more digits after this one
        // So we can pick from positions [start, n - remaining]
        val end = n - remaining

        // Find the position of max digit in [start, end]
        var maxDigit = -1
        var maxPos = start
        for (i in start..end) {
            if (digits[i] > maxDigit) {
                maxDigit = digits[i]
                maxPos = i
            }
        }

        result.append(maxDigit)
        start = maxPos + 1
    }

    return result.toString().toLong()
}
