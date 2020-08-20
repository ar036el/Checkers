package el.arn.checkers.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import el.arn.checkers.*
import el.arn.checkers.android_widgets.buy_premium_activity.PurchaseButton

class BuyPremiumActivity : AppCompatActivity() {

    val purchasesManager = appRoot.purchasesManager


    lateinit var noAdsPurchaseButton: PurchaseButton
    lateinit var premiumVersionPurchaseButton: PurchaseButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_premium)

        noAdsPurchaseButton =
            PurchaseButton(
                findViewById(R.id.purchasePremiumActivity_noAdsButton),
                purchasesManager.noAds,
                R.string.noAds_purchaseButton,
                R.string.purchasingNotAvailable,
                this
            )

        premiumVersionPurchaseButton =
            PurchaseButton(
                findViewById(R.id.purchasePremiumActivity_premiumVersionButton),
                purchasesManager.premiumVersion,
                R.string.premiumVersion_purchaseButton,
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


