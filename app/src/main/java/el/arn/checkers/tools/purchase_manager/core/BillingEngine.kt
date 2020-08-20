package el.arn.checkers.tools.purchase_manager.core

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import el.arn.checkers.appRoot
import el.arn.checkers.complementaries.listener_mechanism.ListenersManager
import el.arn.checkers.complementaries.listener_mechanism.HoldsListeners
import el.arn.checkers.complementaries.EnumWithId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class PurchaseStatus(override val id: String) : EnumWithId { Unspecified("UnspecifiedOrError"), Pending("Pending"), Purchased("Purchased")}

class BillingEngine(
    val SKUs: Set<String>,
    private val listenersMgr: ListenersManager<Listener> = ListenersManager()
) : HoldsListeners<BillingEngine.Listener> by listenersMgr {

    fun reloadPurchases(): Boolean /** @return [false] if the billing client is not connected. In that case, it tried to reconnect. Listeners will be notified when purchases will reload*/ {
        if (billingClient.isReady) {
            queryPurchasesAsync()
            return true
        }
        billingClient.tryToConnect()
        return false
    }

    fun reloadPrices(): Boolean /** @return [false] if the billing client is not connected. In that case, it tried to reconnect. Listeners will be notified when purchases will reload*/ {
        if (billingClient.isReady) {
            querySkuDetailsAsync()
            return true
        }
        billingClient.tryToConnect()
        return false
    }

    interface Listener {
        /** Is being called when: (1)billing client connects to the server, (2)a purchase was just made, (3)[reloadPurchases] was called.*/
        fun onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses: Map<String, PurchaseStatus>) {}
        /** Is being called when: (1)billing client connects to the server, (2)[reloadPrices] was called.*/
        fun onPricesLoadedOrChanged(SKUsWithPrices: Map<String, String>) {}
    }

    private lateinit var billingClient: BillingClient
    private var skuWithSkuDetails = emptyMap<String, SkuDetails>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
//                BillingClient.BillingResponseCode.OK -> {
//                    // will handle server verification, consumables, and updating the local cache
//                    //TODO just purchased?
//                    processAndDeclarePurchases(purchases)
//                }
                BillingClient.BillingResponseCode.OK, BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    // item already owned? call queryPurchasesAsync to verify and process all such items
                    queryPurchasesAsync()
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    billingClient.tryToConnect()
                }
            }
        Log.d(this.toString(), billingResult.debugMessage)
    }

    private fun processPurchases(purchases: List<Purchase>) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            Log.d(this.toString(), "processPurchases called")
            val SKUsWithPurchaseStatuses = mutableMapOf<String, PurchaseStatus>()

            purchases.forEach { purchase ->
                var purchaseStatus: PurchaseStatus =
                    PurchaseStatus.Unspecified

                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (isSignatureValid(purchase)) {
                        purchaseStatus =
                            PurchaseStatus.Purchased
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                    } else {
                        Log.w(this.toString(), "Purchase is not verified!!!: ${purchase.sku}")
                    }

                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    Log.d(this.toString(), "Received a pending purchase of SKU: ${purchase.sku}")
                    purchaseStatus =
                        PurchaseStatus.Pending
                }

                if (purchase.sku !in SKUs || SKUsWithPurchaseStatuses[purchase.sku] != null ) {
                    Log.w(this.toString(), "unlisted sku ${purchase.sku} detected")
                } else {
                    SKUsWithPurchaseStatuses[purchase.sku] =  purchaseStatus
                }
            }

            listenersMgr.notifyAll { it.onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses) }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.acknowledgePurchase(params) { billingResult -> Log.d(this.toString(), "acknowledgeNonConsumablePurchasesAsync response is ${billingResult.responseCode} ${billingResult.debugMessage}") }
    }


    private fun querySkuDetailsAsync() {
        val params = SkuDetailsParams.newBuilder().setSkusList(SKUs.toList()).setType(BillingClient.SkuType.INAPP).build()
        Log.d(this.toString(), "querySkuDetailsAsync for ${BillingClient.SkuType.INAPP}")

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {

                    val SKUsWithPrices = mutableMapOf<String, String>()
                    skuDetailsList.forEach { skuDetails ->

                        if (skuDetails.sku !in SKUs || SKUsWithPrices[skuDetails.sku] != null) {
                            Log.w(this.toString(), "unlisted sku ${skuDetails.sku} detected")
                        } else {
                            SKUsWithPrices[skuDetails.sku] = skuDetails.price
                        }
                    }

                    listenersMgr.notifyAll { it.onPricesLoadedOrChanged(SKUsWithPrices) }
                }
                else -> {
                    Log.e(this.toString(), billingResult.debugMessage)
                }
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
            Security.BASE_64_ENCODED_PUBLIC_KEY,
            purchase.originalJson,
            purchase.signature
        )
    }

    private fun queryPurchasesAsync() {
        Log.d(this.toString(), "queryPurchasesAsync called")
        val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d(this.toString(), "queryPurchasesAsync INAPP results: ${result.purchasesList?.size}")
        // will handle server verification, consumables, and updating the local cache
        processPurchases(result.purchasesList.orEmpty().filterNotNull())
    }

    private fun BillingClient.tryToConnect(): Boolean {
        Log.d(this.toString(), "connectToPlayBillingService")
        if (!this.isReady) {
            this.startConnection(billingClientStateListener)
            return true
        }
        return false
    }

    private val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) { //trivial
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(this.toString(), "onBillingSetupFinished successfully")
                    querySkuDetailsAsync()
                    queryPurchasesAsync()
                }
                else -> {
                    //do nothing. Someone else will connect it through retry policy. May choose to send to server though
                    Log.d(this.toString(), billingResult.debugMessage)
                }
            }
        }

        override fun onBillingServiceDisconnected() { //trivial
            Log.d(this.toString(), "onBillingServiceDisconnected")
            billingClient.tryToConnect()
        }
    }

    fun tryToLaunchBillingFlow(activity: Activity, SKU: String): Boolean /** @return [true] if successful, [false] otherwise (fails because SKU is not found or hasn't finished loading)*/ {
        val skuDetails = skuWithSkuDetails[SKU] ?: return false
        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        billingClient.launchBillingFlow(activity, purchaseParams)
        return true
    }

    init {
        billingClient = BillingClient.newBuilder(appRoot.applicationContext)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
        billingClient.tryToConnect()
    }

}