package aoc.days

/*
 * Day 5: Cafeteria
 *
 * Given a list of "fresh" ID ranges and a list of ingredient IDs.
 *
 * Part 1: Count how many ingredient IDs fall within any fresh range.
 * Part 2: Count total unique IDs covered by all ranges (ignoring ingredient list).
 *
 * Algorithm: Merge overlapping ranges, then query.
 *
 * Step 1 - Merge ranges:
 *   Input:  [3-5], [10-14], [12-18], [16-20]
 *   Sort:   [3-5], [10-14], [12-18], [16-20]
 *   Merge:  [3-5], [10-20]  (10-14, 12-18, 16-20 overlap)
 *
 * Part 1 - Binary search for each ID:
 *   For ID=17, find the rightmost range whose start <= 17.
 *   That's [10-20]. Check if 17 <= 20. Yes, so ID=17 is fresh.
 *
 * Part 2 - Sum range sizes:
 *   [3-5] has 3 IDs, [10-20] has 11 IDs → total = 14
 *
 * Complexity: O(m log m) to merge + O(n log m) to query
 *   where m = number of ranges, n = number of IDs
 */

import java.io.File

fun main() {
    val input = File("src/main/resources/inputs/day05.txt").readText()

    println("Part 1: ${day05Part1(input)}")
    println("Part 2: ${day05Part2(input)}")
}

fun day05Part1(input: String): Int {
    val (ranges, ids) = parseDay05Input(input)
    val merged = mergeRanges(ranges)

    return ids.count { id -> isInRanges(id, merged) }
}

data class LongRange05(val start: Long, val end: Long)

fun parseDay05Input(input: String): Pair<List<LongRange05>, List<Long>> {
    val parts = input.trim().split("\n\n")

    val ranges = parts[0].lines().map { line ->
        val (start, end) = line.split("-").map { it.toLong() }
        LongRange05(start, end)
    }

    val ids = parts[1].lines().map { it.toLong() }

    return ranges to ids
}

fun mergeRanges(ranges: List<LongRange05>): List<LongRange05> {
    if (ranges.isEmpty()) return emptyList()

    // Sort by start position
    val sorted = ranges.sortedBy { it.start }

    val merged = mutableListOf<LongRange05>()
    var current = sorted[0]

    for (i in 1 until sorted.size) {
        val next = sorted[i]
        if (next.start <= current.end + 1) {
            // Overlapping or adjacent - extend current range
            current = LongRange05(current.start, maxOf(current.end, next.end))
        } else {
            // Gap - save current and start new range
            merged.add(current)
            current = next
        }
    }
    merged.add(current)

    return merged
}

fun isInRanges(id: Long, sortedRanges: List<LongRange05>): Boolean {
    // Binary search: find rightmost range whose start <= id
    var lo = 0
    var hi = sortedRanges.size - 1
    var result = -1

    while (lo <= hi) {
        val mid = (lo + hi) / 2
        if (sortedRanges[mid].start <= id) {
            result = mid
            lo = mid + 1
        } else {
            hi = mid - 1
        }
    }

    // Check if id falls within the found range
    return result >= 0 && id <= sortedRanges[result].end
}

fun day05Part2(input: String): Long {
    val (ranges, _) = parseDay05Input(input)
    val merged = mergeRanges(ranges)

    // Sum the size of each merged range (end - start + 1 for inclusive)
    return merged.sumOf { it.end - it.start + 1 }
}
