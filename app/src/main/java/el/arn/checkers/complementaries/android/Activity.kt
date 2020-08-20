package el.arn.checkers.complementaries.android

import android.app.Activity
import android.content.res.Configuration
import android.view.View

val Activity.orientation: Orientations
    get() {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            Orientations.Landscape
        else
            Orientations.Portrait
    }    //TODO also put screen size


val Activity?.isAlive: Boolean
    get() = (this?.isDestroyed == false)



val Activity.isDirectionRTL
    get() =  resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

