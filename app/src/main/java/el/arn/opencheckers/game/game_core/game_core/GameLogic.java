package el.arn.opencheckers.game.game_core.game_core;

import el.arn.opencheckers.game.game_core.game_core.configurations.GameLogicConfig;
import el.arn.opencheckers.game.game_core.game_core.exceptions.CannotPassTurn;
import el.arn.opencheckers.game.game_core.game_core.exceptions.GameException;
import el.arn.opencheckers.game.game_core.game_core.structs.Player;
import el.arn.opencheckers.game.game_core.game_core.structs.Tile;

public interface GameLogic {
    /** @return {@code Tile} of the captured piece if happened, {@code null} otherwise **/
    Tile makeAMove(int xFrom, int yFrom, int xTo, int yTo) throws GameException;

    void passTurn() throws CannotPassTurn;
    boolean isExtraTurn();
    boolean canPassExtraTurn();

    Player getCurrentPlayer();

    void refreshGame();

    GameLogicConfig getConfig();

    /** @return {@code null} if game has not yet ended **/
    Player getWinner();

    void setListener(GameLogicListener Listener);

}
