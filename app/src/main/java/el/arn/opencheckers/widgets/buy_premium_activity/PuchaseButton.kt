package el.arn.opencheckers.widgets.buy_premium_activity

import android.app.Activity
import android.widget.Button
import androidx.annotation.StringRes
import el.arn.opencheckers.*
import el.arn.opencheckers.complementaries.android.activity
import el.arn.opencheckers.complementaries.android.isAlive
import el.arn.opencheckers.complementaries.listener_mechanism.LimitedListener
import el.arn.opencheckers.complementaries.listener_mechanism.LimitedListenerImpl
import el.arn.opencheckers.tools.purchase_manager.core.PurchasableItem
import el.arn.opencheckers.tools.purchase_manager.core.PurchaseStatus

class PurchaseButton(
    private val buttonView: Button,
    private val purchasableItem: PurchasableItem,
    @StringRes private val canPurchaseStringRes: Int,
    @StringRes private val cannotPurchaseStringRes: Int,
    private val activity: Activity
) {
    companion object {
        const val PRICE_STRING_FLAG = "@pr"
    }

    var canPurchase: Boolean = false
        private set(value) {
            if (value) {
                buttonView.text = textReadyToPurchase
                buttonView.isEnabled = true
            } else {
                buttonView.text = appRoot.getStringRes(cannotPurchaseStringRes)
                buttonView.isEnabled = false
            }
            field = value
        }


    private val textReadyToPurchase: String
        get() = appRoot.getStringRes(canPurchaseStringRes).replace(PRICE_STRING_FLAG, purchasableItem.price, true)

    private fun updateState() {
        canPurchase = (purchasableItem.purchaseStatus != PurchaseStatus.Purchased)
    }

    private fun tryToLaunchBillingFlow() {
        val successful = purchasableItem.tryToLaunchBillingFlow(activity)
        if (!successful) {
            appRoot.toastMessageManager.showLong(appRoot.getStringRes(R.string.purchasingNotAvailable))
        }
    }

    init {
        updateState()
        purchasableItem.addListener(
            object : PurchasableItem.Listener, LimitedListener by LimitedListenerImpl(destroyIf = {!buttonView.activity.isAlive}) {
                override fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus) { updateState() }
                override fun priceHasChanged(purchaseStatus: String) { updateState() }
            }
        )
        buttonView.setOnClickListener {
            tryToLaunchBillingFlow()
        }
    }

}