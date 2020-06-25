package el.arn.opencheckers.game.game_core.game_core;

import org.jetbrains.annotations.NotNull;

public interface Game extends GameLogic, ReadableBoardWithAvailableMoves, Cloneable {
    ReadableBoard getBoard();

    @NotNull
    Game clone();
}
