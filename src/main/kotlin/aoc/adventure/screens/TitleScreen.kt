package aoc.adventure.screens

import aoc.adventure.GameState
import aoc.adventure.core.*
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

/**
 * TitleScreen - the main menu of the game.
 *
 * Displays ASCII art title, menu options, and handles
 * new game / continue game / quit selection.
 */
class TitleScreen(
    private val onStartGame: (GameState) -> Unit,
    private val onQuit: () -> Unit
) : Screen {

    private var selectedOption = 0
    private val hasSaveFile = GameState.saveExists()

    // Menu options depend on whether a save exists
    private val options = if (hasSaveFile) {
        listOf("Continue", "New Game", "Quit")
    } else {
        listOf("New Game", "Quit")
    }

    // Animation state for twinkling stars
    private var animationFrame = 0L
    private val starPositions = mutableListOf<Pair<Int, Int>>()
    private var starsInitialized = false

    override fun onEnter() {
        selectedOption = 0
    }

    override fun update(deltaMs: Long) {
        animationFrame += deltaMs
    }

    override fun render(renderer: Renderer) {
        // Initialize star positions based on terminal size (once)
        if (!starsInitialized) {
            initializeStars(renderer.width, renderer.height)
            starsInitialized = true
        }

        // Clear with dark blue background
        renderer.clear(Colors.PANEL_BG)

        // Draw twinkling stars in background
        drawStars(renderer)

        // Draw ASCII art title
        val titleY = 3
        drawTitle(renderer, titleY)

        // Draw subtitle
        renderer.drawCenteredText(
            titleY + TITLE_ART.size + 1,
            "~ A Terminal Adventure ~",
            Colors.GOLD
        )

        // Draw year
        renderer.drawCenteredText(
            titleY + TITLE_ART.size + 3,
            "Advent of Code 2025",
            Colors.ICE_BLUE
        )

        // Draw menu options
        val menuY = titleY + TITLE_ART.size + 7
        drawMenu(renderer, menuY)

        // Draw controls hint at bottom
        renderer.drawCenteredText(
            renderer.height - 2,
            "[↑↓] Select   [Enter] Confirm   [Q] Quit",
            Colors.GRAY
        )

        renderer.refresh()
    }

    override fun handleInput(key: KeyStroke): ScreenAction {
        when (key.keyType) {
            KeyType.ArrowUp -> {
                selectedOption = (selectedOption - 1 + options.size) % options.size
            }
            KeyType.ArrowDown -> {
                selectedOption = (selectedOption + 1) % options.size
            }
            KeyType.Enter -> {
                return handleSelection()
            }
            KeyType.Character -> {
                when (key.character?.lowercaseChar()) {
                    'q' -> return ScreenAction.Quit
                }
            }
            KeyType.Escape -> return ScreenAction.Quit
            else -> {}
        }
        return ScreenAction.None
    }

    private fun handleSelection(): ScreenAction {
        val option = options[selectedOption]
        when (option) {
            "Continue" -> {
                val state = GameState.load()
                onStartGame(state)
            }
            "New Game" -> {
                if (hasSaveFile) {
                    // Could show confirmation, but for now just delete
                    GameState.deleteSave()
                }
                onStartGame(GameState())
            }
            "Quit" -> {
                onQuit()
                return ScreenAction.Quit
            }
        }
        return ScreenAction.None
    }

    private fun initializeStars(width: Int, height: Int) {
        starPositions.clear()
        // Scatter some stars around the background
        repeat(30) {
            val x = (Math.random() * width).toInt()
            val y = (Math.random() * height).toInt()
            starPositions.add(x to y)
        }
    }

    private fun drawStars(renderer: Renderer) {
        val starChars = listOf('·', '∙', '✦', '✧', '⋆', '*')
        val time = animationFrame / 500 // Change every 500ms

        for ((i, pos) in starPositions.withIndex()) {
            // Each star twinkles at different rate
            val brightness = ((time + i * 7) % 4).toInt()
            val color = when (brightness) {
                0 -> Colors.DARK_GRAY
                1 -> Colors.GRAY
                2 -> Colors.LIGHT_GRAY
                else -> Colors.WHITE
            }
            val char = starChars[i % starChars.size]
            renderer.drawChar(pos.first, pos.second, char, color, Colors.PANEL_BG)
        }
    }

    private fun drawTitle(renderer: Renderer, startY: Int) {
        for ((i, line) in TITLE_ART.withIndex()) {
            // Gradient from red to green across the title
            val progress = i.toFloat() / TITLE_ART.size
            val color = if (progress < 0.5f) {
                Colors.CHRISTMAS_RED.lighten(progress * 0.5f)
            } else {
                Colors.CHRISTMAS_GREEN.lighten((1f - progress) * 0.5f)
            }
            renderer.drawCenteredText(startY + i, line, color, Colors.PANEL_BG, SGR.BOLD)
        }
    }

    private fun drawMenu(renderer: Renderer, startY: Int) {
        // Draw box around menu
        val boxWidth = 30
        val boxHeight = options.size + 4
        val boxX = (renderer.width - boxWidth) / 2
        val boxY = startY - 1

        renderer.drawBox(boxX, boxY, boxWidth, boxHeight, Colors.BORDER, Colors.PANEL_BG, BoxStyle.DOUBLE)
        renderer.fillRect(boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2, ' ', Colors.WHITE, Colors.PANEL_BG)

        for ((i, option) in options.withIndex()) {
            val y = startY + i + 1
            val isSelected = i == selectedOption

            if (isSelected) {
                // Highlight selected option
                val text = " > $option < "
                renderer.drawCenteredText(y, text, Colors.GOLD, Colors.HIGHLIGHT, SGR.BOLD)
            } else {
                val text = "   $option   "
                renderer.drawCenteredText(y, text, Colors.LIGHT_GRAY, Colors.PANEL_BG)
            }
        }
    }

    companion object {
        // ASCII art title
        private val TITLE_ART = listOf(
            "╔═╗╔╦╗╦  ╦╔═╗╔╗╔╔╦╗╦ ╦╦═╗╔═╗",
            "╠═╣ ║║╚╗╔╝║╣ ║║║ ║ ║ ║╠╦╝║╣ ",
            "╩ ╩═╩╝ ╚╝ ╚═╝╝╚╝ ╩ ╚═╝╩╚═╚═╝",
            "                            ",
            "  ╔═╗╔╦╗  ╔╦╗╦ ╦╔═╗         ",
            "  ╠═╣ ║    ║ ╠═╣║╣          ",
            "  ╩ ╩ ╩    ╩ ╩ ╩╚═╝         ",
            "                            ",
            "╔╗╔╔═╗╦═╗╔╦╗╦ ╦  ╔═╗╔═╗╦  ╔═╗",
            "║║║║ ║╠╦╝ ║ ╠═╣  ╠═╝║ ║║  ║╣ ",
            "╝╚╝╚═╝╩╚═ ╩ ╩ ╩  ╩  ╚═╝╩═╝╚═╝"
        )
    }
}
