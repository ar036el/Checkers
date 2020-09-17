/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.ultimatecheckers.activity_widgets.buy_premium_activity

import android.app.Activity
import android.widget.Button
import androidx.annotation.StringRes
import el.arn.ultimatecheckers.*
import el.arn.ultimatecheckers.helpers.android.activity
import el.arn.ultimatecheckers.helpers.android.isAlive
import el.arn.ultimatecheckers.helpers.android.stringFromRes
import el.arn.ultimatecheckers.helpers.listeners_engine.LimitedListener
import el.arn.ultimatecheckers.helpers.listeners_engine.LimitedListenerImpl
import el.arn.ultimatecheckers.managers.purchase_manager.PurchasableItem
import el.arn.ultimatecheckers.managers.purchase_manager.core.PurchaseStatus

class PurchaseButton(
    private val buttonView: Button,
    private val purchasableItem: PurchasableItem,
    @StringRes private val purchaseActionStringRes: Int,
    @StringRes private val alreadyPurchasedStringRes: Int,
    @StringRes private val cannotPurchaseMessageStringRes: Int,
    private val activity: Activity
) {
    companion object {
        const val PRICE_STRING_FLAG = "@pr"
    }

    var canPurchase: Boolean = false
        private set(value) {
            if (value) {
                buttonView.text = textReadyToPurchase
                buttonView.isEnabled = true
            } else {
                buttonView.text = stringFromRes(alreadyPurchasedStringRes)
                buttonView.isEnabled = false
            }
            field = value
        }


    private val textReadyToPurchase: String
        get() = stringFromRes(purchaseActionStringRes).replace(PRICE_STRING_FLAG, purchasableItem.price, true)

    private fun updateState() {
        canPurchase = (purchasableItem.purchaseStatus != PurchaseStatus.Purchased)
    }

    private fun tryToLaunchBillingFlow() {
        val successful = purchasableItem.tryToLaunchBillingFlow(activity)
        if (!successful) {
            appRoot.toastManager.showLong(stringFromRes(R.string.purchasingNotAvailable))
        }
    }

    init {
        updateState()
        purchasableItem.addListener(
            object : PurchasableItem.Listener, LimitedListener by LimitedListenerImpl(destroyIf = {!buttonView.activity.isAlive}) {
                override fun purchaseStatusHasChanged(purchaseStatus: PurchaseStatus) { updateState() }
                override fun priceHasChanged(purchaseStatus: String) { updateState() }
            }
        )
        buttonView.setOnClickListener {
            tryToLaunchBillingFlow()
        }
    }

}