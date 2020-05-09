package el.arn.opencheckers.checkers_game.game_core;

import el.arn.opencheckers.checkers_game.game_core.structs.Player;

public interface GameLogicDelegate {
    void gameHasEnded(Player winner);
}
