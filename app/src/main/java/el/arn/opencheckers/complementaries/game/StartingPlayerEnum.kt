package el.arn.opencheckers.complementaries.game

import el.arn.opencheckers.complementaries.EnumWithId

enum class StartingPlayerEnum(override val id: String) : EnumWithId { White("White"), Black("Black"), Random("Random") }