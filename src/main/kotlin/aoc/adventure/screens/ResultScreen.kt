package aoc.adventure.screens

import aoc.adventure.GameState
import aoc.adventure.core.*
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

/**
 * ResultScreen - shows puzzle completion with fanfare.
 *
 * Displays stars earned, answers for both parts, and
 * celebratory animation.
 */
class ResultScreen(
    private val day: Int,
    private val part1Result: Long,
    private val part2Result: Long,
    private val gameState: GameState,
    private val onGameStateChanged: (GameState) -> Unit
) : Screen {

    private var animationFrame = 0L
    private val particles = mutableListOf<Particle>()

    // Particle for celebration effect
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val char: Char,
        val color: Color,
        var life: Int
    )

    override fun onEnter() {
        // Create celebration particles
        repeat(30) {
            particles.add(createParticle())
        }
    }

    private fun createParticle(): Particle {
        val chars = listOf('✦', '✧', '⋆', '★', '☆', '·')
        val colors = listOf(Colors.GOLD, Colors.YELLOW, Colors.WHITE, Colors.CHRISTMAS_RED, Colors.CHRISTMAS_GREEN)
        return Particle(
            x = 40f + (Math.random() * 20 - 10).toFloat(),
            y = 10f,
            vx = (Math.random() * 4 - 2).toFloat(),
            vy = (Math.random() * -3 - 1).toFloat(),
            char = chars.random(),
            color = colors.random(),
            life = (50 + Math.random() * 50).toInt()
        )
    }

    override fun update(deltaMs: Long) {
        animationFrame += deltaMs

        // Update particles
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * 0.1f
            p.y += p.vy * 0.1f
            p.vy += 0.05f // Gravity
            p.life--
            if (p.life <= 0) {
                iterator.remove()
            }
        }

        // Respawn particles
        while (particles.size < 20) {
            particles.add(createParticle())
        }
    }

    override fun render(renderer: Renderer) {
        renderer.clear(Colors.PANEL_BG)

        // Draw particles
        for (p in particles) {
            if (p.x >= 0 && p.x < renderer.width && p.y >= 0 && p.y < renderer.height) {
                val alpha = p.life / 100f
                val color = p.color.darken(0.5f + alpha * 0.5f)
                renderer.drawChar(p.x.toInt(), p.y.toInt(), p.char, color, Colors.PANEL_BG)
            }
        }

        val centerY = renderer.height / 2 - 6

        // Banner
        renderer.drawBox(
            renderer.width / 2 - 20,
            centerY - 2,
            40,
            16,
            Colors.GOLD,
            Colors.PANEL_BG,
            BoxStyle.DOUBLE
        )

        // Title
        renderer.drawCenteredText(centerY, "✨ DAY $day COMPLETE! ✨", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)

        // Stars
        val stars = gameState.getStars(day)
        val starLine = when (stars) {
            2 -> "⭐ ⭐"
            1 -> "⭐ ☆"
            else -> "☆ ☆"
        }
        renderer.drawCenteredText(centerY + 2, starLine, Colors.GOLD)

        // Results
        renderer.drawCenteredText(centerY + 5, "Part 1:", Colors.WHITE)
        renderer.drawCenteredText(centerY + 6, "$part1Result", Colors.CYAN, Colors.PANEL_BG, SGR.BOLD)

        renderer.drawCenteredText(centerY + 8, "Part 2:", Colors.WHITE)
        renderer.drawCenteredText(centerY + 9, "$part2Result", Colors.CYAN, Colors.PANEL_BG, SGR.BOLD)

        // Unlock message if applicable
        if (day < 12) {
            renderer.drawCenteredText(centerY + 12, "Day ${day + 1} unlocked!", Colors.CHRISTMAS_GREEN)
        }

        // Continue prompt
        val blink = (animationFrame / 500) % 2 == 0L
        if (blink) {
            renderer.drawCenteredText(renderer.height - 4, "Press [Enter] to continue", Colors.WHITE)
        }

        renderer.refresh()
    }

    override fun handleInput(key: KeyStroke): ScreenAction {
        when (key.keyType) {
            KeyType.Enter, KeyType.Escape -> {
                // Pop back to location, then pop to world map
                return ScreenAction.Pop
            }
            KeyType.Character -> {
                if (key.character == ' ' || key.character?.lowercaseChar() == 'q') {
                    return ScreenAction.Pop
                }
            }
            else -> {}
        }
        return ScreenAction.None
    }
}
