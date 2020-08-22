package el.arn.checkers.game.game_core.checkers_game;

import el.arn.checkers.game.game_core.checkers_game.structs.Player;

public interface GameLogicListener {
    void gameHasEnded(Player winner);
}
