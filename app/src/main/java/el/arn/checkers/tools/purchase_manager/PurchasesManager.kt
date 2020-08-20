package el.arn.checkers.tools.purchase_manager

import android.content.Context
import el.arn.checkers.tools.purchase_manager.core.GenericPurchasesManager
import el.arn.checkers.tools.purchase_manager.core.PurchaseStatus

class PurchasesManager(
    context: Context
) : GenericPurchasesManager(context, setOf(noAdsSKU, premiumVersionSKU))  {

    private companion object {
        const val noAdsSKU = "noAdsCrap"
        const val premiumVersionSKU = "premiumVersionCrap"
    }

    val noAds = getPurchasableItem(noAdsSKU)
    val premiumVersion = getPurchasableItem(premiumVersionSKU)

    val purchasedPremiumVersion get() = premiumVersion.purchaseStatus == PurchaseStatus.Purchased
    val purchasedNoAds get() = noAds.purchaseStatus == PurchaseStatus.Purchased

}