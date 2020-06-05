package el.arn.opencheckers.kol_minei

import android.content.Context
import android.widget.Toast
import java.lang.ref.WeakReference

class ToastMaker(private val applicationContext: Context) {
    private var currentToast: WeakReference<Toast>? = null

    fun showLong(text: String) {
        cancelIfAny()
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_LONG)
        toast.show()
        currentToast = WeakReference(toast)
    }

    fun showShort(text: String) {
        cancelIfAny()
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        toast.show()
        currentToast = WeakReference(toast)
    }

    fun cancelIfAny() {
        currentToast?.get()?.cancel()
    }
}