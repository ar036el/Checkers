package el.arn.opencheckers.complementaries.game

import el.arn.opencheckers.complementaries.EnumWithId

enum class StartingPlayerEnum(override val id: String) : EnumWithId { Black("Black"), White("White"), Random("Random") }