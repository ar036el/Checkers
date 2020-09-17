/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.helpers.android

import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import el.arn.ultimatecheckers.appRoot

fun stringFromRes(@StringRes stringRes: Int): String {
    return appRoot.resources.getString(stringRes)
}

fun dimenFromRes(@DimenRes dimenRes: Int): Float {
    return appRoot.resources.getDimension(dimenRes)
}