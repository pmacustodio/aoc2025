package aoc.adventure

import aoc.adventure.core.*
import aoc.adventure.screens.TitleScreen
import aoc.adventure.screens.WorldMapScreen
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory

/**
 * AdventureGame - main entry point for the North Pole Adventure TUI.
 *
 * This is a terminal-based adventure game where players explore the North Pole
 * and solve Advent of Code 2025 puzzles as "dungeons".
 *
 * Run with: ./gradlew adventure
 */
class AdventureGame {
    private lateinit var terminal: com.googlecode.lanterna.terminal.Terminal
    private lateinit var screen: TerminalScreen
    private lateinit var renderer: Renderer
    private lateinit var screenManager: ScreenManager

    private var gameState = GameState()
    private var running = true

    /**
     * Initialize the terminal and screens.
     */
    private fun initialize() {
        // Create terminal with default factory (auto-detects best terminal type)
        terminal = DefaultTerminalFactory()
            .setInitialTerminalSize(com.googlecode.lanterna.TerminalSize(100, 35))
            .createTerminal()

        // Create screen for double-buffering
        screen = TerminalScreen(terminal)
        screen.startScreen()
        screen.cursorPosition = null // Hide cursor

        // Create renderer
        renderer = Renderer(screen)

        // Create screen manager and show title
        screenManager = ScreenManager()
        screenManager.push(createTitleScreen())
    }

    /**
     * Create the title screen with proper callbacks.
     */
    private fun createTitleScreen(): TitleScreen {
        return TitleScreen(
            onStartGame = { state ->
                gameState = state
                screenManager.push(WorldMapScreen(gameState, ::onGameStateChanged))
            },
            onQuit = {
                running = false
            }
        )
    }

    /**
     * Callback when game state changes (puzzle completed, etc).
     */
    private fun onGameStateChanged(newState: GameState) {
        gameState = newState
        GameState.save(gameState)
    }

    /**
     * Main game loop.
     */
    fun run() {
        try {
            initialize()

            var lastTime = System.currentTimeMillis()

            while (running && !screenManager.isEmpty) {
                // Calculate delta time
                val currentTime = System.currentTimeMillis()
                val deltaMs = currentTime - lastTime
                lastTime = currentTime

                // Update current screen
                screenManager.update(deltaMs)

                // Render current screen
                screenManager.render(renderer)

                // Handle input (non-blocking poll)
                val key = screen.pollInput()
                if (key != null) {
                    // Global quit shortcut
                    if (key.keyType == KeyType.Character && key.character == 'Q' && key.isCtrlDown) {
                        running = false
                    } else {
                        running = screenManager.handleInput(key)
                    }
                }

                // Cap frame rate (~30 FPS)
                Thread.sleep(33)
            }
        } finally {
            cleanup()
        }
    }

    /**
     * Clean up terminal resources.
     */
    private fun cleanup() {
        try {
            screen.stopScreen()
            terminal.close()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}

/**
 * Main entry point.
 */
fun main() {
    AdventureGame().run()
}
