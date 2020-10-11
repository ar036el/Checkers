/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import el.arn.ultimatecheckers.R
import el.arn.ultimatecheckers.helpers.android.stringFromRes
import el.arn.ultimatecheckers.managers.purchase_manager.PurchasesManager


class ThanksForPurchasingDialog(
    activity: Activity,
    purchasableItemType: PurchasesManager.PurchasableItems
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
        val message = when (purchasableItemType) {
            PurchasesManager.PurchasableItems.NoAds -> stringFromRes(R.string.dialog_thanksForPurchasing_noAds_message)
            PurchasesManager.PurchasableItems.PremiumVersion -> stringFromRes(R.string.dialog_thanksForPurchasing_premiumVersion_message)
        }

        dialog = AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton(stringFromRes(R.string.general_dialog_ok), null)
            .setNegativeButton(stringFromRes(R.string.general_dialog_cancel), null)
            .show()

    }
}
