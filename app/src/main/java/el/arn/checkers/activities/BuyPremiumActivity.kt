package el.arn.checkers.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import el.arn.checkers.*
import el.arn.checkers.activity_widgets.buy_premium_activity.PurchaseButton
import el.arn.checkers.dialogs.ThanksForPurchasingDialog
import el.arn.checkers.managers.purchase_manager.PurchasableItem
import el.arn.checkers.managers.purchase_manager.PurchasesManager
import el.arn.checkers.managers.purchase_manager.core.PurchaseStatus

class BuyPremiumActivity : AppCompatActivity() {

    private val purchasesManager = appRoot.purchasesManager
    private lateinit var noAdsPurchaseButton: PurchaseButton
    private lateinit var premiumVersionPurchaseButton: PurchaseButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_premium)

        noAdsPurchaseButton =
            PurchaseButton(
                findViewById(R.id.purchasePremiumActivity_noAdsButton),
                purchasesManager.noAds,
                R.string.noAds_purchaseButton,
                R.string.alreadyPurchased,
                R.string.purchasingNotAvailable,
                this
            )

        premiumVersionPurchaseButton =
            PurchaseButton(
                findViewById(R.id.purchasePremiumActivity_premiumVersionButton),
                purchasesManager.premiumVersion,
                R.string.premiumVersion_purchaseButton,
                R.string.alreadyPurchased,
                R.string.purchasingNotAvailable,
                this
            )

        purchasesManager.refreshPurchases()
        purchasesManager.refreshPrices()

        addListenersToPurchasableItems()

    }

    override fun onDestroy() {
        super.onDestroy()
        removeListenersFromPurchasableItems()
    }

    private val noAdsListener = object : PurchasableItem.Listener {
        override fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus) {
            if (purchaseStatus == PurchaseStatus.Purchased) {
                ThanksForPurchasingDialog(this@BuyPremiumActivity, PurchasesManager.PurchasableItems.NoAds)
            }
        }
        override fun priceHasChanged(purchaseStatus: String) {}
    }
    private val premiumVersionListener = object : PurchasableItem.Listener {
        override fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus) {
            if (purchaseStatus == PurchaseStatus.Purchased) {
                ThanksForPurchasingDialog(this@BuyPremiumActivity, PurchasesManager.PurchasableItems.PremiumVersion)
            }
        }
        override fun priceHasChanged(purchaseStatus: String) {}
    }
    private fun addListenersToPurchasableItems() {
        purchasesManager.noAds.addListener(noAdsListener)
        purchasesManager.premiumVersion.addListener(premiumVersionListener)
    }
    private fun removeListenersFromPurchasableItems() {
        purchasesManager.noAds.removeListener(noAdsListener)
        purchasesManager.premiumVersion.removeListener(premiumVersionListener)
    }

}


