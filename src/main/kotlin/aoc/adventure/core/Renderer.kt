package aoc.adventure.core

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.Screen

/**
 * Renderer - wraps Lanterna's TextGraphics with convenient drawing methods.
 *
 * Provides higher-level drawing operations for the game:
 * - Text with colors and styles
 * - Boxes and borders
 * - Centered text
 * - Progress bars
 *
 * Uses true color (24-bit RGB) for modern terminals.
 */
class Renderer(private val screen: Screen) {
    private val graphics: TextGraphics = screen.newTextGraphics()

    /** Terminal width in characters */
    val width: Int
        get() = screen.terminalSize.columns

    /** Terminal height in characters */
    val height: Int
        get() = screen.terminalSize.rows

    /** Terminal size */
    val size: TerminalSize
        get() = screen.terminalSize

    /**
     * Clear the entire screen with a background color.
     */
    fun clear(bg: Color = Colors.BLACK) {
        graphics.backgroundColor = bg.toTextColor()
        graphics.fill(' ')
    }

    /**
     * Draw text at a position with optional colors and styles.
     */
    fun drawText(
        x: Int,
        y: Int,
        text: String,
        fg: Color = Colors.WHITE,
        bg: Color = Colors.BLACK,
        vararg styles: SGR
    ) {
        graphics.foregroundColor = fg.toTextColor()
        graphics.backgroundColor = bg.toTextColor()
        if (styles.isNotEmpty()) {
            graphics.enableModifiers(*styles)
        }
        graphics.putString(x, y, text)
        if (styles.isNotEmpty()) {
            graphics.disableModifiers(*styles)
        }
    }

    /**
     * Draw text centered horizontally at a given y position.
     */
    fun drawCenteredText(
        y: Int,
        text: String,
        fg: Color = Colors.WHITE,
        bg: Color = Colors.BLACK,
        vararg styles: SGR
    ) {
        val x = (width - text.length) / 2
        drawText(x, y, text, fg, bg, *styles)
    }

    /**
     * Draw a box border using Unicode box-drawing characters.
     */
    fun drawBox(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        fg: Color = Colors.WHITE,
        bg: Color = Colors.BLACK,
        style: BoxStyle = BoxStyle.DOUBLE
    ) {
        graphics.foregroundColor = fg.toTextColor()
        graphics.backgroundColor = bg.toTextColor()

        val chars = style.chars

        // Corners
        graphics.setCharacter(x, y, chars.topLeft)
        graphics.setCharacter(x + w - 1, y, chars.topRight)
        graphics.setCharacter(x, y + h - 1, chars.bottomLeft)
        graphics.setCharacter(x + w - 1, y + h - 1, chars.bottomRight)

        // Horizontal edges
        for (i in 1 until w - 1) {
            graphics.setCharacter(x + i, y, chars.horizontal)
            graphics.setCharacter(x + i, y + h - 1, chars.horizontal)
        }

        // Vertical edges
        for (i in 1 until h - 1) {
            graphics.setCharacter(x, y + i, chars.vertical)
            graphics.setCharacter(x + w - 1, y + i, chars.vertical)
        }
    }

    /**
     * Draw a filled rectangle.
     */
    fun fillRect(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        char: Char = ' ',
        fg: Color = Colors.WHITE,
        bg: Color = Colors.BLACK
    ) {
        graphics.foregroundColor = fg.toTextColor()
        graphics.backgroundColor = bg.toTextColor()
        for (row in y until y + h) {
            for (col in x until x + w) {
                graphics.setCharacter(col, row, char)
            }
        }
    }

    /**
     * Draw a horizontal progress bar.
     */
    fun drawProgressBar(
        x: Int,
        y: Int,
        width: Int,
        progress: Float, // 0.0 to 1.0
        fg: Color = Colors.GREEN,
        bg: Color = Colors.DARK_GRAY,
        filledChar: Char = '█',
        emptyChar: Char = '░'
    ) {
        val filled = (width * progress.coerceIn(0f, 1f)).toInt()
        graphics.foregroundColor = fg.toTextColor()
        graphics.backgroundColor = bg.toTextColor()

        for (i in 0 until width) {
            val char = if (i < filled) filledChar else emptyChar
            graphics.setCharacter(x + i, y, char)
        }
    }

    /**
     * Draw a single character at a position.
     */
    fun drawChar(
        x: Int,
        y: Int,
        char: Char,
        fg: Color = Colors.WHITE,
        bg: Color = Colors.BLACK
    ) {
        graphics.foregroundColor = fg.toTextColor()
        graphics.backgroundColor = bg.toTextColor()
        graphics.setCharacter(x, y, char)
    }

    /**
     * Refresh the screen to show all drawn content.
     */
    fun refresh() {
        screen.refresh()
    }
}

/**
 * Color - represents an RGB color.
 * Uses true color (24-bit) for modern terminal support.
 */
data class Color(val r: Int, val g: Int, val b: Int) {
    fun toTextColor(): TextColor = TextColor.RGB(r, g, b)

    /** Darken the color by a factor (0.0 = black, 1.0 = unchanged) */
    fun darken(factor: Float): Color = Color(
        (r * factor).toInt().coerceIn(0, 255),
        (g * factor).toInt().coerceIn(0, 255),
        (b * factor).toInt().coerceIn(0, 255)
    )

    /** Lighten the color by a factor */
    fun lighten(factor: Float): Color = Color(
        (r + (255 - r) * factor).toInt().coerceIn(0, 255),
        (g + (255 - g) * factor).toInt().coerceIn(0, 255),
        (b + (255 - b) * factor).toInt().coerceIn(0, 255)
    )
}

/**
 * Common color palette for the game.
 */
object Colors {
    val BLACK = Color(0, 0, 0)
    val WHITE = Color(255, 255, 255)
    val RED = Color(255, 0, 0)
    val GREEN = Color(0, 255, 0)
    val BLUE = Color(0, 0, 255)
    val YELLOW = Color(255, 255, 0)
    val CYAN = Color(0, 255, 255)
    val MAGENTA = Color(255, 0, 255)

    // Grays
    val DARK_GRAY = Color(64, 64, 64)
    val GRAY = Color(128, 128, 128)
    val LIGHT_GRAY = Color(192, 192, 192)

    // Christmas theme
    val CHRISTMAS_RED = Color(200, 30, 30)
    val CHRISTMAS_GREEN = Color(0, 150, 50)
    val GOLD = Color(255, 215, 0)
    val SNOW = Color(240, 248, 255)
    val ICE_BLUE = Color(135, 206, 235)

    // UI colors
    val PANEL_BG = Color(20, 20, 40)
    val HIGHLIGHT = Color(80, 80, 120)
    val BORDER = Color(100, 100, 140)
}

/**
 * Box drawing character sets.
 */
enum class BoxStyle(val chars: BoxChars) {
    SINGLE(BoxChars('┌', '┐', '└', '┘', '─', '│')),
    DOUBLE(BoxChars('╔', '╗', '╚', '╝', '═', '║')),
    ROUNDED(BoxChars('╭', '╮', '╰', '╯', '─', '│')),
    HEAVY(BoxChars('┏', '┓', '┗', '┛', '━', '┃'))
}

data class BoxChars(
    val topLeft: Char,
    val topRight: Char,
    val bottomLeft: Char,
    val bottomRight: Char,
    val horizontal: Char,
    val vertical: Char
)
