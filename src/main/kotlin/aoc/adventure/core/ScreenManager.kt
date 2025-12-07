package aoc.adventure.core

import com.googlecode.lanterna.input.KeyStroke
import java.util.ArrayDeque

/**
 * ScreenManager - manages a stack of screens.
 *
 * The topmost screen is the active one that receives updates,
 * renders, and handles input. Screens can push new screens on top,
 * pop themselves off, or replace themselves.
 *
 * This pattern allows for natural navigation:
 * - TitleScreen -> push WorldMap -> push Room -> push Dungeon
 * - Pressing "back" pops the stack, returning to the previous screen
 */
class ScreenManager {
    private val screenStack = ArrayDeque<Screen>()

    /** The currently active screen (top of stack), or null if empty */
    val currentScreen: Screen?
        get() = screenStack.peekFirst()

    /** True if there are no screens (game should exit) */
    val isEmpty: Boolean
        get() = screenStack.isEmpty()

    /**
     * Push a new screen onto the stack.
     * The current screen's onExit is called, then the new screen's onEnter.
     */
    fun push(screen: Screen) {
        currentScreen?.onExit()
        screenStack.push(screen)
        screen.onEnter()
    }

    /**
     * Pop the current screen off the stack.
     * Returns to the previous screen (if any).
     */
    fun pop() {
        if (screenStack.isNotEmpty()) {
            val popped = screenStack.pop()
            popped.onExit()
            currentScreen?.onEnter()
        }
    }

    /**
     * Replace the current screen with a new one.
     * Useful for transitions where you don't want to go back.
     */
    fun replace(screen: Screen) {
        if (screenStack.isNotEmpty()) {
            val old = screenStack.pop()
            old.onExit()
        }
        screenStack.push(screen)
        screen.onEnter()
    }

    /**
     * Clear all screens from the stack.
     */
    fun clear() {
        while (screenStack.isNotEmpty()) {
            val screen = screenStack.pop()
            screen.onExit()
        }
    }

    /**
     * Update the current screen.
     */
    fun update(deltaMs: Long) {
        currentScreen?.update(deltaMs)
    }

    /**
     * Render the current screen.
     */
    fun render(renderer: Renderer) {
        currentScreen?.render(renderer)
    }

    /**
     * Handle input and process the resulting action.
     * @return true if the game should continue, false if it should quit
     */
    fun handleInput(key: KeyStroke): Boolean {
        val screen = currentScreen ?: return false
        return when (val action = screen.handleInput(key)) {
            is ScreenAction.None -> true
            is ScreenAction.Pop -> {
                pop()
                true
            }
            is ScreenAction.Push -> {
                push(action.screen)
                true
            }
            is ScreenAction.Replace -> {
                replace(action.screen)
                true
            }
            is ScreenAction.Quit -> false
        }
    }
}
