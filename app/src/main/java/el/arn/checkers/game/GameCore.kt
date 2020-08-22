package el.arn.checkers.game

import el.arn.checkers.game.game_core.checkers_game.Game
import el.arn.checkers.game.game_core.SynchronizedSnapshotableGame
import el.arn.checkers.game.game_core.Undoable
import el.arn.checkers.game.game_core.UndoableWithSnapshots


interface GameCore : Game, Undoable

class GameCoreImpl(
    game: Game,
    synchronizedSnapshotableGame: SynchronizedSnapshotableGame = SynchronizedSnapshotableGame(game)
) : GameCore,
    Game by synchronizedSnapshotableGame,
    Undoable by UndoableWithSnapshots(
        synchronizedSnapshotableGame
    )


