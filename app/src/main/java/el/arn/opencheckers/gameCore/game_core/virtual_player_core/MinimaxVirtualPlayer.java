/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.gameCore.game_core.virtual_player_core;

public interface MinimaxVirtualPlayer<M extends GameState.Move> {
    M getMove(GameState gameState, int depthLimit);

    /** @return {@code true} if computation is not in process, {@code false} otherwise **/
    boolean cancelComputationProcess();
}

