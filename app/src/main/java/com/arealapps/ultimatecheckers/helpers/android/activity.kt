/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.helpers.android

import android.app.Activity
import android.content.res.Configuration
import android.view.View

val Activity.orientation: OrientationOptions
    get() {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            OrientationOptions.Landscape
        else
            OrientationOptions.Portrait
    }    //TODO also put screen size


val Activity?.isAlive: Boolean
    get() = (this?.isDestroyed == false)



val Activity.isDirectionRTL
    get() =  resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

