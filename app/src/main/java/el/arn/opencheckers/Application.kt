package el.arn.opencheckers

import el.arn.opencheckers.checkers_game.game_core.Game
import el.arn.opencheckers.checkers_game.game_core.implementations.GameImpl

class Application : android.app.Application() {

    companion object {
        var counter = 1;
    }

    override fun onCreate() {
        super.onCreate()

        counter += 4

    }

//    var checkersGame: Game = GameImpl()

}