package el.arn.checkers.game.game_core.game_core;

import el.arn.checkers.game.game_core.game_core.structs.Player;

public interface GameLogicListener {
    void gameHasEnded(Player winner);
}
