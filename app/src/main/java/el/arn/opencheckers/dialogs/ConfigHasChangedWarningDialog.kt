/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import el.arn.opencheckers.R
import el.arn.opencheckers.helpers.android.stringFromRes


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
