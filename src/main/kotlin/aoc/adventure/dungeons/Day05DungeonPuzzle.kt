package aoc.adventure.dungeons

/**
 * Day 5: Cafeteria - Range Query Puzzle
 *
 * Visualizes checking ingredient IDs against fresh ranges.
 */
class Day05DungeonPuzzle : DungeonPuzzle {

    override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val (ranges, queries) = parseInput(input)

        onStep(AnimationStep(
            "Merging ${ranges.size} ingredient ranges...",
            listOf("Preparing fresh ingredient database"),
            0
        ))

        // Merge overlapping ranges
        val merged = mergeRanges(ranges)

        onStep(AnimationStep(
            "Merged into ${merged.size} disjoint ranges",
            listOf("Ready to check ${queries.size} ingredient IDs"),
            0
        ))

        var freshCount = 0L
        for ((index, query) in queries.withIndex()) {
            if (isInRanges(query, merged)) {
                freshCount++
            }

            if (index < 10 || index % 100 == 0) {
                onStep(AnimationStep(
                    "Checking ID $query: ${if (isInRanges(query, merged)) "FRESH ✓" else "NOT FRESH ✗"}",
                    renderConveyor(query, merged, freshCount, index + 1, queries.size),
                    freshCount
                ))
            }
        }

        onStep(AnimationStep(
            "Complete! Found $freshCount fresh ingredients",
            listOf(
                "╔═════════════════════════════════╗",
                "║    CAFETERIA SCAN COMPLETE      ║",
                "╠═════════════════════════════════╣",
                "║  Fresh IDs: $freshCount".padEnd(34) + "║",
                "╚═════════════════════════════════╝"
            ),
            freshCount
        ))

        return freshCount
    }

    override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val (ranges, _) = parseInput(input)

        onStep(AnimationStep(
            "Counting total fresh ingredient IDs in all ranges...",
            listOf("Merging ranges and counting"),
            0
        ))

        val merged = mergeRanges(ranges)
        var total = 0L

        for ((index, range) in merged.withIndex()) {
            val count = range.second - range.first + 1
            total += count

            if (index < 10) {
                onStep(AnimationStep(
                    "Range ${range.first}-${range.second}: $count IDs",
                    listOf("Running total: $total"),
                    total
                ))
            }
        }

        onStep(AnimationStep(
            "Total fresh ingredient IDs: $total",
            listOf(
                "╔═════════════════════════════════════╗",
                "║  TOTAL FRESH IDS: $total".padEnd(38) + "║",
                "╚═════════════════════════════════════╝"
            ),
            total
        ))

        return total
    }

    private fun parseInput(input: List<String>): Pair<List<Pair<Long, Long>>, List<Long>> {
        val ranges = mutableListOf<Pair<Long, Long>>()
        val queries = mutableListOf<Long>()
        var parsingRanges = true

        for (line in input) {
            if (line.isBlank()) {
                parsingRanges = false
                continue
            }

            if (parsingRanges && line.contains("-")) {
                val parts = line.split("-")
                ranges.add(parts[0].toLong() to parts[1].toLong())
            } else if (!parsingRanges) {
                queries.add(line.toLong())
            }
        }

        return ranges to queries
    }

    private fun mergeRanges(ranges: List<Pair<Long, Long>>): List<Pair<Long, Long>> {
        if (ranges.isEmpty()) return emptyList()

        val sorted = ranges.sortedBy { it.first }
        val merged = mutableListOf<Pair<Long, Long>>()
        var current = sorted[0]

        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (next.first <= current.second + 1) {
                current = current.first to maxOf(current.second, next.second)
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)

        return merged
    }

    private fun isInRanges(value: Long, ranges: List<Pair<Long, Long>>): Boolean {
        // Binary search
        var lo = 0
        var hi = ranges.size - 1
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            val range = ranges[mid]
            when {
                value < range.first -> hi = mid - 1
                value > range.second -> lo = mid + 1
                else -> return true
            }
        }
        return false
    }

    private fun renderConveyor(current: Long, ranges: List<Pair<Long, Long>>, fresh: Long, checked: Int, total: Int): List<String> {
        val isFresh = isInRanges(current, ranges)
        return listOf(
            "┌─────────────────────────────────────────┐",
            "│  🥕 INGREDIENT CONVEYOR                 │",
            "├─────────────────────────────────────────┤",
            "│  Current ID: $current".padEnd(42) + "│",
            "│  Status: ${if (isFresh) "✓ FRESH" else "✗ NOT FRESH"}".padEnd(42) + "│",
            "│                                         │",
            "│  Checked: $checked / $total".padEnd(42) + "│",
            "│  Fresh count: $fresh".padEnd(42) + "│",
            "└─────────────────────────────────────────┘"
        )
    }
}
