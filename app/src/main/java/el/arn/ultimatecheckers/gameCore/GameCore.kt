/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.gameCore

import el.arn.ultimatecheckers.gameCore.game_core.checkers_game.Game
import el.arn.ultimatecheckers.gameCore.game_core.SynchronizedSnapshotableGame
import el.arn.ultimatecheckers.gameCore.game_core.Undoable
import el.arn.ultimatecheckers.gameCore.game_core.UndoableWithSnapshots


interface GameCore : Game, Undoable

class GameCoreImpl(
    game: Game,
    synchronizedSnapshotableGame: SynchronizedSnapshotableGame = SynchronizedSnapshotableGame(game)
) : GameCore,
    Game by synchronizedSnapshotableGame,
    Undoable by UndoableWithSnapshots(
        synchronizedSnapshotableGame
    )


