package aoc.days

/*
 * Day 2: Gift Shop
 *
 * Find "invalid" product IDs within given ranges.
 *
 * Part 1: Invalid = pattern repeated exactly twice (55, 6464, 123123).
 * Part 2: Invalid = pattern repeated at least twice (55, 111, 123123123).
 *
 * Key insight: Instead of checking every number in a range, we generate
 * repeated-pattern numbers directly using a "repetition factor".
 *
 * Example: To generate all 6-digit numbers where a 2-digit pattern repeats 3 times:
 * - pattern "ab" repeated 3 times = ababab
 * - ababab = ab * 10000 + ab * 100 + ab * 1 = ab * 10101
 * - So factor = 10101, and we iterate ab from 10 to 99
 * - ab=12 gives 12 * 10101 = 121212
 *
 * For each range, we calculate which pattern values produce numbers within bounds,
 * avoiding unnecessary iterations.
 */

import java.io.File

fun main() {
    val input = File("src/main/resources/inputs/day02.txt").readText().trim()

    println("Part 1: ${day02Part1(input)}")
    println("Part 2: ${day02Part2(input)}")
}

fun day02Part1(input: String): Long {
    val ranges = parseRanges(input)

    // Use a set to avoid counting duplicates if ranges overlap
    val invalidIds = mutableSetOf<Long>()

    for ((start, end) in ranges) {
        invalidIds.addAll(findDoubledPatternsInRange(start, end))
    }

    return invalidIds.sum()
}

fun parseRanges(input: String): List<Pair<Long, Long>> {
    return input.split(",")
        .filter { it.isNotBlank() }
        .map { range ->
            val (start, end) = range.trim().split("-").map { it.toLong() }
            start to end
        }
}

fun findDoubledPatternsInRange(start: Long, end: Long): List<Long> {
    val patterns = mutableListOf<Long>()

    // Doubled patterns have even digit count. Start halfLength based on start value.
    // A number with d digits has halfLength = ceil(d/2) for the smallest valid doubled pattern.
    val startDigits = digitCount(start)
    var halfLength = (startDigits + 1) / 2 // ceil(startDigits / 2)

    while (true) {
        val multiplier = pow10(halfLength)

        // Range of valid "half" values (no leading zeros)
        val minHalf = if (halfLength == 1) 1L else pow10(halfLength - 1)
        val maxHalf = multiplier - 1

        // doubled = half * multiplier + half = half * (multiplier + 1)
        val factor = multiplier + 1

        // Smallest and largest doubled numbers with this half-length
        val smallestDoubled = minHalf * factor
        val largestDoubled = maxHalf * factor

        // If smallest is beyond our range end, we're done
        if (smallestDoubled > end) break

        // If largest is at least at our range start, there might be matches
        if (largestDoubled >= start) {
            // Calculate the half-value bounds that could produce values in [start, end]
            val halfStart = maxOf(minHalf, (start + factor - 1) / factor) // ceil division
            val halfEnd = minOf(maxHalf, end / factor)

            for (half in halfStart..halfEnd) {
                val doubled = half * factor
                if (doubled in start..end) {
                    patterns.add(doubled)
                }
            }
        }

        halfLength++
    }

    return patterns
}

fun digitCount(n: Long): Int {
    if (n == 0L) return 1
    var count = 0
    var num = n
    while (num > 0) {
        count++
        num /= 10
    }
    return count
}

fun pow10(n: Int): Long {
    var result = 1L
    repeat(n) { result *= 10 }
    return result
}

fun day02Part2(input: String): Long {
    val ranges = parseRanges(input)

    val invalidIds = mutableSetOf<Long>()

    for ((start, end) in ranges) {
        invalidIds.addAll(findRepeatedPatternsInRange(start, end))
    }

    return invalidIds.sum()
}

fun findRepeatedPatternsInRange(start: Long, end: Long): Set<Long> {
    val patterns = mutableSetOf<Long>()

    val startDigits = digitCount(start)
    val endDigits = digitCount(end)

    // For each total digit count that appears in our range
    for (totalDigits in startDigits..endDigits) {
        // Determine the actual range bounds for numbers with exactly totalDigits digits
        val minWithDigits = if (totalDigits == 1) 1L else pow10(totalDigits - 1)
        val maxWithDigits = pow10(totalDigits) - 1
        val rangeStart = maxOf(start, minWithDigits)
        val rangeEnd = minOf(end, maxWithDigits)

        if (rangeStart > rangeEnd) continue

        // For each pattern length that divides totalDigits and repeats >= 2 times
        for (patternLen in 1..totalDigits / 2) {
            if (totalDigits % patternLen != 0) continue

            val repetitions = totalDigits / patternLen
            if (repetitions < 2) continue

            // Generate all valid patterns of this length
            val minPattern = if (patternLen == 1) 1L else pow10(patternLen - 1)
            val maxPattern = pow10(patternLen) - 1

            // Calculate the multiplier for repeating a pattern
            // e.g., for patternLen=2, reps=3: pattern * (10000 + 100 + 1) = pattern * 10101
            val factor = computeRepetitionFactor(patternLen, repetitions)

            // Find pattern range that produces values in [rangeStart, rangeEnd]
            val patternStart = maxOf(minPattern, (rangeStart + factor - 1) / factor)
            val patternEnd = minOf(maxPattern, rangeEnd / factor)

            for (pattern in patternStart..patternEnd) {
                val repeated = pattern * factor
                if (repeated in rangeStart..rangeEnd) {
                    patterns.add(repeated)
                }
            }
        }
    }

    return patterns
}

fun computeRepetitionFactor(patternLen: Int, repetitions: Int): Long {
    // For pattern "ab" repeated 3 times: ababab = ab * 10101
    // factor = 10^(2*patternLen) + 10^patternLen + 1 for 3 reps
    var factor = 0L
    val base = pow10(patternLen)
    var multiplier = 1L
    repeat(repetitions) {
        factor += multiplier
        multiplier *= base
    }
    return factor
}
