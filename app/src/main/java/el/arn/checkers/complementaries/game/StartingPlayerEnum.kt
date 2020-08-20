package el.arn.checkers.complementaries.game

import el.arn.checkers.complementaries.EnumWithId

enum class StartingPlayerEnum(override val id: String) : EnumWithId { White("White"), Black("Black"), Random("Random") }