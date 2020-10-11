/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.managers.purchase_manager

import android.content.Context
import com.arealapps.ultimatecheckers.managers.purchase_manager.core.GenericPurchasesManager
import com.arealapps.ultimatecheckers.managers.purchase_manager.core.GenericPurchasesManagerImpl
import com.arealapps.ultimatecheckers.managers.purchase_manager.core.PurchaseStatus

interface PurchasesManager : GenericPurchasesManager {
    val purchasedPremiumVersion: Boolean
    val purchasedNoAds: Boolean

    val noAds: PurchasableItem
    val premiumVersion: PurchasableItem

    enum class PurchasableItems { NoAds, PremiumVersion }
}


class PurchasesManagerImpl (
    context: Context
) : PurchasesManager, GenericPurchasesManagerImpl(context, setOf(noAdsSKU, premiumVersionSKU))  {

    private companion object {
        const val noAdsSKU = "no_ads" //todo not using resources because of const val. doesn't matter that much...
        const val premiumVersionSKU = "premium_version"
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