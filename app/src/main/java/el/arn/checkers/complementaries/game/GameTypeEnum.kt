package el.arn.checkers.complementaries.game

import el.arn.checkers.complementaries.EnumWithId

enum class GameTypeEnum(override val id: String) :
    EnumWithId { SinglePlayer("SinglePlayer"), Multiplayer("Multiplayer"), VirtualGame("VirtualGame") }