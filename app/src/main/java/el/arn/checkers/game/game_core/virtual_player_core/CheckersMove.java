package el.arn.checkers.game.game_core.virtual_player_core;

import el.arn.checkers.game.game_core.checkers_game.structs.Move;
import el.arn.checkers.game.game_core.checkers_game.structs.Tile;

public class CheckersMove implements GameState.Move {

    public final Tile fromTile;
    public final Move move;

    CheckersMove(Tile fromTile, Move move) {
        this.fromTile = fromTile;
        this.move = move;
    }
}
