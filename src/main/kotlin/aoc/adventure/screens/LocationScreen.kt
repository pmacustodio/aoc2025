package aoc.adventure.screens

import aoc.adventure.GameState
import aoc.adventure.Location
import aoc.adventure.core.*
import aoc.adventure.dungeons.DungeonScreen
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

/**
 * LocationScreen - room exploration for a specific location.
 *
 * Players can walk around, examine objects, talk to NPCs,
 * and enter the dungeon when ready.
 */
class LocationScreen(
    private val day: Int,
    private val location: Location,
    private var gameState: GameState,
    private val onGameStateChanged: (GameState) -> Unit
) : Screen {

    // Player position in room (character grid)
    private var playerX = 20
    private var playerY = 10

    // Room dimensions
    private val roomWidth = 50
    private val roomHeight = 18
    private val roomOffsetX = 5
    private val roomOffsetY = 3

    // Animation state
    private var animationFrame = 0L

    // Room objects (x, y, char, name, description)
    private val objects = mutableListOf<RoomObject>()

    // NPC (elf) position
    private var elfX = 35
    private var elfY = 8

    // Dungeon door position
    private var doorX = 24
    private var doorY = 1

    // Message to display
    private var message: String? = null
    private var messageTime = 0L

    init {
        setupRoom()
    }

    private fun setupRoom() {
        // Add some decorative objects based on location
        when (location) {
            Location.SECRET_ENTRANCE -> {
                objects.add(RoomObject(25, 5, '◎', "Safe", "A massive vault safe with a spinning dial"))
                objects.add(RoomObject(10, 8, '◘', "Crate", "A dusty wooden crate"))
            }
            Location.GIFT_SHOP -> {
                objects.add(RoomObject(10, 5, '▤', "Shelf", "Shelves filled with products"))
                objects.add(RoomObject(40, 5, '▤', "Shelf", "More shelves of merchandise"))
                objects.add(RoomObject(25, 10, '▦', "Counter", "The checkout counter"))
            }
            Location.LOBBY -> {
                objects.add(RoomObject(15, 6, '▓', "Battery Bank", "Humming banks of batteries"))
                objects.add(RoomObject(35, 6, '▓', "Battery Bank", "More power storage"))
            }
            Location.PRINTING_DEPT -> {
                objects.add(RoomObject(12, 7, '◙', "Paper Stack", "Towering stack of paper rolls"))
                objects.add(RoomObject(38, 7, '◙', "Paper Stack", "Another tall stack"))
                objects.add(RoomObject(25, 12, '▣', "Forklift", "A small forklift for moving rolls"))
            }
            Location.CAFETERIA -> {
                objects.add(RoomObject(10, 6, '▥', "Conveyor", "An ingredient conveyor belt"))
                objects.add(RoomObject(25, 9, '◈', "Table", "A dining table"))
                objects.add(RoomObject(40, 6, '▤', "Pantry", "The pantry shelves"))
            }
            Location.TRASH_COMPACTOR -> {
                objects.add(RoomObject(12, 7, '▧', "Compactor", "The trash compactor mechanism"))
                objects.add(RoomObject(38, 7, '▨', "Control Panel", "Control panel with worksheet"))
            }
            else -> {
                // Placeholder rooms
                objects.add(RoomObject(25, 8, '?', "???", "Something mysterious..."))
            }
        }
    }

    override fun onEnter() {
        playerX = 20
        playerY = 15
    }

    override fun update(deltaMs: Long) {
        animationFrame += deltaMs

        // Clear message after 3 seconds
        if (message != null && animationFrame - messageTime > 3000) {
            message = null
        }
    }

    override fun render(renderer: Renderer) {
        renderer.clear(Colors.PANEL_BG)

        // Title bar
        renderer.drawCenteredText(1, "═══ ${location.displayName.uppercase()} ═══", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)

        // Draw room
        drawRoom(renderer)

        // Draw objects
        drawObjects(renderer)

        // Draw NPC (elf)
        drawElf(renderer)

        // Draw door
        drawDoor(renderer)

        // Draw player
        drawPlayer(renderer)

        // Draw message if any
        drawMessage(renderer)

        // Draw info panel
        drawInfoPanel(renderer)

        // Controls
        renderer.drawCenteredText(
            renderer.height - 2,
            "[↑↓←→] Walk   [E] Examine   [T] Talk   [Enter] Door   [Esc] Map",
            Colors.GRAY
        )

        renderer.refresh()
    }

    override fun handleInput(key: KeyStroke): ScreenAction {
        when (key.keyType) {
            KeyType.ArrowUp -> movePlayer(0, -1)
            KeyType.ArrowDown -> movePlayer(0, 1)
            KeyType.ArrowLeft -> movePlayer(-1, 0)
            KeyType.ArrowRight -> movePlayer(1, 0)
            KeyType.Enter -> {
                // Try to enter dungeon if near door
                if (isNear(playerX, playerY, doorX, doorY, 2)) {
                    return ScreenAction.Push(
                        DungeonScreen(day, location, gameState, onGameStateChanged)
                    )
                }
            }
            KeyType.Escape -> return ScreenAction.Pop
            KeyType.Character -> {
                when (key.character?.lowercaseChar()) {
                    'e' -> examine()
                    't' -> talk()
                    'q' -> return ScreenAction.Pop
                    'w' -> movePlayer(0, -1)
                    'a' -> movePlayer(-1, 0)
                    's' -> movePlayer(0, 1)
                    'd' -> movePlayer(1, 0)
                }
            }
            else -> {}
        }
        return ScreenAction.None
    }

    private fun movePlayer(dx: Int, dy: Int) {
        val newX = playerX + dx
        val newY = playerY + dy

        // Check bounds
        if (newX < 1 || newX >= roomWidth - 1) return
        if (newY < 1 || newY >= roomHeight - 1) return

        // Check collision with objects
        for (obj in objects) {
            if (newX == obj.x && newY == obj.y) return
        }

        // Check collision with elf
        if (newX == elfX && newY == elfY) return

        playerX = newX
        playerY = newY
    }

    private fun examine() {
        // Find nearby object
        for (obj in objects) {
            if (isNear(playerX, playerY, obj.x, obj.y, 2)) {
                showMessage("${obj.name}: ${obj.description}")
                return
            }
        }

        // Near door?
        if (isNear(playerX, playerY, doorX, doorY, 2)) {
            showMessage("The dungeon entrance. Press Enter to enter.")
            return
        }

        showMessage("Nothing interesting nearby.")
    }

    private fun talk() {
        if (isNear(playerX, playerY, elfX, elfY, 3)) {
            // Show dialogue based on location/day
            val dialogue = getElfDialogue()
            showMessage("Elf: \"$dialogue\"")
        } else {
            showMessage("No one nearby to talk to.")
        }
    }

    private fun getElfDialogue(): String {
        return when (location) {
            Location.SECRET_ENTRANCE ->
                "Welcome! This safe has a tricky dial mechanism. Can you crack the code?"
            Location.GIFT_SHOP ->
                "Some product IDs in our database are invalid. They're repeated patterns!"
            Location.LOBBY ->
                "We need to select digits from these batteries to form the largest number."
            Location.PRINTING_DEPT ->
                "The forklift can only reach paper rolls on the edges of the stacks."
            Location.CAFETERIA ->
                "The fresh ingredients each have a valid ID range. Check if IDs are fresh!"
            Location.TRASH_COMPACTOR ->
                "Help! I fell in here. Could you solve this worksheet while we wait?"
            else ->
                "This area isn't ready yet. Check back later!"
        }
    }

    private fun isNear(x1: Int, y1: Int, x2: Int, y2: Int, distance: Int): Boolean {
        return kotlin.math.abs(x1 - x2) <= distance && kotlin.math.abs(y1 - y2) <= distance
    }

    private fun showMessage(msg: String) {
        message = msg
        messageTime = animationFrame
    }

    private fun drawRoom(renderer: Renderer) {
        // Draw room border
        renderer.drawBox(
            roomOffsetX,
            roomOffsetY,
            roomWidth,
            roomHeight,
            Colors.BORDER,
            Colors.PANEL_BG,
            BoxStyle.DOUBLE
        )

        // Fill room floor
        renderer.fillRect(
            roomOffsetX + 1,
            roomOffsetY + 1,
            roomWidth - 2,
            roomHeight - 2,
            '·',
            Colors.DARK_GRAY,
            Color(15, 15, 25)
        )
    }

    private fun drawObjects(renderer: Renderer) {
        for (obj in objects) {
            renderer.drawChar(
                roomOffsetX + obj.x,
                roomOffsetY + obj.y,
                obj.char,
                Colors.LIGHT_GRAY,
                Color(15, 15, 25)
            )
        }
    }

    private fun drawElf(renderer: Renderer) {
        // Animated elf
        val bounce = if ((animationFrame / 500) % 2 == 0L) 0 else 0
        renderer.drawText(
            roomOffsetX + elfX - 1,
            roomOffsetY + elfY + bounce,
            "🧝",
            Colors.CHRISTMAS_GREEN,
            Color(15, 15, 25)
        )
    }

    private fun drawDoor(renderer: Renderer) {
        // Dungeon door at top
        val doorColor = if ((animationFrame / 300) % 2 == 0L) Colors.GOLD else Colors.YELLOW
        renderer.drawText(
            roomOffsetX + doorX - 2,
            roomOffsetY + doorY,
            "【🚪】",
            doorColor,
            Color(15, 15, 25)
        )
    }

    private fun drawPlayer(renderer: Renderer) {
        renderer.drawChar(
            roomOffsetX + playerX,
            roomOffsetY + playerY,
            '@',
            Colors.CYAN,
            Color(15, 15, 25)
        )
    }

    private fun drawMessage(renderer: Renderer) {
        message?.let { msg ->
            val boxY = renderer.height - 6
            val boxWidth = minOf(msg.length + 4, renderer.width - 10)
            val boxX = (renderer.width - boxWidth) / 2

            renderer.drawBox(boxX, boxY, boxWidth, 3, Colors.GOLD, Colors.PANEL_BG)
            renderer.fillRect(boxX + 1, boxY + 1, boxWidth - 2, 1, ' ', Colors.WHITE, Colors.PANEL_BG)
            renderer.drawText(boxX + 2, boxY + 1, msg.take(boxWidth - 4), Colors.WHITE, Colors.PANEL_BG)
        }
    }

    private fun drawInfoPanel(renderer: Renderer) {
        // Right side info
        val panelX = roomOffsetX + roomWidth + 3
        val panelY = roomOffsetY

        renderer.drawText(panelX, panelY, "Day $day", Colors.GOLD, Colors.PANEL_BG, SGR.BOLD)

        val stars = gameState.getStars(day)
        val starDisplay = "⭐".repeat(stars) + "☆".repeat(2 - stars)
        renderer.drawText(panelX, panelY + 2, starDisplay, Colors.GOLD)

        renderer.drawText(panelX, panelY + 4, "Nearby:", Colors.WHITE)

        // Show what's nearby
        var infoY = panelY + 5
        if (isNear(playerX, playerY, elfX, elfY, 3)) {
            renderer.drawText(panelX, infoY++, "  🧝 Elf", Colors.LIGHT_GRAY)
        }
        if (isNear(playerX, playerY, doorX, doorY, 2)) {
            renderer.drawText(panelX, infoY++, "  🚪 Door", Colors.LIGHT_GRAY)
        }
        for (obj in objects) {
            if (isNear(playerX, playerY, obj.x, obj.y, 2)) {
                renderer.drawText(panelX, infoY++, "  ${obj.char} ${obj.name}", Colors.LIGHT_GRAY)
            }
        }
    }
}

/**
 * An object in a room that can be examined.
 */
data class RoomObject(
    val x: Int,
    val y: Int,
    val char: Char,
    val name: String,
    val description: String
)
