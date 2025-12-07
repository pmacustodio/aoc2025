package aoc.adventure.dungeons

/**
 * Day 1: Secret Entrance - Dial Puzzle
 *
 * Visualizes a spinning vault dial (0-99) processing rotations.
 * Shows the dial position, direction of rotation, and zero crossings.
 */
class Day01DungeonPuzzle : DungeonPuzzle {

    override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        var position = 50
        var zeroCount = 0

        // Initial state
        onStep(AnimationStep(
            "Dial starts at position 50",
            renderDial(50, null, 0),
            0
        ))

        for ((index, line) in input.withIndex()) {
            if (line.isBlank()) continue

            val direction = line[0]
            val distance = line.substring(1).toInt()

            position = when (direction) {
                'L' -> (position - distance).mod(100)
                'R' -> (position + distance).mod(100)
                else -> position
            }

            val landsOnZero = position == 0
            if (landsOnZero) zeroCount++

            // Only emit step every few instructions to avoid too many steps
            if (index < 20 || landsOnZero || index % 50 == 0) {
                val dirName = if (direction == 'L') "Left" else "Right"
                onStep(AnimationStep(
                    "$line: Rotate $dirName $distance → Position $position${if (landsOnZero) " ★ ZERO!" else ""}",
                    renderDial(position, direction, zeroCount),
                    zeroCount.toLong()
                ))
            }
        }

        // Final state
        onStep(AnimationStep(
            "Complete! Dial landed on 0 a total of $zeroCount times",
            renderDial(position, null, zeroCount),
            zeroCount.toLong()
        ))

        return zeroCount.toLong()
    }

    override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        var position = 50
        var zeroCount = 0L

        // Initial state
        onStep(AnimationStep(
            "Now counting PASSES through 0 (not just landings)",
            renderDial(50, null, 0),
            0
        ))

        for ((index, line) in input.withIndex()) {
            if (line.isBlank()) continue

            val direction = line[0]
            val distance = line.substring(1).toInt()

            val crosses = when (direction) {
                'L' -> if (position == 0) distance / 100 else (distance - position + 100) / 100
                'R' -> (distance + position) / 100
                else -> 0
            }

            zeroCount += crosses

            position = when (direction) {
                'L' -> (position - distance).mod(100)
                'R' -> (position + distance).mod(100)
                else -> position
            }

            // Only emit step every few instructions or when interesting
            if (index < 20 || crosses > 0 || index % 50 == 0) {
                val dirName = if (direction == 'L') "Left" else "Right"
                val crossMsg = if (crosses > 0) " → Crosses 0 $crosses time(s)!" else ""
                onStep(AnimationStep(
                    "$line: Rotate $dirName $distance$crossMsg",
                    renderDialWithCrossings(position, direction, zeroCount),
                    zeroCount
                ))
            }
        }

        // Final state
        onStep(AnimationStep(
            "Complete! Dial passed through 0 a total of $zeroCount times",
            renderDialWithCrossings(position, null, zeroCount),
            zeroCount
        ))

        return zeroCount
    }

    /**
     * Render a visual representation of the dial.
     */
    private fun renderDial(position: Int, direction: Char?, zeroCount: Int): List<String> {
        val lines = mutableListOf<String>()

        // ASCII art dial
        lines.add("        ┌───────────────────┐")
        lines.add("       ╱    99   0   1       ╲")
        lines.add("      │   95           5      │")
        lines.add("      │ 90       ↑       10   │")
        lines.add("      │           ${formatPosition(position)}           │")
        lines.add("      │ 85               15   │")
        lines.add("      │   80           20     │")
        lines.add("       ╲    75  50  25       ╱")
        lines.add("        └───────────────────┘")

        lines.add("")
        lines.add("  Position: $position")
        lines.add("  Direction: ${direction?.let { if (it == 'L') "← Left" else "→ Right" } ?: "---"}")
        lines.add("  Zero landings: $zeroCount")

        // Visual position indicator
        val bar = "  [" + "░".repeat(position) + "█" + "░".repeat(99 - position) + "]"
        if (bar.length <= 104) {
            lines.add("")
            lines.add("  0" + " ".repeat(48) + "50" + " ".repeat(47) + "99")
            lines.add(bar)
        }

        return lines
    }

    private fun renderDialWithCrossings(position: Int, direction: Char?, crossings: Long): List<String> {
        val lines = mutableListOf<String>()

        // ASCII art dial with emphasis on zero
        lines.add("        ┌───────────────────┐")
        lines.add("       ╱    99  ★0★  1       ╲")
        lines.add("      │   95           5      │")
        lines.add("      │ 90       ↑       10   │")
        lines.add("      │           ${formatPosition(position)}           │")
        lines.add("      │ 85               15   │")
        lines.add("      │   80           20     │")
        lines.add("       ╲    75  50  25       ╱")
        lines.add("        └───────────────────┘")

        lines.add("")
        lines.add("  Position: $position")
        lines.add("  Direction: ${direction?.let { if (it == 'L') "← Left" else "→ Right" } ?: "---"}")
        lines.add("  Zero crossings: $crossings ★")

        return lines
    }

    private fun formatPosition(pos: Int): String {
        return pos.toString().padStart(2, ' ')
    }
}
