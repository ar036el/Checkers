package el.arn.checkers.helpers.game_enums

import el.arn.checkers.helpers.EnumWithId

enum class StartingPlayerEnum(override val id: String) : EnumWithId { White("White"), Black("Black"), Random("Random") }