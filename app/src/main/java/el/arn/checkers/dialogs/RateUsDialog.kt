package el.arn.checkers.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.dialogs.composites.FiveStarsLayout
import el.arn.checkers.tools.external_activity_invoker.GooglePlayStoreAppPageInvoker

class RateUsDialog(
    private val dialogInvoker: RateUsDialogInvoker,
    private val activity: Activity
) : Dialog {

    override val isShowing: Boolean
        get() = dialog.isShowing
    override fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    private val layout = activity.layoutInflater.inflate(R.layout.dialog_rate_us, null)
    private val positiveMessageTextView: TextView = layout.findViewById(R.id.rateUsDialog_positiveMessage)
    private val negativeMessageTextView: TextView = layout.findViewById(R.id.rateUsDialog_negativeMessage)
    private val dialog: AlertDialog

    private var ratingInStars = 0

    enum class FeedbackState { NeutralFeedbackOrUnspecified, NegativeFeedback, PositiveFeedback}

    private var feedbackState: FeedbackState = FeedbackState.NeutralFeedbackOrUnspecified
        set(value) {
            when(value) {
                FeedbackState.NeutralFeedbackOrUnspecified -> setNeutralOrUnspecifiedFeedbackResponse()
                FeedbackState.NegativeFeedback -> setNegativeFeedbackResponse()
                FeedbackState.PositiveFeedback -> setPositiveFeedbackResponse()
            }
            field = value
        }

    private fun getFeedbackStateFromRating(rating: Int): FeedbackState {
        return when (rating) {
            in 1..3 -> FeedbackState.NegativeFeedback
            in 4..5 -> FeedbackState.PositiveFeedback
            else -> FeedbackState.NeutralFeedbackOrUnspecified
        }
    }

    private fun setPositiveFeedbackResponse() {
        positiveMessageTextView.visibility = View.VISIBLE
        negativeMessageTextView.visibility = View.INVISIBLE
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.rateUsDialog_button_rateInAppStore)
    }

    private fun setNegativeFeedbackResponse() {
        positiveMessageTextView.visibility = View.INVISIBLE
        negativeMessageTextView.visibility = View.VISIBLE
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.rateUsDialog_button_sendFeedBack)
    }

    private fun setNeutralOrUnspecifiedFeedbackResponse() {
        positiveMessageTextView.visibility = View.GONE
        negativeMessageTextView.visibility = View.GONE
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.dialog_ok)
    }

    private var feedbackDialogWasOpened = false
    private fun openFeedbackDialog() {
        feedbackDialogWasOpened = true
        FeedbackDialog(activity, appRoot.userFeedbackManager, ratingInStars)
    }

    private fun openAppStore() {
        GooglePlayStoreAppPageInvoker(activity).open()
    }

    private fun submitStars() {
        appRoot.userFeedbackManager.sendFeedback(ratingInStars, null)
    }

    private fun actPositiveAccordingToState() {
        when (feedbackState) {
            FeedbackState.NegativeFeedback -> openFeedbackDialog()
            FeedbackState.PositiveFeedback -> openAppStore()
            //FeedbackState.NeutralFeedbackOrUnspecified -> sumbitStars() automatically happens at onDismiss
        }
    }

    private val ratingLayout =
        FiveStarsLayout(
            layout.findViewById<LinearLayout>(R.id.fiveStarsLayout),
            activity,
            object :
                FiveStarsLayout.Listener {
                override fun onRatingChanged(rating: Int) {
                    enablePositiveDialogButtonIfDisabled()
                    ratingInStars = rating
                    feedbackState = getFeedbackStateFromRating(rating)
                }
            }
        )

    private fun enablePositiveDialogButtonIfDisabled() {
        if (!dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        }
    }

    private fun setToInvokeDialogLater() {
        dialogInvoker.invokeDialogLater(feedbackState)
    }

    private fun setToNotInvokeDialogAgain() {
        dialogInvoker.dontInvokeDialogAgain()
    }

    private var noButtonWasPressed = true

    private fun createDialog(): AlertDialog {
        val dialog =  AlertDialog.Builder(activity)
            .setView(layout)
            .setNeutralButton(R.string.rateUsDialog_button_dontShowAgain) { _,_ -> setToNotInvokeDialogAgain(); noButtonWasPressed = false }
            .setNegativeButton(R.string.rateUsDialog_button_later) { _,_ -> setToInvokeDialogLater(); noButtonWasPressed = false }
            .setPositiveButton(R.string.rateUsDialog_button_rateInAppStore) { _,_ -> actPositiveAccordingToState(); noButtonWasPressed = false }
            .setOnDismissListener {
                if (noButtonWasPressed) {
                    setToInvokeDialogLater()
                }
                submitStars()
            }
            .show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        return dialog
    }

    init {
        dialog = createDialog()

        feedbackState = getFeedbackStateFromRating(ratingLayout.rating)
    }



}