package el.arn.opencheckers.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import el.arn.opencheckers.*
import el.arn.opencheckers.delegationMangement.LimitedDelegate
import el.arn.opencheckers.delegationMangement.LimitedDelegateImpl
import el.arn.opencheckers.purchase_manager.PurchasableItem
import el.arn.opencheckers.purchase_manager.PurchaseStatus

class PurchasePremiumActivity : AppCompatActivity() {

    val purchasesManager = App.instance.purchasesManager


    lateinit var noAdsPurchaseButton: PurchaseButton
    lateinit var fullVersionPurchaseButton: PurchaseButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_premium)

        noAdsPurchaseButton = PurchaseButton(
            findViewById(R.id.purchasePremiumActivity_noAdsButton),
            purchasesManager.noAds,
            R.string.noAds_purchaseButton,
            R.string.purchasingNotAvailable,
            this
        )

        fullVersionPurchaseButton = PurchaseButton(
            findViewById(R.id.purchasePremiumActivity_fullVersionButton),
            purchasesManager.fullVersion,
            R.string.fullVersion_purchaseButton,
            R.string.purchasingNotAvailable,
            this
        )

        purchasesManager.refreshPurchases()
        purchasesManager.refreshPrices()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}


class PurchaseButton(
    private val buttonView: Button,
    private val purchasableItem: PurchasableItem,
    @StringRes private val canPurchaseStringRes: Int,
    @StringRes private val cannotPurchaseStringRes: Int,
    private val activity: Activity) {
    companion object {
        const val PRICE_STRING_FLAG = "@pr"
    }

    var canPurchase: Boolean = false
        private set(value) {
            if (value) {
                buttonView.text = textReadyToPurchase
                buttonView.isEnabled = true
            } else {
                buttonView.text = StringsRes.get(cannotPurchaseStringRes)
                buttonView.isEnabled = false
            }
            field = value
        }


    private val textReadyToPurchase: String
        get() = StringsRes.get(canPurchaseStringRes).replace(PRICE_STRING_FLAG, purchasableItem.price, true)

    private fun updateState() {
        canPurchase = (purchasableItem.purchaseStatus != PurchaseStatus.Purchased)
    }

    private fun tryToLaunchBillingFlow() {
        val successful = purchasableItem.tryToLaunchBillingFlow(activity)
        if (!successful) {
            App.instance.toastMaker.showLong(StringsRes.get(R.string.purchasingNotAvailable))
        }
    }

    init {
        updateState()
        purchasableItem.addDelegate(
            object : PurchasableItem.Delegate, LimitedDelegate by LimitedDelegateImpl(destroyIf = {!buttonView.activity.isAlive}) {
                override fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus) { updateState() }
                override fun priceHasChanged(purchaseStatus: String) { updateState() }
            }
        )
        buttonView.setOnClickListener {
            tryToLaunchBillingFlow()
        }
    }

}