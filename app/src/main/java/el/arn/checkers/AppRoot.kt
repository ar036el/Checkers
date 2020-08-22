package el.arn.checkers

import android.content.Context
import androidx.annotation.StringRes
import el.arn.checkers.dialogs.RateUsDialogInvoker
import el.arn.checkers.dialogs.RateUsDialogInvokerImpl
import el.arn.checkers.managers.ACRA.CustomReportSenderFactory
import el.arn.checkers.managers.feedback_manager.FeedbackManager
import el.arn.checkers.game.*
import el.arn.checkers.game.NewGameFactory
import el.arn.checkers.managers.*
import el.arn.checkers.managers.preferences_managers.GamePreferencesManager
import el.arn.checkers.managers.preferences_managers.SettingsPreferencesManagerAsBridge
import el.arn.checkers.managers.purchase_manager.PurchasesManager
import org.acra.annotation.AcraCore

lateinit var appRoot: AppRoot

@AcraCore(
    buildConfigClass = BuildConfig::class,
    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
)
class AppRoot : android.app.Application() { //todo organize all this.. appRoot doesn't say much about all this rootAccesses

    lateinit var userFeedbackManager: FeedbackManager
    lateinit var purchasesManager: PurchasesManager
    lateinit var toastMessageManager: ToastManager
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


    fun getStringRes(@StringRes stringRes: Int) = resources.getString(stringRes)


    var gameCoordinator: GameCoordinator? = null

    override fun onCreate() {
        super.onCreate()

        appRoot = this

        userFeedbackManager = FeedbackManager()
        purchasesManager = PurchasesManager(this)
        toastMessageManager = ToastManager(this)
        gamePreferencesManager = GamePreferencesManager()
        settingsPreferencesManager = SettingsPreferencesManagerAsBridge(gamePreferencesManager, purchasesManager)
        rateUsDialogInvoker = RateUsDialogInvokerImpl(this)
        timer = TimerImpl(null)
        soundEffectsManager = SoundEffectsManagerImplBySoundPool(gamePreferencesManager)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        ACRA.init(this)
    }


}