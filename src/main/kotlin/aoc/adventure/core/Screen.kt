package aoc.adventure.core

import com.googlecode.lanterna.input.KeyStroke

/**
 * Screen interface - all game screens implement this.
 *
 * Screens handle their own rendering and input processing.
 * The ScreenManager maintains a stack of screens and delegates
 * to the topmost screen.
 */
interface Screen {
    /**
     * Called when this screen becomes the active screen.
     * Use for initialization that needs to happen each time
     * the screen is shown (not just first creation).
     */
    fun onEnter() {}

    /**
     * Called when this screen is no longer the active screen
     * (either popped or another screen pushed on top).
     */
    fun onExit() {}

    /**
     * Update game logic. Called each frame before render.
     * @param deltaMs milliseconds since last update
     */
    fun update(deltaMs: Long)

    /**
     * Render this screen to the terminal.
     * @param renderer the renderer to draw with
     */
    fun render(renderer: Renderer)

    /**
     * Handle keyboard input.
     * @param key the key that was pressed
     * @return ScreenAction indicating what to do next
     */
    fun handleInput(key: KeyStroke): ScreenAction
}

/**
 * Actions that a screen can request after handling input.
 */
sealed class ScreenAction {
    /** Stay on current screen, continue normal operation */
    object None : ScreenAction()

    /** Pop this screen off the stack (go back) */
    object Pop : ScreenAction()

    /** Push a new screen onto the stack */
    data class Push(val screen: Screen) : ScreenAction()

    /** Replace current screen with a new one */
    data class Replace(val screen: Screen) : ScreenAction()

    /** Quit the game entirely */
    object Quit : ScreenAction()
}
