package aoc.adventure.dungeons

/**
 * Day 2: Gift Shop - Pattern Detection Puzzle
 *
 * Visualizes finding invalid product IDs that are repeated patterns
 * (like 55, 6464, 123123).
 */
class Day02DungeonPuzzle : DungeonPuzzle {

    override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val ranges = parseRanges(input)
        var total = 0L
        var rangesProcessed = 0

        onStep(AnimationStep(
            "Scanning product ID ranges for doubled patterns...",
            listOf(
                "Looking for patterns like: 55, 6464, 123123",
                "Each digit sequence is repeated exactly twice",
                "",
                "Ranges to scan: ${ranges.size}"
            ),
            0
        ))

        for ((start, end) in ranges) {
            val count = countDoubledInRange(start, end)
            total += count
            rangesProcessed++

            if (rangesProcessed <= 10 || rangesProcessed % 100 == 0) {
                onStep(AnimationStep(
                    "Range $start-$end: Found $count doubled patterns",
                    renderScanner(start, end, count, total, rangesProcessed, ranges.size),
                    total
                ))
            }
        }

        onStep(AnimationStep(
            "Scan complete! Found $total total invalid IDs",
            listOf(
                "╔════════════════════════════════════╗",
                "║    GIFT SHOP SCANNER RESULTS       ║",
                "╠════════════════════════════════════╣",
                "║  Ranges scanned: ${ranges.size.toString().padStart(8)}        ║",
                "║  Invalid IDs:    ${total.toString().padStart(8)}        ║",
                "╚════════════════════════════════════╝"
            ),
            total
        ))

        return total
    }

    override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val ranges = parseRanges(input)
        var total = 0L

        onStep(AnimationStep(
            "Now finding patterns repeated at least twice...",
            listOf(
                "Patterns like: 555 (5×3), 121212 (12×3)",
                "Any sequence repeated 2+ times"
            ),
            0
        ))

        for ((index, pair) in ranges.withIndex()) {
            val (start, end) = pair
            val count = countRepeatedInRange(start, end)
            total += count

            if (index < 10 || index % 100 == 0) {
                onStep(AnimationStep(
                    "Range $start-$end: $count repeated patterns",
                    listOf("Progress: ${index + 1}/${ranges.size}", "Total so far: $total"),
                    total
                ))
            }
        }

        onStep(AnimationStep(
            "Complete! Found $total IDs with repeated patterns",
            listOf("Total repeated pattern IDs: $total"),
            total
        ))

        return total
    }

    private fun parseRanges(input: List<String>): List<Pair<Long, Long>> {
        // Input format: comma-separated ranges on a single line (e.g., "12077-25471,4343258-4520548,...")
        return input.filter { it.isNotBlank() }
            .flatMap { line -> line.split(",") }
            .filter { it.isNotBlank() }
            .map { range ->
                val (start, end) = range.trim().split("-").map { it.toLong() }
                start to end
            }
    }

    private fun countDoubledInRange(start: Long, end: Long): Long {
        var count = 0L
        // Generate doubled patterns within range
        val startDigits = start.toString().length
        val endDigits = end.toString().length

        for (halfLen in (startDigits + 1) / 2..(endDigits / 2)) {
            val minHalf = if (halfLen == 1) 1L else Math.pow(10.0, (halfLen - 1).toDouble()).toLong()
            val maxHalf = Math.pow(10.0, halfLen.toDouble()).toLong() - 1

            for (half in minHalf..maxHalf) {
                val doubled = (half.toString() + half.toString()).toLong()
                if (doubled in start..end) {
                    count++
                }
            }
        }
        return count
    }

    private fun countRepeatedInRange(start: Long, end: Long): Long {
        var count = 0L
        val endDigits = end.toString().length

        for (repCount in 2..endDigits) {
            val unitLen = endDigits / repCount
            if (unitLen == 0) continue

            val minUnit = if (unitLen == 1) 1L else Math.pow(10.0, (unitLen - 1).toDouble()).toLong()
            val maxUnit = Math.pow(10.0, unitLen.toDouble()).toLong() - 1

            for (unit in minUnit..maxUnit) {
                val repeated = unit.toString().repeat(repCount).toLongOrNull() ?: continue
                if (repeated in start..end) {
                    count++
                }
            }
        }
        return count
    }

    private fun renderScanner(start: Long, end: Long, found: Long, total: Long, processed: Int, totalRanges: Int): List<String> {
        return listOf(
            "╔══════════════════════════════════════════╗",
            "║  🔍 BARCODE SCANNER                      ║",
            "╠══════════════════════════════════════════╣",
            "║  Scanning: $start - $end".padEnd(43) + "║",
            "║  Doubled patterns found: $found".padEnd(43) + "║",
            "║                                          ║",
            "║  Progress: $processed / $totalRanges ranges".padEnd(43) + "║",
            "║  Total invalid: $total".padEnd(43) + "║",
            "╚══════════════════════════════════════════╝"
        )
    }
}
