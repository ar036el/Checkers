package el.arn.checkers.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import el.arn.checkers.R
import el.arn.checkers.appRoot


class ConfigHasChangedWarningDialog(
    activity: Activity,
    private val applyIfConfirmed: () -> Unit
) : Dialog {

    override val isShowing: Boolean
        get() = dialog.isShowing
    override fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    private var dialog: android.app.Dialog

    init {
        dialog =
            AlertDialog.Builder(activity)
                .setMessage(appRoot.getStringRes(R.string.configHasChangedWarningDialog_message))
                .setPositiveButton(appRoot.getStringRes(R.string.dialog_ok)) { _,_ ->  applyIfConfirmed.invoke() }
                .setNegativeButton(appRoot.getStringRes(R.string.dialog_cancel), null)
                .show()

    }
}
