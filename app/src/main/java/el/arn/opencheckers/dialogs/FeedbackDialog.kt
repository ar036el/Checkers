package el.arn.opencheckers.dialogs

import android.app.Activity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import el.arn.opencheckers.R
import el.arn.opencheckers.managers.feedback_manager.FeedbackManager

class FeedbackDialog(
    private val activity: Activity,
    private val feedbackManager: FeedbackManager,
    private val stars: Int?
) : Dialog {

    override val isShowing: Boolean
        get() = dialogBeingShown.isShowing
    override fun dismiss() {
        if (dialogBeingShown.isShowing) {
            dialogBeingShown.dismiss()
        }
    }

    private var dialogBeingShown: android.app.Dialog

    private fun sendFeedback() {
        val feedback = layout.findViewById<EditText>(R.id.dialogSendFeedback_editText).text.toString()
        feedbackManager.sendFeedback(stars, feedback)

    }

    private fun showApprovedDialog() {
        dialogBeingShown =
            AlertDialog.Builder(activity)
                .setTitle(R.string.sendFeedbackDialog_approved)
                .setPositiveButton(R.string.general_dialog_ok, null)
                .show()
    }

    private val layout = activity.layoutInflater.inflate(R.layout.dialog_send_feedback, null) as LinearLayout

    init {
        dialogBeingShown =
            AlertDialog.Builder(activity)
                .setTitle(R.string.sendFeedbackDialog_title)
                .setView(layout)
                .setPositiveButton(R.string.sendFeedbackDialog_submitButton) { _,_ ->
                    showApprovedDialog()
                    sendFeedback()
                }
                .show()
    }

}