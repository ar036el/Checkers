package el.arn.opencheckers.purchase_manager

import android.content.Context

class PurchasesManager(
    context: Context
) : GenericPurchasesManager(context, setOf(noAdsSKU, fullVersionSKU))  {

    private companion object {
        const val noAdsSKU = "noAdsCrap"
        const val fullVersionSKU = "fullVersionCrap"
    }

    val noAds = getPurchasableItem(noAdsSKU)
    val fullVersion = getPurchasableItem(fullVersionSKU)

}