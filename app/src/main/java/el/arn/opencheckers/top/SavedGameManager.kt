package el.arn.opencheckers.top

import android.content.Context
import el.arn.opencheckers.checkers_game.game_core.Game
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


class SavedGameManager(private val context: Context) {

    /** @return true if successful, false otherwise*/
    fun saveGame(game: Game): Boolean = putGameToMemory(game)
    /** @return true if successful, false otherwise*/
    fun clearGame(): Boolean = putGameToMemory(null)
    fun loadGame(): Game? = readGameFromMemory()

    private fun readGameFromMemory(): Game? {
        var game: Game?

        var fileInputStream: FileInputStream? = null
        var objectInputStream: ObjectInputStream? = null
        try {
            fileInputStream = context.openFileInput("openCheckersSavedGame")
            objectInputStream = ObjectInputStream(fileInputStream)
            game = objectInputStream.readObject() as Game
        } catch (e: Exception) {
            game = null
        } finally {
            try { objectInputStream?.close() } catch (e: Exception) {}
            try { fileInputStream?.close() } catch (e: Exception) {}
        }

        return game
    }

    private fun putGameToMemory(game: Game?): Boolean {
        var successful = true
        var fileOutputStream: FileOutputStream? = null
        var objectOutputStream: ObjectOutputStream? = null
        try {
            fileOutputStream = context.openFileOutput("openCheckersSavedGame", Context.MODE_PRIVATE)
            objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(game)
        } catch (e: Exception) {
            successful = false
        } finally {
            try { objectOutputStream?.close() } catch (e: Exception) {}
            try { fileOutputStream?.close() } catch (e: Exception) {}
        }
        return successful
    }

}