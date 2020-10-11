/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arealapps.ultimatecheckers.*
import com.arealapps.ultimatecheckers.activityWidgets.buyPremiumActivity.PurchaseButton
import com.arealapps.ultimatecheckers.dialogs.ThanksForPurchasingDialog
import com.arealapps.ultimatecheckers.managers.purchase_manager.PurchasableItem
import com.arealapps.ultimatecheckers.managers.purchase_manager.PurchasesManager
import com.arealapps.ultimatecheckers.managers.purchase_manager.core.PurchaseStatus

class BuyPremiumActivity : AppCompatActivity() {

    private val purchasesManager = appRoot.purchasesManager
    private lateinit var noAdsPurchaseButton: PurchaseButton
    private lateinit var premiumVersionPurchaseButton: PurchaseButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_premium)

        noAdsPurchaseButton =
            PurchaseButton(
                findViewById(R.id.purchasePremiumActivity_noAdsButton),
                purchasesManager.noAds,
                R.string.noAds_purchaseButton,
                R.string.alreadyPurchased,
                R.string.purchasingNotAvailable,
                this
            )

        premiumVersionPurchaseButton =
            PurchaseButton(
                findViewById(R.id.purchasePremiumActivity_premiumVersionButton),
                purchasesManager.premiumVersion,
                R.string.premiumVersion_purchaseButton,
                R.string.alreadyPurchased,
                R.string.purchasingNotAvailable,
                this
            )

        purchasesManager.refreshPurchases()
        purchasesManager.refreshPrices()

        addListenersToPurchasableItems()

    }

    override fun onDestroy() {
        super.onDestroy()
        removeListenersFromPurchasableItems()
    }

    private val noAdsListener = object : PurchasableItem.Listener {
        override fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus) {
            if (purchaseStatus == PurchaseStatus.Purchased) {
                ThanksForPurchasingDialog(this@BuyPremiumActivity, PurchasesManager.PurchasableItems.NoAds)
            }
        }
        override fun priceHasChanged(purchaseStatus: String) {}
    }
    private val premiumVersionListener = object : PurchasableItem.Listener {
        override fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus) {
            if (purchaseStatus == PurchaseStatus.Purchased) {
                ThanksForPurchasingDialog(this@BuyPremiumActivity, PurchasesManager.PurchasableItems.PremiumVersion)
            }
        }
        override fun priceHasChanged(purchaseStatus: String) {}
    }
    private fun addListenersToPurchasableItems() {
        purchasesManager.noAds.addListener(noAdsListener)
        purchasesManager.premiumVersion.addListener(premiumVersionListener)
    }
    private fun removeListenersFromPurchasableItems() {
        purchasesManager.noAds.removeListener(noAdsListener)
        purchasesManager.premiumVersion.removeListener(premiumVersionListener)
    }

}


