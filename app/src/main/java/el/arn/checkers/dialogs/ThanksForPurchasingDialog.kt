package el.arn.checkers.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import el.arn.checkers.R
import el.arn.checkers.helpers.android.stringFromRes
import el.arn.checkers.managers.purchase_manager.PurchasesManager


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
