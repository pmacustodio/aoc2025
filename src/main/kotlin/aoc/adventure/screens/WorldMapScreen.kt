package aoc.adventure.screens

import aoc.adventure.GameState
import aoc.adventure.Location
import aoc.adventure.core.*
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

/**
 * WorldMapScreen - navigation hub showing all 12 locations.
 *
 * Players select a day/location to visit from this screen.
 * Locked locations show a lock icon and cannot be selected.
 * The map visually shows the North Pole base layout.
 */
class WorldMapScreen(
    private var gameState: GameState,
    private val onGameStateChanged: (GameState) -> Unit
) : Screen {

    private var selectedDay = gameState.currentDay.coerceIn(1, 12)
    private var animationFrame = 0L

    override fun onEnter() {
        // Refresh state in case it changed
    }

    override fun update(deltaMs: Long) {
        animationFrame += deltaMs
    }

    override fun render(renderer: Renderer) {
        renderer.clear(Colors.PANEL_BG)

        // Title
        renderer.drawCenteredText(1, "═══ NORTH POLE BASE ═══", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)

        // Draw status bar
        drawStatusBar(renderer)

        // Draw the map
        drawMap(renderer)

        // Draw location info panel
        drawLocationInfo(renderer)

        // Controls
        renderer.drawCenteredText(
            renderer.height - 2,
            "[↑↓←→] Navigate   [Enter] Visit   [Esc] Back",
            Colors.GRAY
        )

        renderer.refresh()
    }

    override fun handleInput(key: KeyStroke): ScreenAction {
        when (key.keyType) {
            KeyType.ArrowUp -> {
                if (selectedDay < 12) selectedDay++
            }
            KeyType.ArrowDown -> {
                if (selectedDay > 1) selectedDay--
            }
            KeyType.ArrowLeft -> {
                if (selectedDay > 1) selectedDay--
            }
            KeyType.ArrowRight -> {
                if (selectedDay < 12) selectedDay++
            }
            KeyType.Enter -> {
                return tryEnterLocation()
            }
            KeyType.Escape -> {
                return ScreenAction.Pop
            }
            KeyType.Character -> {
                when (key.character) {
                    in '1'..'9' -> {
                        selectedDay = key.character.digitToInt()
                    }
                    '0' -> selectedDay = 10
                    'q', 'Q' -> return ScreenAction.Pop
                }
            }
            else -> {}
        }
        return ScreenAction.None
    }

    private fun tryEnterLocation(): ScreenAction {
        val location = Location.forDay(selectedDay) ?: return ScreenAction.None

        // Check if day is unlocked
        if (!gameState.isDayUnlocked(selectedDay)) {
            // TODO: Show "locked" message
            return ScreenAction.None
        }

        // Check if puzzle is implemented
        if (!location.isImplemented) {
            // TODO: Show "coming soon" message
            return ScreenAction.None
        }

        // Go to location/room screen
        return ScreenAction.Push(
            LocationScreen(selectedDay, location, gameState, onGameStateChanged)
        )
    }

    private fun drawStatusBar(renderer: Renderer) {
        val stars = gameState.totalStars
        val maxStars = 12 * 2 // 2 stars per day

        // Draw star progress
        val starText = "⭐ $stars / $maxStars"
        renderer.drawText(2, 1, starText, Colors.GOLD)

        // Draw unlocked days
        val unlockedText = "Days: ${gameState.unlockedDays.size}/12"
        renderer.drawText(renderer.width - unlockedText.length - 2, 1, unlockedText, Colors.ICE_BLUE)
    }

    private fun drawMap(renderer: Renderer) {
        // Map layout - vertical arrangement showing progression
        // Each day is a node, connected vertically

        val mapCenterX = renderer.width / 2
        val mapStartY = 5

        // Draw vertical path
        for (y in mapStartY until mapStartY + 24) {
            renderer.drawChar(mapCenterX, y, '│', Colors.DARK_GRAY)
        }

        // Draw each day location
        for (day in 1..12) {
            val y = mapStartY + (12 - day) * 2 // Day 1 at bottom, Day 12 at top
            val location = Location.forDay(day)
            val isUnlocked = gameState.isDayUnlocked(day)
            val isSelected = day == selectedDay
            val stars = gameState.getStars(day)

            // Choose display style based on state
            val (icon, fg, bg) = when {
                isSelected && isUnlocked -> Triple("►", Colors.GOLD, Colors.HIGHLIGHT)
                isSelected && !isUnlocked -> Triple("►", Colors.RED, Colors.HIGHLIGHT)
                isUnlocked && stars == 2 -> Triple("★", Colors.GOLD, Colors.PANEL_BG)
                isUnlocked && stars == 1 -> Triple("☆", Colors.YELLOW, Colors.PANEL_BG)
                isUnlocked -> Triple("○", Colors.WHITE, Colors.PANEL_BG)
                else -> Triple("🔒", Colors.DARK_GRAY, Colors.PANEL_BG)
            }

            // Draw node on map
            renderer.drawText(mapCenterX - 1, y, "─$icon─", fg, bg)

            // Draw day label
            val dayLabel = "Day %2d".format(day)
            val labelX = if (day % 2 == 1) mapCenterX - 15 else mapCenterX + 5
            renderer.drawText(labelX, y, dayLabel, if (isUnlocked) Colors.WHITE else Colors.DARK_GRAY)

            // Draw location name (shortened)
            val name = location?.displayName?.take(12) ?: "???"
            val nameColor = when {
                isSelected -> Colors.GOLD
                isUnlocked -> Colors.LIGHT_GRAY
                else -> Colors.DARK_GRAY
            }
            // Put name on alternate side for visual balance
            if (day % 2 == 0) {
                renderer.drawText(mapCenterX - name.length - 5, y, name, nameColor)
            } else {
                renderer.drawText(mapCenterX + 5, y, name, nameColor)
            }
        }

        // Draw entrance at bottom
        renderer.drawText(mapCenterX - 5, mapStartY + 25, "ENTRANCE", Colors.CHRISTMAS_GREEN, Colors.PANEL_BG, SGR.BOLD)
    }

    private fun drawLocationInfo(renderer: Renderer) {
        // Info panel on the right side
        val panelX = renderer.width - 35
        val panelY = 5
        val panelWidth = 33
        val panelHeight = 10

        // Draw panel box
        renderer.drawBox(panelX, panelY, panelWidth, panelHeight, Colors.BORDER, Colors.PANEL_BG)
        renderer.fillRect(panelX + 1, panelY + 1, panelWidth - 2, panelHeight - 2, ' ', Colors.WHITE, Colors.PANEL_BG.darken(0.8f))

        val location = Location.forDay(selectedDay)
        val isUnlocked = gameState.isDayUnlocked(selectedDay)

        // Title
        renderer.drawText(panelX + 2, panelY + 1, "Day $selectedDay", Colors.GOLD, Colors.PANEL_BG.darken(0.8f), SGR.BOLD)

        // Location name
        val name = location?.displayName ?: "Unknown"
        renderer.drawText(panelX + 2, panelY + 3, name, Colors.WHITE, Colors.PANEL_BG.darken(0.8f))

        // Description or status
        val desc = when {
            !isUnlocked -> "🔒 Complete Day ${selectedDay - 1} first"
            location?.isImplemented == false -> "🚧 Coming soon..."
            else -> location?.description ?: ""
        }

        // Word wrap description
        val maxWidth = panelWidth - 4
        val words = desc.split(" ")
        var line = ""
        var lineY = panelY + 5
        for (word in words) {
            if ((line + " " + word).length > maxWidth) {
                renderer.drawText(panelX + 2, lineY, line, Colors.LIGHT_GRAY, Colors.PANEL_BG.darken(0.8f))
                line = word
                lineY++
            } else {
                line = if (line.isEmpty()) word else "$line $word"
            }
        }
        if (line.isNotEmpty()) {
            renderer.drawText(panelX + 2, lineY, line, Colors.LIGHT_GRAY, Colors.PANEL_BG.darken(0.8f))
        }

        // Stars earned
        val stars = gameState.getStars(selectedDay)
        val starDisplay = "⭐".repeat(stars) + "☆".repeat(2 - stars)
        renderer.drawText(panelX + 2, panelY + panelHeight - 2, "Stars: $starDisplay", Colors.GOLD, Colors.PANEL_BG.darken(0.8f))
    }
}
