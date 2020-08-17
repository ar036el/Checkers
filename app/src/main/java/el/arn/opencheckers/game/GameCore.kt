package el.arn.opencheckers.game

import el.arn.opencheckers.game.game_core.game_core.Game
import el.arn.opencheckers.game.composites.SynchronizedSnapshotableGame
import el.arn.opencheckers.game.composites.Undoable
import el.arn.opencheckers.game.composites.UndoableWithSnapshots


interface GameCore : Game, Undoable

class GameCoreImpl(
    game: Game,
    synchronizedSnapshotableGame: SynchronizedSnapshotableGame = SynchronizedSnapshotableGame(game)
) : GameCore,
    Game by synchronizedSnapshotableGame,
    Undoable by UndoableWithSnapshots(
        synchronizedSnapshotableGame
    )


