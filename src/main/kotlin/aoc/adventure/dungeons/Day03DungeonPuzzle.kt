package aoc.adventure.dungeons

/**
 * Day 3: Lobby - Battery Selection Puzzle
 *
 * Visualizes selecting k digits from a battery bank to form
 * the largest possible k-digit number.
 */
class Day03DungeonPuzzle : DungeonPuzzle {

    override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val line = input.firstOrNull { it.isNotBlank() } ?: return 0
        val parts = line.split(" ")
        val digits = parts[0]
        val k = parts[1].toInt()

        onStep(AnimationStep(
            "Battery bank contains: $digits",
            listOf(
                "Digits available: $digits",
                "Need to select: $k digits",
                "Goal: Form largest $k-digit number"
            ),
            0
        ))

        val result = selectLargest(digits, k, onStep)
        return result.toLong()
    }

    override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val line = input.firstOrNull { it.isNotBlank() } ?: return 0
        val parts = line.split(" ")
        val digits = parts[0]
        val k = parts[2].toInt() // Second number for part 2

        onStep(AnimationStep(
            "Part 2: Select $k digits from $digits",
            listOf("Now selecting $k digits for maximum value"),
            0
        ))

        val result = selectLargest(digits, k, onStep)
        return result.toLong()
    }

    private fun selectLargest(digits: String, k: Int, onStep: (AnimationStep) -> Unit): Long {
        val n = digits.length
        if (k >= n) return digits.toLong()

        // Precompute suffix maximums
        val suffixMax = IntArray(n + 1) { -1 }
        for (i in n - 1 downTo 0) {
            suffixMax[i] = maxOf(digits[i] - '0', suffixMax[i + 1])
        }

        val result = StringBuilder()
        var pos = 0

        for (i in 0 until k) {
            val remaining = k - i
            val lastValid = n - remaining

            // Find largest digit in valid range
            var bestDigit = -1
            var bestPos = pos

            for (j in pos..lastValid) {
                val d = digits[j] - '0'
                if (d > bestDigit) {
                    bestDigit = d
                    bestPos = j
                    // Early exit if we found max possible
                    if (d == 9) break
                }
            }

            result.append(bestDigit)
            pos = bestPos + 1

            if (i < 5 || i == k - 1) {
                onStep(AnimationStep(
                    "Selected digit $bestDigit at position $bestPos",
                    renderBattery(digits, pos - 1, result.toString(), k),
                    result.toString().toLongOrNull() ?: 0
                ))
            }
        }

        onStep(AnimationStep(
            "Largest $k-digit number: $result",
            listOf(
                "╔══════════════════════════════════════╗",
                "║    BATTERY SELECTION COMPLETE        ║",
                "╠══════════════════════════════════════╣",
                "║  Result: $result".padEnd(39) + "║",
                "╚══════════════════════════════════════╝"
            ),
            result.toString().toLong()
        ))

        return result.toString().toLong()
    }

    private fun renderBattery(digits: String, selectedPos: Int, current: String, target: Int): List<String> {
        val displayDigits = digits.take(60) // Truncate for display
        val highlight = buildString {
            for (i in displayDigits.indices) {
                append(if (i == selectedPos) "▲" else " ")
            }
        }

        return listOf(
            "Battery bank:",
            "┌${"─".repeat(displayDigits.length + 2)}┐",
            "│ $displayDigits │",
            "└${"─".repeat(displayDigits.length + 2)}┘",
            "  $highlight",
            "",
            "Building number: $current",
            "Digits remaining: ${target - current.length}"
        )
    }
}
