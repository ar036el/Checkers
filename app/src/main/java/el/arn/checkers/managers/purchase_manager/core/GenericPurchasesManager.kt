package el.arn.checkers.managers.purchase_manager.core

import android.app.Activity
import android.content.Context
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.listeners_engine.*
import el.arn.checkers.helpers.NonNullMap
import el.arn.checkers.managers.preferences_managers.Pref
import el.arn.checkers.managers.preferences_managers.PreferencesManager

open class GenericPurchasesManager(
    private val context: Context,
    private val SKUs: Set<String>
) {
    private val SKUsWithPurchasableItems: NonNullMap<String, PurchasableItem>
    private val purchasesPreferencesManager: PreferencesManager = PreferencesManager(appRoot.getSharedPreferences("purchases", Context.MODE_PRIVATE))
    private val billingEngine =
        BillingEngine(SKUs)

    fun getPurchasableItem(SKU: String) = SKUsWithPurchasableItems[SKU]

    fun refreshPurchases(doWhenRefreshed: ((PurchasableItemsWithPurchaseStatuses: Map<PurchasableItem, PurchaseStatus>) -> Unit)? = null) {
        billingEngine.addListener(
            object : BillingEngine.Listener, LimitedListener by LimitedListenerImpl(destroyAfterCall = true) {
                override fun onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses: Map<String, PurchaseStatus>) {
                    val map = SKUs.toList().map {SKU ->
                        (SKUsWithPurchasableItems[SKU]) to (SKUsWithPurchaseStatuses[SKU] ?: PurchaseStatus.Unspecified) }
                        .toMap()
                    doWhenRefreshed?.invoke(map)
                }
            }
        )
        billingEngine.reloadPurchases()
    }

    fun refreshPrices(doWhenRefreshed: ((PurchasableItemsWithPrices: Map<PurchasableItem, String?>) -> Unit)? = null) {
        billingEngine.addListener(
            object : BillingEngine.Listener, LimitedListener by LimitedListenerImpl(destroyAfterCall = true) {
                override fun onPricesLoadedOrChanged(SKUsWithPrices: Map<String, String>) {
                    val map = SKUs.toList().map {SKU ->
                        (SKUsWithPurchasableItems[SKU]) to SKUsWithPrices[SKU] }
                        .toMap()
                    doWhenRefreshed?.invoke(map)
                }
            }
        )
        billingEngine.reloadPrices()
    }

    init {
        val map = mutableMapOf<String, PurchasableItem>()
        for (SKU in SKUs) {
            map[SKU] =
                PurchasableItemImpl(
                    SKU,
                    billingEngine,
                    purchasesPreferencesManager
                )
        }
        SKUsWithPurchasableItems = NonNullMap(map.toMap())
    }
}

//TODo holds Listener and Listeners is really anoying. just make everything "holdsListeners" and make interfaces accordingly
interface PurchasableItem : HoldsListeners<PurchasableItem.Listener> {

    val SKU: String
    val price: String
    val purchaseStatus: PurchaseStatus
    fun tryToLaunchBillingFlow(activity: Activity): Boolean

    interface Listener {
        fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus)
        fun priceHasChanged(purchaseStatus: String)
    }
}

class PurchasableItemImpl(
    override val SKU: String,
    private val billingEngine: BillingEngine,
    purchasesPreferencesManager: PreferencesManager,
    private val listenersMgr: ListenersManager<PurchasableItem.Listener> = ListenersManager()
): PurchasableItem, HoldsListeners<PurchasableItem.Listener> by listenersMgr {


    override val purchaseStatus: PurchaseStatus
        get() = prefForPurchaseStatus.value
    override val price: String
        get() = prefForPrice.value

    private val prefForPurchaseStatus = purchasesPreferencesManager.createEnumPref(SKU + "PurchaseStatus", PurchaseStatus.values(), PurchaseStatus.Unspecified)
    private val prefForPrice = purchasesPreferencesManager.createStringPref(SKU + "Price", null, "")


    override fun tryToLaunchBillingFlow(activity: Activity): Boolean {
        return billingEngine.tryToLaunchBillingFlow(activity, SKU)
    }

    init {
        prefForPurchaseStatus.addListener( object : Pref.Listener<PurchaseStatus> {
            override fun prefHasChanged(pref: Pref<PurchaseStatus>, value: PurchaseStatus) {
                listenersMgr.notifyAll { it.purchaseStatusHasChanged(value) }
            }
        })
        prefForPrice.addListener( object : Pref.Listener<String> {
            override fun prefHasChanged(pref: Pref<String>, value: String) {
                listenersMgr.notifyAll { it.priceHasChanged(value) }
            }
        })

        billingEngine.addListener(object : BillingEngine.Listener {
            override fun onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses: Map<String, PurchaseStatus>) {
                prefForPurchaseStatus.value = SKUsWithPurchaseStatuses[SKU] ?: PurchaseStatus.Unspecified
            }
            override fun onPricesLoadedOrChanged(SKUsWithPrices: Map<String, String>) {
                SKUsWithPrices[SKU]?.let { prefForPrice.value = it }
            }
        })
    }

}