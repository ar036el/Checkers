package el.arn.opencheckers.kol_minei

import android.view.View
import el.arn.opencheckers.helpers.PointInPx

val View.locationInWindow: PointInPx
    get() {
        val delegate = IntArray(2)
        this.getLocationInWindow(delegate)
        return PointInPx(delegate[0], delegate[1])
    }