package el.arn.checkers.helpers.game_enums

import el.arn.checkers.helpers.EnumWithId

enum class GameTypeEnum(override val id: String) :
    EnumWithId { SinglePlayer("SinglePlayer"), Multiplayer("Multiplayer"), VirtualGame("VirtualGame") }