package el.arn.checkers.helpers.points

operator fun Point.minus(other: Point) =
    Point(this.x - other.x, this.y - other.y)
operator fun Point.plus(other: Point) =
    Point(this.x + other.x, this.y + other.y)
