package el.arn.opencheckers.complementaries.android

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View

val View.locationInWindow: PixelCoordinate
    get() {
        val Listener = IntArray(2)
        this.getLocationInWindow(Listener)
        return PixelCoordinate(
            Listener[0],
            Listener[1]
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
