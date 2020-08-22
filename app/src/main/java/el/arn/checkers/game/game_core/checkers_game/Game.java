package el.arn.checkers.game.game_core.checkers_game;

import org.jetbrains.annotations.NotNull;

public interface Game extends GameLogic, ReadableBoardWithAvailableMoves, Cloneable {
    ReadableBoard getBoard();

    @NotNull
    Game clone();
}
