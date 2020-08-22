package el.arn.checkers.helpers.android

import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import el.arn.checkers.appRoot

fun stringFromRes(@StringRes stringRes: Int): String {
    return appRoot.resources.getString(stringRes)
}

fun dimenFromRes(@DimenRes dimenRes: Int): Float {
    return appRoot.resources.getDimension(dimenRes)
}