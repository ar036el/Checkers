package el.arn.opencheckers.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import el.arn.opencheckers.*
import el.arn.opencheckers.widgets.buy_premium_activity.PurchaseButton

class PremiumPlansActivity : AppCompatActivity() {

    val purchasesManager = appRoot.purchasingManager


    lateinit var noAdsPurchaseButton: PurchaseButton
    lateinit var fullVersionPurchaseButton: PurchaseButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_premium)

        noAdsPurchaseButton =
            PurchaseButton(
                findViewById(R.id.purchasePremiumActivity_noAdsButton),
                purchasesManager.noAds,
                R.string.noAds_purchaseButton,
                R.string.purchasingNotAvailable,
                this
            )

        fullVersionPurchaseButton =
            PurchaseButton(
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


