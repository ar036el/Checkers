/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package el.arn.opencheckers

import android.content.Context
import el.arn.opencheckers.dialogs.RateUsDialogInvoker
import el.arn.opencheckers.dialogs.RateUsDialogInvokerImpl
import el.arn.opencheckers.managers.acra.CustomReportSenderFactory
import el.arn.opencheckers.managers.feedback_manager.FeedbackManager
import el.arn.opencheckers.gameCore.*
import el.arn.opencheckers.gameCore.NewGameFactory
import el.arn.opencheckers.managers.*
import el.arn.opencheckers.managers.feedback_manager.FeedbackManagerImpl
import el.arn.opencheckers.managers.preferences_managers.GamePreferencesManager
import el.arn.opencheckers.managers.preferences_managers.SettingsPreferencesManagerAsBridge
import el.arn.opencheckers.managers.purchase_manager.PurchasesManager
import el.arn.opencheckers.managers.purchase_manager.PurchasesManagerImpl
import org.acra.ACRA
import org.acra.annotation.AcraCore

lateinit var appRoot: AppRoot

@AcraCore(
    buildConfigClass = BuildConfig::class,
    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
)
class AppRoot : android.app.Application() { //todo can be further organized

    lateinit var userFeedbackManager: FeedbackManager
    lateinit var purchasesManager: PurchasesManager
    lateinit var toastManager: ToastManager
    lateinit var gamePreferencesManager: GamePreferencesManager
    lateinit var settingsPreferencesManager: SettingsPreferencesManagerAsBridge
    lateinit var rateUsDialogInvoker: RateUsDialogInvoker
    lateinit var timer: Timer
    lateinit var soundEffectsManager: SoundEffectsManager

    //todo I think all needs to be lateinit for safety sake and general rule
    val undoRedoDataBridge: UndoRedoDataBridge = UndoRedoDataBridgeImpl()
    val undoRedoDataBridgeSideA: UndoRedoDataBridgeSideA = undoRedoDataBridge as UndoRedoDataBridgeSideA
    val undoRedoDataBridgeSideB: UndoRedoDataBridgeSideB = undoRedoDataBridge as UndoRedoDataBridgeSideB
    var newGameFactory = NewGameFactory(this)

    var gameCoordinator: GameCoordinator? = null


    override fun onCreate() {
        super.onCreate()
        appRoot = this
        initAllVars()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }

    private fun initAllVars() {
        userFeedbackManager = FeedbackManagerImpl()
        purchasesManager = PurchasesManagerImpl(this)
        toastManager = ToastManagerImpl(this)
        gamePreferencesManager = GamePreferencesManager()
        settingsPreferencesManager = SettingsPreferencesManagerAsBridge(gamePreferencesManager, purchasesManager)
        rateUsDialogInvoker = RateUsDialogInvokerImpl(this)
        timer = TimerImpl(null)
        soundEffectsManager = SoundEffectsManagerImplBySoundPool(gamePreferencesManager)
    }

}