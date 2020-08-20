package el.arn.checkers.game

import el.arn.checkers.game.game_core.game_core.Game
import el.arn.checkers.game.composites.SynchronizedSnapshotableGame
import el.arn.checkers.game.composites.Undoable
import el.arn.checkers.game.composites.UndoableWithSnapshots


interface GameCore : Game, Undoable

class GameCoreImpl(
    game: Game,
    synchronizedSnapshotableGame: SynchronizedSnapshotableGame = SynchronizedSnapshotableGame(game)
) : GameCore,
    Game by synchronizedSnapshotableGame,
    Undoable by UndoableWithSnapshots(
        synchronizedSnapshotableGame
    )


