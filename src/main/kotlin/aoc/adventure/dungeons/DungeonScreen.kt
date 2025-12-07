package aoc.adventure.dungeons

import aoc.adventure.GameState
import aoc.adventure.Location
import aoc.adventure.core.*
import aoc.adventure.screens.ResultScreen
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import java.io.File

/**
 * DungeonScreen - base class for puzzle dungeons.
 *
 * Each day has a dungeon that visualizes the puzzle solution.
 * Subclasses implement the specific visualization for each puzzle.
 */
class DungeonScreen(
    private val day: Int,
    private val location: Location,
    private var gameState: GameState,
    private val onGameStateChanged: (GameState) -> Unit
) : Screen {

    // Puzzle state
    private var puzzleState = PuzzleState.INTRO
    private var currentPart = 1
    private var animationFrame = 0L

    // Input data
    private var inputLines: List<String> = emptyList()

    // Puzzle solver (specific to each day)
    private lateinit var puzzle: DungeonPuzzle

    // Results
    private var part1Result: Long = 0
    private var part2Result: Long = 0

    // Animation steps for visualization
    private var animationSteps = mutableListOf<AnimationStep>()
    private var currentStep = 0
    private var stepTime = 0L
    private var autoPlay = false
    private val stepDuration = 100L // ms per step

    enum class PuzzleState {
        INTRO,      // Show puzzle description
        SOLVING_1,  // Animating part 1
        RESULT_1,   // Show part 1 result
        SOLVING_2,  // Animating part 2
        RESULT_2,   // Show part 2 result
        COMPLETE    // All done
    }

    override fun onEnter() {
        // Load input file
        val inputFile = File("src/main/resources/inputs/day${day.toString().padStart(2, '0')}.txt")
        inputLines = if (inputFile.exists()) {
            inputFile.readLines()
        } else {
            listOf("No input file found")
        }

        // Create the appropriate puzzle solver
        puzzle = createPuzzle(day)

        puzzleState = PuzzleState.INTRO
    }

    private fun createPuzzle(day: Int): DungeonPuzzle {
        return when (day) {
            1 -> Day01DungeonPuzzle()
            2 -> Day02DungeonPuzzle()
            3 -> Day03DungeonPuzzle()
            4 -> Day04DungeonPuzzle()
            5 -> Day05DungeonPuzzle()
            6 -> Day06DungeonPuzzle()
            else -> PlaceholderPuzzle(day)
        }
    }

    override fun update(deltaMs: Long) {
        animationFrame += deltaMs

        // Auto-advance animation steps
        if (autoPlay && (puzzleState == PuzzleState.SOLVING_1 || puzzleState == PuzzleState.SOLVING_2)) {
            stepTime += deltaMs
            if (stepTime >= stepDuration && currentStep < animationSteps.size - 1) {
                currentStep++
                stepTime = 0
            }

            // Check if animation complete
            if (currentStep >= animationSteps.size - 1) {
                if (puzzleState == PuzzleState.SOLVING_1) {
                    puzzleState = PuzzleState.RESULT_1
                } else if (puzzleState == PuzzleState.SOLVING_2) {
                    puzzleState = PuzzleState.RESULT_2
                }
            }
        }
    }

    override fun render(renderer: Renderer) {
        renderer.clear(Colors.PANEL_BG)

        // Title
        renderer.drawCenteredText(1, "═══ ${location.displayName.uppercase()} - DUNGEON ═══", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)

        when (puzzleState) {
            PuzzleState.INTRO -> renderIntro(renderer)
            PuzzleState.SOLVING_1, PuzzleState.SOLVING_2 -> renderSolving(renderer)
            PuzzleState.RESULT_1 -> renderResult(renderer, 1, part1Result)
            PuzzleState.RESULT_2 -> renderResult(renderer, 2, part2Result)
            PuzzleState.COMPLETE -> renderComplete(renderer)
        }

        renderer.refresh()
    }

    override fun handleInput(key: KeyStroke): ScreenAction {
        when (key.keyType) {
            KeyType.Escape -> return ScreenAction.Pop
            KeyType.Enter -> return handleEnter()
            KeyType.Character -> {
                when (key.character?.lowercaseChar()) {
                    ' ' -> return handleEnter()
                    'q' -> return ScreenAction.Pop
                    'a' -> autoPlay = !autoPlay
                    'n' -> { // Next step
                        if (currentStep < animationSteps.size - 1) {
                            currentStep++
                        }
                    }
                    'p' -> { // Previous step
                        if (currentStep > 0) {
                            currentStep--
                        }
                    }
                    's' -> { // Skip animation
                        skipAnimation()
                    }
                }
            }
            KeyType.ArrowRight -> {
                if (currentStep < animationSteps.size - 1) currentStep++
            }
            KeyType.ArrowLeft -> {
                if (currentStep > 0) currentStep--
            }
            else -> {}
        }
        return ScreenAction.None
    }

    private fun handleEnter(): ScreenAction {
        when (puzzleState) {
            PuzzleState.INTRO -> {
                startPart1()
            }
            PuzzleState.RESULT_1 -> {
                startPart2()
            }
            PuzzleState.RESULT_2, PuzzleState.COMPLETE -> {
                // Update game state and return to location
                val newState = gameState
                    .completePart(day, 1)
                    .completePart(day, 2)
                onGameStateChanged(newState)
                return ScreenAction.Replace(
                    ResultScreen(day, part1Result, part2Result, newState, onGameStateChanged)
                )
            }
            else -> {}
        }
        return ScreenAction.None
    }

    private fun startPart1() {
        puzzleState = PuzzleState.SOLVING_1
        currentPart = 1
        currentStep = 0
        stepTime = 0
        autoPlay = true

        // Run puzzle and collect animation steps
        animationSteps.clear()
        part1Result = puzzle.solvePart1(inputLines) { step ->
            animationSteps.add(step)
        }

        // Ensure at least one step
        if (animationSteps.isEmpty()) {
            animationSteps.add(AnimationStep("Calculating...", emptyList(), part1Result))
        }
    }

    private fun startPart2() {
        puzzleState = PuzzleState.SOLVING_2
        currentPart = 2
        currentStep = 0
        stepTime = 0
        autoPlay = true

        // Run puzzle and collect animation steps
        animationSteps.clear()
        part2Result = puzzle.solvePart2(inputLines) { step ->
            animationSteps.add(step)
        }

        // Ensure at least one step
        if (animationSteps.isEmpty()) {
            animationSteps.add(AnimationStep("Calculating...", emptyList(), part2Result))
        }
    }

    private fun skipAnimation() {
        currentStep = animationSteps.size - 1
        if (puzzleState == PuzzleState.SOLVING_1) {
            puzzleState = PuzzleState.RESULT_1
        } else if (puzzleState == PuzzleState.SOLVING_2) {
            puzzleState = PuzzleState.RESULT_2
        }
    }

    private fun renderIntro(renderer: Renderer) {
        val centerY = renderer.height / 2 - 5

        renderer.drawCenteredText(centerY, "Day $day: ${location.displayName}", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)
        renderer.drawCenteredText(centerY + 2, location.description, Colors.LIGHT_GRAY)

        renderer.drawCenteredText(centerY + 5, "Press [Enter] to begin solving", Colors.CYAN)
        renderer.drawCenteredText(centerY + 7, "[Esc] Return to room", Colors.GRAY)
    }

    private fun renderSolving(renderer: Renderer) {
        // Part indicator
        renderer.drawCenteredText(3, "Part $currentPart", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)

        // Progress bar
        val progress = if (animationSteps.isNotEmpty()) {
            currentStep.toFloat() / animationSteps.size
        } else 0f
        renderer.drawProgressBar(10, 5, renderer.width - 20, progress, Colors.CHRISTMAS_GREEN, Colors.DARK_GRAY)

        // Current step info
        if (animationSteps.isNotEmpty() && currentStep < animationSteps.size) {
            val step = animationSteps[currentStep]

            // Step description
            renderer.drawCenteredText(7, step.description, Colors.WHITE)

            // Visualization area
            val vizY = 9
            val vizHeight = renderer.height - 16

            // Draw visualization lines
            for ((i, line) in step.visualization.take(vizHeight).withIndex()) {
                val truncated = line.take(renderer.width - 4)
                renderer.drawText(2, vizY + i, truncated, Colors.LIGHT_GRAY)
            }

            // Current value
            renderer.drawCenteredText(renderer.height - 6, "Current: ${step.currentValue}", Colors.CYAN)
        }

        // Controls
        renderer.drawCenteredText(renderer.height - 4, "Step ${currentStep + 1} / ${animationSteps.size}", Colors.GRAY)
        renderer.drawCenteredText(
            renderer.height - 2,
            "[←→] Step   [A] Auto-play: ${if (autoPlay) "ON" else "OFF"}   [S] Skip   [Esc] Exit",
            Colors.GRAY
        )
    }

    private fun renderResult(renderer: Renderer, part: Int, result: Long) {
        val centerY = renderer.height / 2 - 3

        renderer.drawCenteredText(centerY, "Part $part Complete!", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)
        renderer.drawCenteredText(centerY + 2, "Answer: $result", Colors.CYAN, Colors.PANEL_BG, SGR.BOLD)

        val starEarned = "⭐"
        renderer.drawCenteredText(centerY + 4, starEarned, Colors.GOLD)

        if (part == 1) {
            renderer.drawCenteredText(centerY + 7, "Press [Enter] to continue to Part 2", Colors.WHITE)
        } else {
            renderer.drawCenteredText(centerY + 7, "Press [Enter] to see results", Colors.WHITE)
        }
    }

    private fun renderComplete(renderer: Renderer) {
        val centerY = renderer.height / 2 - 3

        renderer.drawCenteredText(centerY, "Dungeon Complete!", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)
        renderer.drawCenteredText(centerY + 2, "Part 1: $part1Result", Colors.CYAN)
        renderer.drawCenteredText(centerY + 3, "Part 2: $part2Result", Colors.CYAN)
        renderer.drawCenteredText(centerY + 5, "⭐⭐", Colors.GOLD)
        renderer.drawCenteredText(centerY + 8, "Press [Enter] to return", Colors.WHITE)
    }
}

/**
 * A single step in the puzzle animation.
 */
data class AnimationStep(
    val description: String,
    val visualization: List<String>,
    val currentValue: Long
)

/**
 * Interface for puzzle solvers with animation support.
 */
interface DungeonPuzzle {
    fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long
    fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long
}

/**
 * Placeholder puzzle for unimplemented days.
 */
class PlaceholderPuzzle(private val day: Int) : DungeonPuzzle {
    override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        onStep(AnimationStep("Day $day puzzle not yet implemented", listOf("Coming soon..."), 0))
        return 0
    }

    override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
        onStep(AnimationStep("Day $day puzzle not yet implemented", listOf("Coming soon..."), 0))
        return 0
    }
}
