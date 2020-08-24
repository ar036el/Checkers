package el.arn.checkers.managers.purchase_manager

import android.content.Context
import el.arn.checkers.managers.purchase_manager.core.GenericPurchasesManager
import el.arn.checkers.managers.purchase_manager.core.GenericPurchasesManagerImpl
import el.arn.checkers.managers.purchase_manager.core.PurchasableItem
import el.arn.checkers.managers.purchase_manager.core.PurchaseStatus

interface PurchasesManager : GenericPurchasesManager {
    val purchasedPremiumVersion: Boolean
    val purchasedNoAds: Boolean

    val noAds: PurchasableItem
    val premiumVersion: PurchasableItem
}

class PurchasesManagerImpl (
    context: Context
) : PurchasesManager, GenericPurchasesManagerImpl(context, setOf(noAdsSKU, premiumVersionSKU))  {

    private companion object {
        const val noAdsSKU = "noAdsCrap"
        const val premiumVersionSKU = "premiumVersionCrap"
    }


    override val noAds = getPurchasableItem(noAdsSKU)
    override val premiumVersion = getPurchasableItem(premiumVersionSKU)

    override val purchasedPremiumVersion: Boolean
        get() {
            return (premiumVersion.purchaseStatus == PurchaseStatus.Purchased)
        }

    override val purchasedNoAds: Boolean
        get() {
            return (noAds.purchaseStatus == PurchaseStatus.Purchased)
        }
}