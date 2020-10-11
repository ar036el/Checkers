/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.gameCore.game_core.virtual_player_core;

import java.util.Set;

public interface GameState<M extends GameState.Move> {

    /**Returns a new game state after the given Move has applied to it. the turn is switched to the other player.<br>
     *Returns {@code null} if move is not available.<br/>
     *<li>Can only receive a move that came from {@code getAvailableMoves()}.
    **/
    GameState<M> makeAMove(M move);

    /** Returns all available current player's moves.<br/>
     * Returns an empty set if no moves are available.**/
    Set<M> getPossibleMoves();

    int getHeuristicValue();
    int getSecondaryHeuristicValue();

    interface Move { }
}
