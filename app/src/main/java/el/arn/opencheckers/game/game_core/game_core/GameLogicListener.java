package el.arn.opencheckers.game.game_core.game_core;

import el.arn.opencheckers.game.game_core.game_core.structs.Player;

public interface GameLogicListener {
    void gameHasEnded(Player winner);
}
