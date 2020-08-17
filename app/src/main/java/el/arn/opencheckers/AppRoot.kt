package el.arn.opencheckers

import android.content.Context
import androidx.annotation.StringRes
import el.arn.opencheckers.dialogs.RateUsDialogInvoker
import el.arn.opencheckers.dialogs.RateUsDialogInvokerImpl
import el.arn.opencheckers.tools.feedback_manager.CustomReportSenderFactory
import el.arn.opencheckers.tools.feedback_manager.FeedbackManager
import el.arn.opencheckers.game.*
import el.arn.opencheckers.game.NewGameFactory
import el.arn.opencheckers.tools.ToastManager
import el.arn.opencheckers.tools.preferences_managers.GamePreferencesManager
import el.arn.opencheckers.tools.preferences_managers.SettingsPreferencesManager
import el.arn.opencheckers.tools.purchase_manager.PurchasesManager
import org.acra.annotation.AcraCore

lateinit var appRoot: AppRoot

@AcraCore(
    buildConfigClass = BuildConfig::class,
    reportSenderFactoryClasses = [CustomReportSenderFactory::class]
)
class AppRoot : android.app.Application() {

    lateinit var userFeedbackManager: FeedbackManager
    lateinit var purchasesManager: PurchasesManager
    lateinit var toastMessageManager: ToastManager
    lateinit var gamePreferencesManager: GamePreferencesManager
    lateinit var settingsPreferencesManager: SettingsPreferencesManager
    lateinit var rateUsDialogInvoker: RateUsDialogInvoker


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
        settingsPreferencesManager = SettingsPreferencesManager(gamePreferencesManager, purchasesManager)
        rateUsDialogInvoker = RateUsDialogInvokerImpl(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        ACRA.init(this)
    }


}