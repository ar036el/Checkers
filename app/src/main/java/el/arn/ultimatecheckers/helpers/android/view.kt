/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.helpers.android

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import el.arn.ultimatecheckers.helpers.points.PixelCoordinate

val View.locationInWindow: PixelCoordinate
    get() {
        val loc = IntArray(2)
        this.getLocationInWindow(loc)
        return PixelCoordinate(
            loc[0],
            loc[1]
        )
    }

val View.activity: Activity?
    get() {
        var context: Context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }