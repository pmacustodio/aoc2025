# Advent of Code 2025

Kotlin solutions for [Advent of Code 2025](https://adventofcode.com/2025).

## Project Structure

```
aoc2025/
├── src/main/
│   ├── kotlin/aoc/
│   │   ├── days/            # Solution files (Day01.kt, Day02.kt, ...)
│   │   └── adventure/       # TUI Adventure Game
│   └── resources/
│       ├── inputs/          # Puzzle inputs (day01.txt, day02.txt, ...)
│       └── puzzles/         # Puzzle descriptions
├── build.gradle.kts         # Gradle build configuration
└── settings.gradle.kts
```

## Requirements

- JDK 21+
- Gradle 9.0+

## Running Solutions

Each day has its own Gradle task:

```bash
./gradlew day01    # Run Day 1
./gradlew day02    # Run Day 2
# ... and so on up to day12
```

## Solution Format

Each day's solution follows this pattern:

```kotlin
package aoc.days

/*
 * Day X: Title
 *
 * Solution rationale explaining the approach for both parts.
 */

fun main() {
    val input = File("src/main/resources/inputs/dayXX.txt").readLines()
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}

fun part1(input: List<String>): Int { /* ... */ }
fun part2(input: List<String>): Int { /* ... */ }
```

Each solution file includes a comment block at the top explaining the problem and the rationale behind the solution approach.

## Adventure Game

In addition to the standard solutions, this project includes a terminal-based adventure game that wraps the puzzles in an explorable world.

### Features

- **Zelda-like exploration** - Walk around rooms, talk to NPCs, examine objects
- **Animated puzzle visualizations** - Watch the solutions unfold step-by-step
- **Progressive unlock** - Complete Day N to unlock Day N+1
- **Save/load progress** - Game state saved to `~/.aoc2025/save.json`
- **Modern terminal support** - True color, Unicode, box-drawing characters

### Running the Adventure

```bash
./gradlew adventure
```

### Controls

| Key | Action |
|-----|--------|
| Arrow keys / WASD | Move |
| E | Examine nearby object |
| T | Talk to NPC |
| Enter | Enter dungeon / Confirm |
| Esc | Go back / Exit |

### Terminal Requirements

For the best experience, use a modern terminal with:
- True color (24-bit) support
- Unicode font with box-drawing characters
- Recommended: iTerm2, Kitty, WezTerm, Windows Terminal

See [adventure/README.md](src/main/kotlin/aoc/adventure/README.md) for detailed documentation.
