package el.arn.opencheckers.complementaries.game

import el.arn.opencheckers.complementaries.EnumWithId

enum class GameTypeEnum(override val id: String) :
    EnumWithId { SinglePlayer("SinglePlayer"), Multiplayer("Multiplayer"), VirtualGame("VirtualGame") }