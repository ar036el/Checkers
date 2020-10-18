/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.helpers.android

import android.content.res.Resources
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import el.arn.opencheckers.appRoot

fun stringFromRes(@StringRes stringRes: Int): String {
    return appRoot.resources.getString(stringRes)
}

fun dimenFromRes(@DimenRes dimenRes: Int): Float {
    return appRoot.resources.getDimension(dimenRes)
}

fun Resources.getFloatCompat(@DimenRes floatRes: Int): Float {
    val outValue = android.util.TypedValue()
    el.arn.opencheckers.appRoot.resources.getValue(floatRes, outValue, true)
    return outValue.float
}