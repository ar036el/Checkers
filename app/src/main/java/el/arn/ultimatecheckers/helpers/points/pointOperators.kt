/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.helpers.points

operator fun Point.minus(other: Point) =
    Point(
        this.x - other.x,
        this.y - other.y
    )
operator fun Point.plus(other: Point) =
    Point(
        this.x + other.x,
        this.y + other.y
    )
