# Adventure Game

A terminal-based adventure game wrapper for Advent of Code 2025 puzzles.

## Overview

Navigate the North Pole base as a hero, exploring rooms, talking to elves, and solving puzzles presented as "dungeons" with animated visualizations.

## Running

```bash
./gradlew adventure
```

## Architecture

```
adventure/
├── AdventureGame.kt          # Main entry point & game loop
├── GameState.kt              # Save/load, Location enum
├── core/
│   ├── Screen.kt             # Screen interface & ScreenAction
│   ├── ScreenManager.kt      # Stack-based screen navigation
│   └── Renderer.kt           # Terminal rendering (colors, boxes, text)
├── screens/
│   ├── TitleScreen.kt        # Main menu with ASCII art
│   ├── WorldMapScreen.kt     # Location selection map
│   ├── LocationScreen.kt     # Room exploration
│   └── ResultScreen.kt       # Puzzle completion celebration
└── dungeons/
    ├── DungeonScreen.kt      # Base dungeon with animation system
    └── Day01-06DungeonPuzzle.kt  # Puzzle visualizations
```

## Game Flow

```
TitleScreen → WorldMapScreen → LocationScreen → DungeonScreen → ResultScreen
     ↑                              ↑                               │
     └──────────────────────────────┴───────────────────────────────┘
```

## Screens

### TitleScreen
- ASCII art title with twinkling star animation
- Menu: Continue / New Game / Quit
- Loads/creates game state

### WorldMapScreen
- Vertical map showing all 12 days
- Visual indicators: locked, unlocked, completed (with stars)
- Navigate with arrow keys, Enter to select

### LocationScreen
- Zelda-like room exploration
- Move player (@) with arrow keys
- Interact with objects (E) and NPCs (T)
- Enter dungeon door with Enter

### DungeonScreen
- Animated puzzle solving visualization
- Step-through controls (arrows, A for auto-play, S to skip)
- Progress bar and current value display
- Part 1 → Part 2 → Results flow

### ResultScreen
- Celebration with particle effects
- Stars earned display
- Part 1 and Part 2 answers

## Adding New Puzzles (Days 7-12)

1. **Create the puzzle file:**
   ```kotlin
   // dungeons/Day07DungeonPuzzle.kt
   class Day07DungeonPuzzle : DungeonPuzzle {
       override fun solvePart1(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
           // Your solution with onStep() calls for visualization
       }
       override fun solvePart2(input: List<String>, onStep: (AnimationStep) -> Unit): Long {
           // Part 2 solution
       }
   }
   ```

2. **Register in DungeonScreen.kt:**
   ```kotlin
   private fun createPuzzle(day: Int): DungeonPuzzle {
       return when (day) {
           // ... existing days ...
           7 -> Day07DungeonPuzzle()
           else -> PlaceholderPuzzle(day)
       }
   }
   ```

3. **Update Location enum in GameState.kt:**
   ```kotlin
   WORKSHOP_7(7, "New Location Name", "Description of the location", true),  // Change isImplemented to true
   ```

4. **Add room objects in LocationScreen.kt:**
   ```kotlin
   Location.WORKSHOP_7 -> {
       objects.add(RoomObject(25, 8, '?', "Object", "Description"))
   }
   ```

5. **Update NPC dialogue in LocationScreen.getElfDialogue():**
   ```kotlin
   Location.WORKSHOP_7 -> "Dialogue for day 7 puzzle"
   ```

## Animation System

Puzzles emit `AnimationStep` objects during solving:

```kotlin
data class AnimationStep(
    val description: String,      // What's happening
    val visualization: List<String>,  // ASCII art lines
    val currentValue: Long        // Running total/result
)
```

Example:
```kotlin
onStep(AnimationStep(
    "Processing item $i",
    listOf(
        "┌─────────────┐",
        "│ Value: $val │",
        "└─────────────┘"
    ),
    runningTotal
))
```

## Controls

| Screen | Key | Action |
|--------|-----|--------|
| All | Esc | Go back |
| Title | ↑↓ | Select menu option |
| Title | Enter | Confirm selection |
| WorldMap | ↑↓←→ | Navigate locations |
| WorldMap | Enter | Visit location |
| Location | ↑↓←→ / WASD | Move player |
| Location | E | Examine nearby object |
| Location | T | Talk to nearby NPC |
| Location | Enter | Enter dungeon (near door) |
| Dungeon | ←→ | Step through animation |
| Dungeon | A | Toggle auto-play |
| Dungeon | S | Skip to end |
| Dungeon | Enter | Continue to next phase |

## Save System

Game state is saved to `~/.aoc2025/save.json`:

```json
{
    "playerName": "Adventurer",
    "currentDay": 1,
    "unlockedDays": [1, 2, 3],
    "completedParts": {"1": 2, "2": 2},
    "dialogueFlags": []
}
```

Auto-saves after completing each dungeon.

## Terminal Requirements

**Minimum:**
- Terminal with ANSI escape code support
- Monospace font

**Recommended:**
- True color (24-bit) support
- Unicode font with box-drawing characters (─ │ ┌ ┐ └ ┘ etc.)
- Emoji support

**Tested terminals:**
- iTerm2 (macOS)
- Kitty
- WezTerm
- Windows Terminal
- VS Code integrated terminal

## Libraries

- **Lanterna 3.1.1** - Full-screen TUI, keyboard input
- **Mordant 2.7.2** - Styled text (available but not heavily used yet)
- **kotlinx-serialization 1.6.2** - JSON save/load

## World Map

```
                [??? - Day 12]
                      │
     [??? - Day 11]───┼───[??? - Day 10]
                      │
      [??? - Day 8]───┼───[??? - Day 9]
                      │
                [??? - Day 7]
                      │
[CAFETERIA - Day 5]───┼───[TRASH COMPACTOR - Day 6]
                      │
[GIFT SHOP - Day 2]───┼───[PRINTING DEPT - Day 4]
                      │
                [LOBBY - Day 3]
                      │
            [SECRET ENTRANCE - Day 1]
                      │
                [ENTRANCE - Hub]
```
