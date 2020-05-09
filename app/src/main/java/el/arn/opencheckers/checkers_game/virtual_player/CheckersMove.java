package el.arn.opencheckers.checkers_game.virtual_player;

import el.arn.opencheckers.checkers_game.game_core.structs.Move;
import el.arn.opencheckers.checkers_game.game_core.structs.Tile;

public class CheckersMove implements GameState.Move {

    public final Tile tile;
    public final Move move;

    CheckersMove(Tile tile, Move move) {
        this.tile = tile;
        this.move = move;
    }
}
