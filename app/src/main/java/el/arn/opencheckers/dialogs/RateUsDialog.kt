package el.arn.opencheckers.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import el.arn.opencheckers.R

class RateUsDialog(
    private val dialogInvoker: RateUsDialogInvoker,
    private val activity: Activity
) {

    private val layout = activity.layoutInflater.inflate(R.layout.dialog_rate_us, null)
    private val positiveMessage: TextView = layout.findViewById(R.id.rateUsDialog_positiveMessage)
    private val negativeMessage: TextView = layout.findViewById(R.id.rateUsDialog_negativeMessage)


    private val dialog: AlertDialog

    enum class FeedbackState { NeutralFeedbackOrUnspecified, NegativeFeedback, PositiveFeedback}

    private var feedbackState: FeedbackState = FeedbackState.NeutralFeedbackOrUnspecified
        set(value) {
            when(value) {
                FeedbackState.NeutralFeedbackOrUnspecified -> setNegativeFeedbackResponse()
                FeedbackState.NegativeFeedback -> setNeutralFeedbackResponse()
                FeedbackState.PositiveFeedback -> setPositiveFeedbackResponse()
            }
            field = value
        }

    private fun getFeedbackStateFromRating(rating: Int): FeedbackState {
        return when {
            rating in 1..2 -> FeedbackState.NegativeFeedback
            rating <= 4 -> FeedbackState.PositiveFeedback
            else -> FeedbackState.NeutralFeedbackOrUnspecified
        }
    }

    private fun setPositiveFeedbackResponse() {
        positiveMessage.visibility = View.VISIBLE
        negativeMessage.visibility = View.INVISIBLE
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.rateUsDialog_button_rateInAppStore)
        feedbackState = FeedbackState.PositiveFeedback
    }

    private fun setNegativeFeedbackResponse() {
        positiveMessage.visibility = View.INVISIBLE
        negativeMessage.visibility = View.VISIBLE
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.rateUsDialog_button_sendFeedBack)
        feedbackState = FeedbackState.NegativeFeedback
    }

    private fun setNeutralFeedbackResponse() {
        positiveMessage.visibility = View.INVISIBLE
        negativeMessage.visibility = View.INVISIBLE
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.dialog_ok)
        feedbackState = FeedbackState.NeutralFeedbackOrUnspecified
    }

    private var feedbackDialogWasOpened = false
    private fun openFeedbackDialog() {
        feedbackDialogWasOpened = true
    }

    private fun openAppStore() {

    }

    private fun sendStars() {

    }

    private fun actAccordingToState() {
        when (feedbackState) {
            FeedbackState.NeutralFeedbackOrUnspecified -> sendStars()
            FeedbackState.NegativeFeedback -> openFeedbackDialog()
            FeedbackState.PositiveFeedback -> openAppStore()
        }
    }

    private val ratingLayout = FiveStarsRatingLayout(
        layout.findViewById<LinearLayout>(R.id.fiveStarsLayout),
        activity,
        object : FiveStarsRatingLayout.Delegate {
            override fun onRatingChanged(rating: Int) {
                feedbackState = getFeedbackStateFromRating(rating)
            }
        }
    )

    init {
        dialog = AlertDialog.Builder(activity)
            .setView(layout)
            .setNegativeButton(R.string.rateUsDialog_button_dontShowAgain) { _,_ -> dialogInvoker.dontInvokeDialogAgain() }
            .setNeutralButton(R.string.rateUsDialog_button_later) { _,_ -> dialogInvoker.enableInvokingDialogLater(feedbackState) }
            .setPositiveButton(R.string.rateUsDialog_button_later) { _,_ -> actAccordingToState() }
            .setOnDismissListener {
                if (!feedbackDialogWasOpened) {
                    sendStars()
                }
            }
            .show()

        feedbackState = getFeedbackStateFromRating(ratingLayout.rating)
    }



}