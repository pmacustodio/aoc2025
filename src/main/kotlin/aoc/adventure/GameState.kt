package aoc.adventure

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * GameState - persistent state for the adventure game.
 *
 * Tracks player progress including:
 * - Which days are unlocked (complete day N to unlock day N+1)
 * - Stars earned per puzzle (2 stars per day, like AoC)
 * - Current location for resuming
 *
 * Saves to ~/.aoc2025/save.json for persistence across sessions.
 */
@Serializable
data class GameState(
    val playerName: String = "Adventurer",
    val currentDay: Int = 1,
    val unlockedDays: Set<Int> = setOf(1),      // Days the player can access
    val completedParts: Map<Int, Int> = emptyMap(), // Day -> parts completed (1 or 2)
    val dialogueFlags: Set<String> = emptySet(), // Track conversation progress
) {
    /** Total stars earned (2 per day max) */
    val totalStars: Int
        get() = completedParts.values.sum()

    /** Check if a day is unlocked */
    fun isDayUnlocked(day: Int): Boolean = day in unlockedDays

    /** Check if a day is fully completed (both parts) */
    fun isDayCompleted(day: Int): Boolean = (completedParts[day] ?: 0) >= 2

    /** Get stars for a specific day */
    fun getStars(day: Int): Int = completedParts[day] ?: 0

    /**
     * Complete a part of a day's puzzle.
     * Returns a new GameState with updated progress.
     */
    fun completePart(day: Int, part: Int): GameState {
        val currentParts = completedParts[day] ?: 0
        val newParts = maxOf(currentParts, part)

        // Unlock next day if both parts completed
        val newUnlocked = if (newParts >= 2 && day < 12) {
            unlockedDays + (day + 1)
        } else {
            unlockedDays
        }

        return copy(
            completedParts = completedParts + (day to newParts),
            unlockedDays = newUnlocked
        )
    }

    /**
     * Set a dialogue flag (for tracking conversation branches).
     */
    fun setDialogueFlag(flag: String): GameState =
        copy(dialogueFlags = dialogueFlags + flag)

    /**
     * Check if a dialogue flag is set.
     */
    fun hasDialogueFlag(flag: String): Boolean = flag in dialogueFlags

    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true // Forward compatibility
        }

        private val saveDir = File(System.getProperty("user.home"), ".aoc2025")
        private val saveFile = File(saveDir, "save.json")

        /**
         * Load game state from disk, or create default if none exists.
         */
        fun load(): GameState {
            return try {
                if (saveFile.exists()) {
                    json.decodeFromString<GameState>(saveFile.readText())
                } else {
                    GameState()
                }
            } catch (e: Exception) {
                // If save is corrupted, start fresh
                println("Warning: Could not load save file, starting fresh")
                GameState()
            }
        }

        /**
         * Save game state to disk.
         */
        fun save(state: GameState) {
            try {
                saveDir.mkdirs()
                saveFile.writeText(json.encodeToString(state))
            } catch (e: Exception) {
                println("Warning: Could not save game: ${e.message}")
            }
        }

        /**
         * Delete the save file (for "New Game" option).
         */
        fun deleteSave() {
            try {
                if (saveFile.exists()) {
                    saveFile.delete()
                }
            } catch (e: Exception) {
                println("Warning: Could not delete save file: ${e.message}")
            }
        }

        /**
         * Check if a save file exists.
         */
        fun saveExists(): Boolean = saveFile.exists()
    }
}

/**
 * Location - represents a location on the world map.
 *
 * Each location corresponds to one day's puzzle.
 * Days 1-6 have defined names and descriptions.
 * Days 7-12 are placeholders until puzzles are released.
 */
enum class Location(
    val day: Int,
    val displayName: String,
    val description: String,
    val isImplemented: Boolean = false
) {
    // Hub - always accessible
    ENTRANCE(0, "North Pole Entrance", "The main entrance to the North Pole facility", true),

    // Day 1-6: Implemented puzzles
    SECRET_ENTRANCE(1, "Secret Entrance", "A mysterious safe with a spinning dial", true),
    GIFT_SHOP(2, "Gift Shop", "Shelves of products with suspicious ID numbers", true),
    LOBBY(3, "Lobby", "Banks of batteries humming with energy", true),
    PRINTING_DEPT(4, "Printing Dept", "Towering stacks of paper rolls", true),
    CAFETERIA(5, "Cafeteria", "Fresh ingredients organized in ranges", true),
    TRASH_COMPACTOR(6, "Trash Compactor", "A worksheet of math problems awaits", true),

    // Day 7-12: Placeholders (not implemented yet)
    WORKSHOP_7(7, "??? (Day 7)", "Coming soon...", false),
    WORKSHOP_8(8, "??? (Day 8)", "Coming soon...", false),
    WORKSHOP_9(9, "??? (Day 9)", "Coming soon...", false),
    WORKSHOP_10(10, "??? (Day 10)", "Coming soon...", false),
    WORKSHOP_11(11, "??? (Day 11)", "Coming soon...", false),
    WORKSHOP_12(12, "??? (Day 12)", "Coming soon...", false);

    companion object {
        /** Get location by day number */
        fun forDay(day: Int): Location? = entries.find { it.day == day }

        /** Get all locations that are actually implemented */
        fun implemented(): List<Location> = entries.filter { it.isImplemented && it.day > 0 }
    }
}
