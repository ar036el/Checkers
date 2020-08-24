package el.arn.checkers.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.android.stringFromRes


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
                .setMessage(stringFromRes(R.string.mainActivity_configHasChangedWarningDialog_message))
                .setPositiveButton(stringFromRes(R.string.general_dialog_ok)) { _, _ ->  applyIfConfirmed.invoke() }
                .setNegativeButton(stringFromRes(R.string.general_dialog_cancel), null)
                .show()

    }
}
