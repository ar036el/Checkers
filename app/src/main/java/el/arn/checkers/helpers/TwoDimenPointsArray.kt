package el.arn.checkers.helpers

import el.arn.checkers.helpers.points.Point


class TwoDimenPointsArray<T>(
    val size: Int,
    private val init: (index: Point) -> T)
{

    private val array =
        Array(size) {
            x ->
            Array<Any?>(size) {
                y ->
                init(Point(x, y))
            }
        }

    operator fun get(x: Int, y: Int): T {
        return array[x][y] as T
    }

    operator fun get(point: Point): T {
        return array[point.x][point.y] as T
    }

    operator fun set(x: Int, y: Int, value: T) {
        array[x][y] = value
    }

    operator fun set(point: Point, value: T) {
        array[point.x][point.y] = value
    }

    fun indexOf(item: T): Point? {
        for (x in array.indices) {
            for (y in array[x].indices) {
                if (array[x][y] == item) {
                    return Point(x, y)
                }
            }
        }
        return null
    }

    operator fun iterator(): Iterator<T> {
        return object: Iterator<T> {
            private var x = 0
            private var y = 0

            override fun hasNext(): Boolean {
                return y < array.size
            }

            override fun next(): T {
                val result = array[x++][y] as T
                if (x > array.lastIndex) {
                    x = 0
                    y += 1
                }
                return result
            }

        }

    }
}
