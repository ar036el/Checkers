package el.arn.checkers.tools

import android.R
import android.content.Context
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import java.lang.ref.WeakReference

class ToastManager(private val applicationContext: Context) {
    private var currentToast: WeakReference<Toast>? = null

    fun showLong(text: String) {
        makeToast(text, false)
    }

    fun showShort(text: String) {
        makeToast(text, true)
    }

    fun cancelIfAny() {
        currentToast?.get()?.cancel()
    }

    private fun makeToast(text: String, isShort: Boolean) {
        cancelIfAny()
        val toast = Toast.makeText(applicationContext, text, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
        val v = toast.view.findViewById<TextView>(R.id.message)
        if (v != null) v.gravity = Gravity.CENTER
        toast.show()
        currentToast = WeakReference(toast)
    }
}