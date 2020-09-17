/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.managers.purchase_manager.core

import android.content.Context
import el.arn.ultimatecheckers.appRoot
import el.arn.ultimatecheckers.helpers.listeners_engine.*
import el.arn.ultimatecheckers.helpers.NonNullMap
import el.arn.ultimatecheckers.managers.preferences_managers.PreferencesManager
import el.arn.ultimatecheckers.managers.preferences_managers.PreferencesManagerImpl
import el.arn.ultimatecheckers.managers.purchase_manager.PurchasableItem
import el.arn.ultimatecheckers.managers.purchase_manager.PurchasableItemImpl

interface GenericPurchasesManager {
    fun getPurchasableItem(SKU: String): PurchasableItem
    fun refreshPurchases(doWhenRefreshed: ((PurchasableItemsWithPurchaseStatuses: Map<PurchasableItem, PurchaseStatus>) -> Unit)? = null)
    fun refreshPrices(doWhenRefreshed: ((PurchasableItemsWithPrices: Map<PurchasableItem, String?>) -> Unit)? = null)
}

open class GenericPurchasesManagerImpl(
    private val context: Context,
    private val SKUs: Set<String>
) : GenericPurchasesManager {
    private val SKUsWithPurchasableItems: NonNullMap<String, PurchasableItem>
    private val purchasesPreferencesManager: PreferencesManager = PreferencesManagerImpl(appRoot.getSharedPreferences("purchases", Context.MODE_PRIVATE))
    private val billingEngine =
        BillingEngineImpl(SKUs)

    override fun getPurchasableItem(SKU: String): PurchasableItem = SKUsWithPurchasableItems[SKU]

    override fun refreshPurchases(doWhenRefreshed: ((PurchasableItemsWithPurchaseStatuses: Map<PurchasableItem, PurchaseStatus>) -> Unit)?) {
        billingEngine.addListener(
            object : BillingEngine.Listener, LimitedListener by LimitedListenerImpl(destroyAfterCall = true) {
                override fun onPurchaseStatusesLoadedOrChanged(SKUsWithPurchaseStatuses: Map<String, PurchaseStatus>) {
                    val map = SKUs.toList().map {SKU ->
                        (SKUsWithPurchasableItems[SKU]) to (SKUsWithPurchaseStatuses[SKU] ?: PurchaseStatus.UnspecifiedOrNotPurchased) }
                        .toMap()
                    doWhenRefreshed?.invoke(map)
                }
            }
        )
        billingEngine.reloadPurchases()
    }

    override fun refreshPrices(doWhenRefreshed: ((PurchasableItemsWithPrices: Map<PurchasableItem, String?>) -> Unit)?) {
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
