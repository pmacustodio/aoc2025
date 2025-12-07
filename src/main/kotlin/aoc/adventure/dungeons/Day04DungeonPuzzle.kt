package aoc.adventure.dungeons

/**
 * Day 4: Printing Department - Grid Removal Puzzle
 *
 * Visualizes a grid of paper rolls (@) where accessible ones
 * (fewer than 4 neighbors) are removed iteratively.
 */
class Day04DungeonPuzzle : DungeonPuzzle {

    override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val grid = input.filter { it.isNotBlank() }.map { it.toMutableList() }.toMutableList()
        if (grid.isEmpty()) return 0

        onStep(AnimationStep(
            "Counting accessible paper rolls (< 4 neighbors)...",
            renderGrid(grid, emptySet()),
            0
        ))

        var accessible = 0L
        val rows = grid.size
        val cols = grid[0].size

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (grid[r][c] == '@') {
                    val neighbors = countNeighbors(grid, r, c)
                    if (neighbors < 4) {
                        accessible++
                    }
                }
            }
        }

        onStep(AnimationStep(
            "Found $accessible accessible rolls in initial grid",
            renderGrid(grid, emptySet()),
            accessible
        ))

        return accessible
    }

    override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        val grid = input.filter { it.isNotBlank() }.map { it.toMutableList() }.toMutableList()
        if (grid.isEmpty()) return 0

        val rows = grid.size
        val cols = grid[0].size
        var totalRemoved = 0L
        var round = 0

        onStep(AnimationStep(
            "Iteratively removing accessible rolls until none remain...",
            renderGrid(grid, emptySet()),
            0
        ))

        while (true) {
            round++
            val toRemove = mutableSetOf<Pair<Int, Int>>()

            // Find all accessible rolls
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (grid[r][c] == '@') {
                        val neighbors = countNeighbors(grid, r, c)
                        if (neighbors < 4) {
                            toRemove.add(r to c)
                        }
                    }
                }
            }

            if (toRemove.isEmpty()) break

            // Show what will be removed
            if (round <= 10 || round % 10 == 0) {
                onStep(AnimationStep(
                    "Round $round: Removing ${toRemove.size} accessible rolls",
                    renderGrid(grid, toRemove),
                    totalRemoved
                ))
            }

            // Remove them
            for ((r, c) in toRemove) {
                grid[r][c] = '.'
            }
            totalRemoved += toRemove.size
        }

        onStep(AnimationStep(
            "Complete! Removed $totalRemoved rolls over $round rounds",
            listOf(
                "╔═══════════════════════════════════╗",
                "║   FORKLIFT OPERATION COMPLETE     ║",
                "╠═══════════════════════════════════╣",
                "║  Rounds: $round".padEnd(36) + "║",
                "║  Total removed: $totalRemoved".padEnd(36) + "║",
                "╚═══════════════════════════════════╝"
            ),
            totalRemoved
        ))

        return totalRemoved
    }

    private fun countNeighbors(grid: List<List<Char>>, r: Int, c: Int): Int {
        val dirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        var count = 0
        for ((dr, dc) in dirs) {
            val nr = r + dr
            val nc = c + dc
            if (nr in grid.indices && nc in grid[0].indices && grid[nr][nc] == '@') {
                count++
            }
        }
        return count
    }

    private fun renderGrid(grid: List<List<Char>>, highlight: Set<Pair<Int, Int>>): List<String> {
        val lines = mutableListOf<String>()
        val displayRows = minOf(grid.size, 15)
        val displayCols = minOf(grid[0].size, 60)

        for (r in 0 until displayRows) {
            val line = buildString {
                for (c in 0 until displayCols) {
                    val ch = grid[r][c]
                    if (r to c in highlight) {
                        append('X') // Marked for removal
                    } else {
                        append(ch)
                    }
                }
            }
            lines.add(line)
        }

        if (grid.size > displayRows || grid[0].size > displayCols) {
            lines.add("... (grid truncated for display)")
        }

        return lines
    }
}
