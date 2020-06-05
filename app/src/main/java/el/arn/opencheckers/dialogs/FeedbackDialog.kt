package el.arn.opencheckers.dialogs

import android.app.Activity
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import el.arn.opencheckers.App
import el.arn.opencheckers.R

class FeedbackDialog(private val activity: Activity, private val stars: Int?) {

    private fun sendFeedback() {
        val feedback = layout.findViewById<EditText>(R.id.dialogSendFeedback_editText).text.toString()
        App.instance.feedbackManager.sendFeedback(stars, feedback)

    }

    private fun sendOnlyStars() {
        App.instance.feedbackManager.sendFeedback(stars, null)
    }

    private fun showApprovedDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.sendFeedbackDialog_title)
            .setTitle(R.string.sendFeedbackDialog_approved)
            .setPositiveButton(R.string.dialog_ok, null)
            .show()
    }

    private val layout = activity.layoutInflater.inflate(R.layout.dialog_send_feedback, null) as ConstraintLayout
    private var feedbackWasSubmitted = false

    init {
        AlertDialog.Builder(activity)
            .setTitle(R.string.sendFeedbackDialog_title)
            .setView(layout)
            .setPositiveButton(R.string.sendFeedbackDialog_submitButton) { _,_ ->
                showApprovedDialog()
                sendFeedback()
                feedbackWasSubmitted = true
            }
            .setOnDismissListener {
                if (stars != null && !feedbackWasSubmitted){
                    sendOnlyStars()
                }
            }
    }

}