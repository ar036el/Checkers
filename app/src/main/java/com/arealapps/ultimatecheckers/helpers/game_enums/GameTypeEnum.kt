/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.helpers.game_enums

import com.arealapps.ultimatecheckers.helpers.EnumWithId

enum class GameTypeEnum(override val id: String) :
    EnumWithId { SinglePlayer("SinglePlayer"), Multiplayer("Multiplayer"), VirtualGame("VirtualGame") }