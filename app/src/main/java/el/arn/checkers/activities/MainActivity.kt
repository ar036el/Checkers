package el.arn.checkers.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import el.arn.checkers.R
import el.arn.checkers.activity_widgets.main_activity.UndoRedoFabs
import el.arn.checkers.activity_widgets.main_activity.UndoRedoFabsImpl
import el.arn.checkers.activity_widgets.main_activity.WinnerMessage
import el.arn.checkers.activity_widgets.main_activity.WinnerMessageImpl
import el.arn.checkers.activity_widgets.main_activity.board.PiecesManager
import el.arn.checkers.activity_widgets.main_activity.board.PiecesManagerImpl
import el.arn.checkers.activity_widgets.main_activity.board.TilesManager
import el.arn.checkers.activity_widgets.main_activity.board.TilesManager_impl
import el.arn.checkers.activity_widgets.main_activity.toolbar.ToolbarAbstract
import el.arn.checkers.activity_widgets.main_activity.toolbar.ToolbarSide
import el.arn.checkers.activity_widgets.main_activity.toolbar.ToolbarTop
import el.arn.checkers.appRoot
import el.arn.checkers.helpers.functions.late_invocation_function.GateByNumberOfCalls
import el.arn.checkers.helpers.functions.late_invocation_function.LateInvocationFunction
import el.arn.checkers.helpers.functions.LimitedAccessFunction
import el.arn.checkers.helpers.android.OrientationOptions
import el.arn.checkers.helpers.android.isDirectionRTL
import el.arn.checkers.helpers.android.orientation
import el.arn.checkers.helpers.game_enums.GameTypeEnum
import el.arn.checkers.helpers.game_enums.StartingPlayerEnum
import el.arn.checkers.dialogs.ConfigHasChangedWarningDialog
import el.arn.checkers.dialogs.Dialog
import el.arn.checkers.dialogs.FeedbackDialog
import el.arn.checkers.dialogs.NewGameDialog
import el.arn.checkers.game.UndoRedoDataBridge
import el.arn.checkers.game.game_core.checkers_game.structs.Player
import el.arn.checkers.helpers.android.stringFromRes
import el.arn.checkers.managers.Timer
import el.arn.checkers.managers.external_activity_invoker.GooglePlayStoreAppPageInvoker
import el.arn.checkers.managers.external_activity_invoker.UrlInvoker
import el.arn.checkers.managers.preferences_managers.Preference
import el.arn.checkers.managers.purchase_manager.core.PurchaseStatus


class MainActivity : AppCompatActivity() {

    private lateinit var activityLayout: DrawerLayout
    private lateinit var sideDrawer: NavigationView
    private lateinit var toolbar: ToolbarAbstract
    private lateinit var undoRedoFabs: UndoRedoFabs
    private lateinit var piecesManager: PiecesManager
    private lateinit var tilesManager: TilesManager
    private lateinit var winnerMessage: WinnerMessage

    private var dialogBeingShown: Dialog? = null

    private val areDebugButtonsEnabled = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setSupportActionBar(findViewById(R.id.toolbar_top))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initMobileAds()
        initActivityLayout()
        initSideDrawer()
        initToolbarAsync()
        initTheCheckersBoardAsync()
        initUndoRedoFabsAsync()
        initWinnerMessage()
        initTimer()
        initDebugButtons()

        appRoot.undoRedoDataBridge.addListener(undoRedoDataBridgeListener)
        appRoot.gamePreferencesManager.boardSize.addListener(preferenceChangedListenerForBoardSizeAndStartingRows)
        appRoot.gamePreferencesManager.startingRows.addListener(preferenceChangedListenerForBoardSizeAndStartingRows)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeAllListeners()
    }

    override fun onPause() {
        super.onPause()
        appRoot.gameCoordinator?.isPaused = true
    }

    override fun onResume() {
        super.onResume()
        appRoot.gameCoordinator?.isPaused = false
        openDialogConfigHasChangedWarningDialog.invokeIfHasAccess()
        tryToLoadBannerAd()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        //all needs location-related variables that become available only here
        initUndoAndRedoFabs.invokeIfHasAccess()
        initPiecesManager.invokeIfHasAccess()
        initToolbarSide.invokeIfHasAccess()
        hideNavigationBarOrKeepHidden()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (orientation == OrientationOptions.Portrait) {
            initToolbarTop(menu)
        }
        return true
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }


    private fun initDebugButtons() {
        val debugButtonA: FloatingActionButton = findViewById(R.id.debugButtonA)
        val debugButtonB: FloatingActionButton = findViewById(R.id.debugButtonB)
        val debugButtonC: FloatingActionButton = findViewById(R.id.debugButtonC)
        val debugButtonAContainer: ViewGroup = findViewById(R.id.debugButtonAContainer)
        val debugButtonBContainer: ViewGroup = findViewById(R.id.debugButtonBContainer)
        val debugButtonCContainer: ViewGroup = findViewById(R.id.debugButtonCContainer)

        if (!areDebugButtonsEnabled) {
            debugButtonAContainer.visibility = View.GONE
            debugButtonBContainer.visibility = View.GONE
            debugButtonCContainer.visibility = View.GONE
        } else {

            debugButtonA.setOnClickListener{
                val newPurchaseStatus = if (appRoot.purchasesManager.purchasedNoAds) PurchaseStatus.UnspecifiedOrNotPurchased else PurchaseStatus.Purchased
                appRoot.purchasesManager.noAds.forceDebug(newPurchaseStatus)
                appRoot.toastManager.showShort("debug: no ads purchase status: " + newPurchaseStatus.name)
            }
            debugButtonB.setOnClickListener{
                val newPurchaseStatus = if (appRoot.purchasesManager.purchasedPremiumVersion) PurchaseStatus.UnspecifiedOrNotPurchased else PurchaseStatus.Purchased
                appRoot.purchasesManager.premiumVersion.forceDebug(newPurchaseStatus)
                appRoot.toastManager.showShort("debug: premium version purchase status: " + newPurchaseStatus.name)
            }
//        findViewById<FloatingActionButton>(R.id.testo3).setOnClickListener{
//            throw InternalError("erroru")
//        }
        }
    }

    private fun initTimer() {
        appRoot.timer.hostingActivityForUiThread = this
        appRoot.timer.addListener(timerListener)
    }

    private fun hideNavigationBarOrKeepHidden() {
        val decorView: View = window.decorView
        val uiOptions: Int = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
    }

    private fun initMobileAds() {
        MobileAds.initialize(this) {}
        rotateSideAdView90DegreesAndAlignToLayoutStart()
        updateBannerAdViewsVisibility()
    }

    private fun updateBannerAdViewsVisibility() {
        val adViewSide: AdView = findViewById(R.id.mainActivity_bannerAdViewSide)
        val adViewBottom: AdView = findViewById(R.id.mainActivity_bannerAdViewBottom)

        if (appRoot.purchasesManager.purchasedNoAds) {
            adViewSide.visibility = View.GONE
            adViewBottom.visibility = View.GONE
        }
        if (orientation == OrientationOptions.Portrait) {
            adViewSide.visibility = View.GONE
        } else {
            adViewBottom.visibility = View.GONE
        }
    }

    private fun rotateSideAdView90DegreesAndAlignToLayoutStart() {
        val adViewSide: AdView = findViewById(R.id.mainActivity_bannerAdViewSide)
        val h = AdSize.BANNER.getHeightInPixels(this)
        val w = AdSize.BANNER.getWidthInPixels(this)
        adViewSide.rotation = 90f
        adViewSide.x += if (!isDirectionRTL) -(w-h)/2 else (w-h)/2
    }

    private fun tryToLoadBannerAd() {
        updateBannerAdViewsVisibility()
        if (appRoot.purchasesManager.purchasedNoAds) {
            return
        }
        val id = if (orientation == OrientationOptions.Portrait) R.id.mainActivity_bannerAdViewBottom else R.id.mainActivity_bannerAdViewSide
        val bannerAdView: AdView = findViewById(id)
        val adRequest = AdRequest.Builder().build()
        bannerAdView.loadAd(adRequest)
    }

    private fun initWinnerMessage() {
        winnerMessage = WinnerMessageImpl(findViewById(R.id.winnerMessage))
        if (hasAGameFinished()) {
            appRoot.gameCoordinator?.winningTypeIfGameWasFinished?.let {
                winnerMessage.show(false, it)
            }
        }
        winnerMessage.addListener(winnerMessageListener)
        fillUpBoardIfGameIsActuallyOnAndActivityWasCreatedBecauseDeviceWasJustChangedInOrientation.trigger(
            3
        )
    }

    private val winnerMessageListener = object : WinnerMessage.Listener {
        var postDelayedHandler: Handler? = null
        override fun messageAnimationWasFinished() {
            postDelayedHandler = Handler()
            postDelayedHandler!!.postDelayed({
                val dialog = appRoot.rateUsDialogInvoker.tryToInvokeDialog(this@MainActivity)
                if (dialog != null) {
                    this@MainActivity.dialogBeingShown = dialog
                } else {
                    openDialogNewGame()
                }
            }, 1000)
        }

        override fun messageWasClickedWhenMessageIsShown() {
            winnerMessage.hide()
            openDialogNewGame()
        }

        override fun stateWasChanged(state: WinnerMessage.States) {
            if (state == WinnerMessage.States.Hidden) {
                postDelayedHandler?.removeCallbacksAndMessages(null)
            }
        }

    }

    private fun hasAGameFinished(): Boolean = appRoot.gameCoordinator?.isGameOn == false

    private fun initUndoRedoFabsAsync() {
        initUndoAndRedoFabs.grantOneAccess()
    }

    private fun initActivityLayout() {
        activityLayout = findViewById(R.id.mainActivity_mainActivityDrawerLayout)
    }

    private fun initSideDrawer() {
        sideDrawer = findViewById(R.id.mainActivity_navigationViewSideDrawer)
        initSideDrawerMenuItems(sideDrawer.menu)
    }

    private fun initSideDrawerMenuItems(sideDrawerMenu: Menu) {
        sideDrawerMenu.findItem(R.id.sideDrawerMenuItem_rateUs).setOnMenuItemClickListener { GooglePlayStoreAppPageInvoker(this
        ).open(); closeSideDrawer(); true }
        sideDrawerMenu.findItem(R.id.sideDrawerMenuItem_purchasePremiumVersion).setOnMenuItemClickListener { openActivityBuyPremiumActivity(); closeSideDrawer(); true }
        sideDrawerMenu.findItem(R.id.sideDrawerMenuItem_purchaseNoAds).setOnMenuItemClickListener { openActivityBuyPremiumActivity(); closeSideDrawer(); true }
        ///-------------------
        sideDrawerMenu.findItem(R.id.sideDrawerMenuItem_contactUs).setOnMenuItemClickListener { openIntentSendEmailToDeveloper(); closeSideDrawer(); true }
        sideDrawerMenu.findItem(R.id.sideDrawerMenuItem_sendFeedback).setOnMenuItemClickListener { openDialogSendFeedback(); closeSideDrawer(); true }
        sideDrawerMenu.findItem(R.id.sideDrawerMenuItem_privacyPolicy).setOnMenuItemClickListener { openUrlAppPrivacyPolicy(); closeSideDrawer(); true }
        sideDrawerMenu.findItem(R.id.sideDrawerMenuItem_settings).setOnMenuItemClickListener { openActivitySettingsActivity(); closeSideDrawer(); true }
    }

    private fun getScreenMinLength() : Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels.coerceAtMost(displayMetrics.widthPixels)
    }

    private fun initLayoutParamsOfBoardRelatedViews(boardSizeInPx: Int) {
        fun setLayout(idRes: Int) {
            findViewById<View>(idRes).layoutParams =
                FrameLayout.LayoutParams(
                    boardSizeInPx,
                    boardSizeInPx
                )
        }
        setLayout(R.id.boardBackground)
        setLayout(R.id.boardPiecesContainer)
        setLayout(R.id.boardTilesContainer)
        setLayout(R.id.boardTilesCoverContainer)
    }

    private fun initTheCheckersBoardAsync() {
        val boardSizeInPx = getScreenMinLength()

        initLayoutParamsOfBoardRelatedViews(boardSizeInPx)

        initTilesManager(boardSizeInPx.toFloat())
        initPiecesManager.grantOneAccess()
        fillUpBoardIfGameIsActuallyOnAndActivityWasCreatedBecauseDeviceWasJustChangedInOrientation.willInvokeWhenAllGatesAreTriggeredAndOpen()
    }

    private fun initTilesManager(boardSizeInPx: Float) {
        tilesManager = TilesManager_impl(
            findViewById(R.id.boardTilesContainer),
            findViewById(R.id.boardTilesCoverContainer),
            boardSizeInPx,
            appRoot.gamePreferencesManager.boardSize.value,
            true
        )
        tilesManager.addListener(tilesManagerListener)
        fillUpBoardIfGameIsActuallyOnAndActivityWasCreatedBecauseDeviceWasJustChangedInOrientation.trigger(
            2
        )
    }

    private val tilesManagerListener = object : TilesManager.Listener {
        override fun boardWasClickedWhenSelectionIsDisabled() {
            if (appRoot.gameCoordinator?.isGameOn != true) {
                openDialogNewGame()
            }
        }
    }

    //late invocation function: invokes only after all 3 activity components are initialized
    private val fillUpBoardIfGameIsActuallyOnAndActivityWasCreatedBecauseDeviceWasJustChangedInOrientation = LateInvocationFunction(
        {
            if (appRoot.gameCoordinator != null) {
                appRoot.gameCoordinator?.replaceActivityComponents(
                    piecesManager,
                    tilesManager,
                    toolbar,
                    winnerMessage
                )
            }
        },
        true,
        true,
        GateByNumberOfCalls(1),
        GateByNumberOfCalls(1),
        GateByNumberOfCalls(1),
        GateByNumberOfCalls(
            1
        )
    )

    private val initPiecesManager = LimitedAccessFunction({
        piecesManager = PiecesManagerImpl(
            findViewById(R.id.boardPiecesContainer),
            appRoot.gamePreferencesManager.boardSize.value,
            tilesManager.tileLengthInPx,
            tilesManager.tilesLocationInWindow
        )
        piecesManager.addListener(piecesManagerListener)

        fillUpBoardIfGameIsActuallyOnAndActivityWasCreatedBecauseDeviceWasJustChangedInOrientation.trigger(
            0
        )
    })

    private val initUndoAndRedoFabs = LimitedAccessFunction({
        undoRedoFabs =
            UndoRedoFabsImpl(
                appRoot.undoRedoDataBridgeSideA,
                findViewById(R.id.undoButton_FAB),
                findViewById(R.id.redoButton_FAB),
                this,
                isDirectionRTL
            )

        undoRedoFabs.addListener(undoRedoFabsListener)
    })

    private val openDialogConfigHasChangedWarningDialog = LimitedAccessFunction({
        dismissDialogIfOneIsOpen()
        dialogBeingShown = ConfigHasChangedWarningDialog(this) { openDialogNewGame() }
    })

    private fun initToolbarAsync() {
        val toolbarLayoutTop: Toolbar = findViewById(R.id.toolbar_top)
        val toolbarLayoutSide: LinearLayout = findViewById(R.id.toolbar_side)
        val progressBarTop: ProgressBar = findViewById(R.id.progressBarTop)
        val progressBarSide: ProgressBar = findViewById(R.id.progressBar_side)

        if (orientation == OrientationOptions.Portrait) {
            toolbarLayoutSide.visibility = View.GONE
            progressBarSide.visibility = View.GONE

            //continues in [onCreateOptionsMenu]
        } else {
            toolbarLayoutTop.visibility = View.GONE
            progressBarTop.visibility = View.GONE
            initToolbarSide.grantOneAccess()
        }
    }

    private fun initToolbarTop(menu: Menu) {
        if (this::toolbar.isInitialized) { throw InternalError() }
        val toolbar = ToolbarTop(findViewById(R.id.toolbar_top), this, menu, menuInflater)
        toolbar.addListener(toolbarListener)
        this.toolbar = toolbar
        fillUpBoardIfGameIsActuallyOnAndActivityWasCreatedBecauseDeviceWasJustChangedInOrientation.trigger(
            1
        )
    }

    private val initToolbarSide = LimitedAccessFunction({
        if (orientation == OrientationOptions.Landscape) {
            if (this@MainActivity::toolbar.isInitialized) {
                throw InternalError()
            }
            val toolbar = ToolbarSide(findViewById(R.id.toolbar_side), this)
            toolbar.addListener(toolbarListener)
            this.toolbar = toolbar
            fillUpBoardIfGameIsActuallyOnAndActivityWasCreatedBecauseDeviceWasJustChangedInOrientation.trigger(
                1
            )
        }
    })

    private fun startANewGame(
        startingPlayer: StartingPlayerEnum,
        gameType: GameTypeEnum,
        doesUserPlayFirst: Boolean
    ) {
        if (!this::piecesManager.isInitialized || !this::tilesManager.isInitialized || !this::toolbar.isInitialized) {
            Log.d(
                "MainActivity",
                "startANewGame(): function params not yet initialized. function was terminated"
            )
            return
        }
        when (gameType) {
            GameTypeEnum.SinglePlayer -> {
                appRoot.newGameFactory.startNewSinglePlayerGame(
                    startingPlayerEnumToPlayerEnum(startingPlayer),
                    doesUserPlayFirst,
                    piecesManager,
                    tilesManager,
                    toolbar,
                    winnerMessage,
                    appRoot.undoRedoDataBridgeSideB
                )
            }
            GameTypeEnum.Multiplayer -> {
                appRoot.newGameFactory.startNewMultiplayerGame(
                    startingPlayerEnumToPlayerEnum(startingPlayer),
                    piecesManager,
                    tilesManager,
                    toolbar,
                    winnerMessage,
                    appRoot.undoRedoDataBridgeSideB
                )
            }
            GameTypeEnum.VirtualGame -> {
                appRoot.newGameFactory.startNewVirtualGame(
                    startingPlayerEnumToPlayerEnum(startingPlayer),
                    piecesManager,
                    tilesManager,
                    toolbar,
                    winnerMessage,
                    appRoot.undoRedoDataBridgeSideB
                )
            }
        }
    }

    private fun startingPlayerEnumToPlayerEnum(startingPlayer: StartingPlayerEnum): Player {
        return when (startingPlayer) {
            StartingPlayerEnum.White -> Player.White
            StartingPlayerEnum.Black -> Player.Black
            StartingPlayerEnum.Random -> listOf(Player.White, Player.Black).random()
        }
    }

    private fun openActivitySettingsActivity() {
        val settingsActivity = Intent(this, SettingsActivity::class.java)
        startActivity(settingsActivity)
    }

    private fun openActivityBuyPremiumActivity() {
        val buyPremiumActivity = Intent(this, BuyPremiumActivity::class.java)
        startActivity(buyPremiumActivity)
    }

    private fun openIntentSendEmailToDeveloper() {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(stringFromRes(R.string.internal_developerEmail)))
//        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email")
//        i.putExtra(Intent.EXTRA_TEXT, "body of email")
        try {
            startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this@MainActivity,
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openUrlAppPrivacyPolicy() {
        UrlInvoker(stringFromRes(R.string.internal_privacyPolicyURL), this).open()
    }

    private fun openSideDrawer() {
        if (!this::activityLayout.isInitialized) {
            Log.d(
                "MainActivity",
                "openSideDrawer(): function param not yet initialized. function was terminated"
            )
            return
        }
        if (!activityLayout.isDrawerOpen(GravityCompat.START)) {
            activityLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun closeSideDrawer() {
        if (!this::activityLayout.isInitialized) {
            Log.d(
                "MainActivity",
                "openSideDrawer(): function param not yet initialized. function was terminated"
            ) //todo see that this check is everywhere there is a "floating function" like this
            return
        }
        if (activityLayout.isDrawerOpen(GravityCompat.START)) {
            activityLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun removeAllListeners() {
        toolbar.removeListener(toolbarListener)
        piecesManager.removeListener(piecesManagerListener)
        tilesManager.removeListener(tilesManagerListener)
        undoRedoFabs.removeListener(undoRedoFabsListener)
        winnerMessage.removeListener(winnerMessageListener)
        appRoot.timer.removeListener(timerListener)
        appRoot.undoRedoDataBridge.removeListener(undoRedoDataBridgeListener)
        appRoot.gamePreferencesManager.boardSize.removeListener(
            preferenceChangedListenerForBoardSizeAndStartingRows
        )
        appRoot.gamePreferencesManager.startingRows.removeListener(
            preferenceChangedListenerForBoardSizeAndStartingRows
        )
    }



    private val toolbarListener = object: ToolbarAbstract.Listener {
        override fun menuButtonWasClicked() {
            openSideDrawer()
        }

        override fun undoButtonWasClicked() {
            if (appRoot.undoRedoDataBridge.isEnabled) {
                appRoot.undoRedoDataBridgeSideA.undo()
            }
        }

        override fun redoButtonWasClicked() {
            if (appRoot.undoRedoDataBridge.isEnabled) {
                appRoot.undoRedoDataBridgeSideA.redo()
            }
        }

        override fun newGameButtonWasClicked() {
            openDialogNewGame()
        }

        override fun settingsButtonWasClicked() {
            openActivitySettingsActivity()
        }
    }

    private fun dismissDialogIfOneIsOpen() {
        if (dialogBeingShown?.isShowing == true) {
            dialogBeingShown?.dismiss()
        } else {
            dialogBeingShown = null
        }
    }

    private fun openDialogNewGame() {
        dismissDialogIfOneIsOpen()
        dialogBeingShown = NewGameDialog(this, appRoot.purchasesManager.purchasedPremiumVersion) { startingPlayer, gameType, _, doesUserPlayFirst ->
            startANewGame(startingPlayer, gameType, doesUserPlayFirst)
        }
    }

    private fun openDialogSendFeedback() {
        dismissDialogIfOneIsOpen()
        dialogBeingShown = FeedbackDialog(this, appRoot.userFeedbackManager, null)
    }

    private val preferenceChangedListenerForBoardSizeAndStartingRows = object : Preference.Listener<Int> {
        override fun prefHasChanged(preference: Preference<Int>, value: Int) {
            openDialogConfigHasChangedWarningDialog.grantOneAccess()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                openDialogConfigHasChangedWarningDialog.invokeIfHasAccess()
            }
        }
    }

    private val undoRedoFabsListener = object : UndoRedoFabs.Listener {
        override fun undoFabWasClicked() {
            if (appRoot.undoRedoDataBridge.isEnabled) {
                appRoot.undoRedoDataBridgeSideA.undo()
            }
        }

        override fun redoFabsWasClicked() {
            if (appRoot.undoRedoDataBridge.isEnabled) {
                appRoot.undoRedoDataBridgeSideA.redo()
            }
        }

    }

    private val piecesManagerListener =  object: PiecesManager.Listener { //todo why here?
        override fun animationHasStarted() {
            appRoot.undoRedoDataBridgeSideA.isEnabled = false
        }

        override fun animationHasFinished() {
            appRoot.undoRedoDataBridgeSideA.isEnabled = true
        }

        override fun piecesWereLoaded() {
            appRoot.undoRedoDataBridgeSideA.isEnabled = true
        }
    }

    private val undoRedoDataBridgeListener = object : UndoRedoDataBridge.Listener {
        override fun stateWasChangedOrReloaded(
            isEnabled: Boolean,
            canUndo: Boolean,
            canRedo: Boolean
        ) {
            if (this@MainActivity::toolbar.isInitialized) {
                toolbar.undoButtonEnabled = canUndo
                toolbar.redoButtonEnabled = canRedo
            }
            if (this@MainActivity::undoRedoFabs.isInitialized) {
                if (canUndo) undoRedoFabs.enableUndoButton() else undoRedoFabs.disableUndoButton()
                if (canRedo) undoRedoFabs.showRedoButton(true) else undoRedoFabs.hideRedoButton(true)
            }
        }
    }

    private val timerListener = object : Timer.Listener {
        override fun timeWasChanged(timer: Timer, timeInSeconds: Int) {
            if (this@MainActivity::toolbar.isInitialized) {
                toolbar.timerTimeInSeconds = timeInSeconds
            }
        }
    }


}