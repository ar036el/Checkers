package el.arn.opencheckers.dialogs

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout

class LayoutDialog(
    activity: Activity,
    title: String,
    @LayoutRes layoutRes: Int,
    private val showInstantly: Boolean = true,
    positiveButton: DialogButtonParams? = null,
    negativeButton: DialogButtonParams? = null,
    neutralButton: DialogButtonParams? = null
) {
    private val dialog: AlertDialog
    private val _dialogBuilder: AlertDialog.Builder

    init {
        val layout = activity.layoutInflater.inflate(layoutRes, null) as ConstraintLayout
        _dialogBuilder = AlertDialog.Builder(activity).setTitle(title).setView(layout)

        if (positiveButton != null) {
            _dialogBuilder.setPositiveButton(positiveButton.text) { _,_ -> positiveButton.doWhenClicked?.invoke() }
        }
        if (negativeButton != null) {
            _dialogBuilder.setNegativeButton(negativeButton.text) { _,_ -> negativeButton.doWhenClicked?.invoke() }
        }
        if (neutralButton != null) {
            _dialogBuilder.setNeutralButton(neutralButton.text) { _,_ -> neutralButton.doWhenClicked?.invoke() }
        }
        dialog = _dialogBuilder.create()
        if (showInstantly) {
            show()
        }
    }

    val dialogBuilder: AlertDialog.Builder?
        get() = if (!showInstantly) _dialogBuilder else null

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

}