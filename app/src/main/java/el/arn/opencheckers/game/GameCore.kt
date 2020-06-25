package el.arn.opencheckers.game

import el.arn.opencheckers.game.game_core.game_core.Game
import el.arn.opencheckers.game.game_core.game_core.GameLogicListener
import el.arn.opencheckers.game.game_core.game_core.ReadableBoard
import el.arn.opencheckers.game.game_core.game_core.configurations.GameLogicConfig
import el.arn.opencheckers.game.game_core.game_core.structs.Move
import el.arn.opencheckers.game.game_core.game_core.structs.Piece
import el.arn.opencheckers.game.game_core.game_core.structs.Player
import el.arn.opencheckers.game.game_core.game_core.structs.Tile
import el.arn.opencheckers.game.parts.Snapshotable
import el.arn.opencheckers.game.parts.SynchronizedSnapshotableGame
import el.arn.opencheckers.game.parts.Undoable
import el.arn.opencheckers.game.parts.UndoableWithSnapshots


interface GameCore : Game, Undoable

class GameCoreImpl(
    game: Game,
    synchronizedSnapshotableGame: SynchronizedSnapshotableGame = SynchronizedSnapshotableGame(game)
) : GameCore,
    Game by synchronizedSnapshotableGame,
    Undoable by UndoableWithSnapshots(
        synchronizedSnapshotableGame
    )


