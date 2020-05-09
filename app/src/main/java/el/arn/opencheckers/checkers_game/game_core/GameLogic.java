package el.arn.opencheckers.checkers_game.game_core;

import el.arn.opencheckers.checkers_game.game_core.exceptions.CannotPassTurn;
import el.arn.opencheckers.checkers_game.game_core.exceptions.GameException;
import el.arn.opencheckers.checkers_game.game_core.structs.Piece;
import el.arn.opencheckers.checkers_game.game_core.structs.Player;

public interface GameLogic {
    /** @return {@code Piece} if a piece was captured, {@code null} otherwise **/
    Piece makeAMove(int xFrom, int yFrom, int xTo, int yTo) throws GameException;

    void passTurn() throws CannotPassTurn;
    boolean isExtraTurn();
    Player getCurrentPlayer();

    /** @return {@code null} if game has not yet ended **/
    Player getWinner();

}
