package el.arn.opencheckers.complementaries.game

import el.arn.opencheckers.game.game_core.game_core.structs.Point

class TileCoordinates(x: Int, y: Int) : Point(x,y)

operator fun Point.minus(other: Point) = Point(this.x - other.x, this.y - other.y)
operator fun Point.plus(other: Point) = Point(this.x + other.x, this.y + other.y)
