package el.arn.opencheckers.dialogs

import android.app.Activity
import android.content.DialogInterface
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog

class MessageDialog(
    activity: Activity,
    title: String,
    message: String,
    private val showInstantly: Boolean = true,
    positiveButton: DialogButtonParams? = null,
    negativeButton: DialogButtonParams? = null,
    neutralButton: DialogButtonParams? = null
) {
    private val dialog: AlertDialog
    private val _dialogBuilder: AlertDialog.Builder

    init {
        _dialogBuilder = AlertDialog.Builder(activity).setTitle(title).setMessage(message)

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