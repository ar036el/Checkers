package el.arn.opencheckers.helpers

import el.arn.opencheckers.checkers_game.game_core.structs.Point

operator fun Point.minus(other: Point) = Point(this.x - other.x, this.y - other.y)
operator fun Point.plus(other: Point) = Point(this.x + other.x, this.y + other.y)

class PointInPx(x: Int, y: Int) : Point(x, y)