package el.arn.opencheckers.purchase_manager

import android.app.Activity
import android.content.Context
import el.arn.opencheckers.App
import el.arn.opencheckers.delegationMangement.*
import el.arn.opencheckers.helpers.NonNullMap
import el.arn.opencheckers.prefs_managers.Pref
import el.arn.opencheckers.prefs_managers.PrefsManager

open class GenericPurchasesManager(
    private val context: Context,
    private val SKUs: Set<String>
) {
    private val SKUsWithPurchasableItems: NonNullMap<String, PurchasableItem>
    private val purchasesPrefsManager: PrefsManager = PrefsManager(App.instance.getSharedPreferences("purchases", Context.MODE_PRIVATE))
    private val billingEngine = BillingEngine(SKUs)

    fun getPurchasableItem(SKU: String) = SKUsWithPurchasableItems[SKU]

    fun refreshPurchases(doWhenRefreshed: ((PurchasableItemsWithPurchaseStatuses: Map<PurchasableItem, PurchaseStatus>) -> Unit)? = null) {
        billingEngine.addDelegate(
            object : BillingEngine.Delegate, LimitedDelegate by LimitedDelegateImpl(destroyAfterCall = true) {
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
        billingEngine.addDelegate(
            object : BillingEngine.Delegate, LimitedDelegate by LimitedDelegateImpl(destroyAfterCall = true) {
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
            map[SKU] = PurchasableItemImpl(SKU, billingEngine, purchasesPrefsManager)
        }
        SKUsWithPurchasableItems = NonNullMap(map.toMap())
    }
}

//TODo holds delegate and delegates is really anoying. just make everything "holdsDelegates" and make interfaces accordingly
interface PurchasableItem : HoldsDelegates<PurchasableItem.Delegate> {

    val SKU: String
    val price: String
    val purchaseStatus: PurchaseStatus
    fun tryToLaunchBillingFlow(activity: Activity): Boolean

    interface Delegate {
        fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus)
        fun priceHasChanged(purchaseStatus: String)
    }
}

class PurchasableItemImpl(
    override val SKU: String,
    private val billingEngine: BillingEngine,
    purchasesPrefsManager: PrefsManager,
    private val delegationMgr: DelegatesManager<PurchasableItem.Delegate> = DelegatesManager()
): PurchasableItem, HoldsDelegates<PurchasableItem.Delegate> by delegationMgr {


    override val purchaseStatus: PurchaseStatus
        get() = prefForPurchaseStatus.value
    override val price: String
        get() = prefForPrice.value

    private val prefForPurchaseStatus = purchasesPrefsManager.createEnumPref(SKU + "PurchaseStatus", PurchaseStatus.values(), PurchaseStatus.Unspecified)
    private val prefForPrice = purchasesPrefsManager.createStringPref(SKU + "Price", null, "")


    override fun tryToLaunchBillingFlow(activity: Activity): Boolean {
        return billingEngine.tryToLaunchBillingFlow(activity, SKU)
    }

    init {
        prefForPurchaseStatus.addDelegate( object : Pref.Delegate<PurchaseStatus > {
            override fun prefHasChanged(pref: Pref<PurchaseStatus>, value: PurchaseStatus) {
                delegationMgr.notifyAll { it.purchaseStatusHasChanged(value) }
            }
        })
        prefForPrice.addDelegate( object : Pref.Delegate<String> {
            override fun prefHasChanged(pref: Pref<String>, value: String) {
                delegationMgr.notifyAll { it.priceHasChanged(value) }
            }
        })

        billingEngine.addDelegate(object : BillingEngine.Delegate {
            override fun onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses: Map<String, PurchaseStatus>) {
                prefForPurchaseStatus.value = SKUsWithPurchaseStatuses[SKU] ?: PurchaseStatus.Unspecified
            }
            override fun onPricesLoadedOrChanged(SKUsWithPrices: Map<String, String>) {
                SKUsWithPrices[SKU]?.let { prefForPrice.value = it }
            }
        })
    }

}