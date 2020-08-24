package el.arn.checkers.managers.feedback_manager

import el.arn.checkers.R
import el.arn.checkers.helpers.android.stringFromRes
import java.lang.Exception


interface FeedbackManager {
    fun sendCrashReport(crashReportMessage: String)
    fun sendFeedback(stars: Int?, message: String?)
}

class FeedbackManagerImpl : FeedbackManager {
    private val emailSender =
        EmailSender(
            stringFromRes(R.string.internal_feedbackSenderEmail),
            "lalalala000",
            "smtp.gmail.com",
            "587"
        )
    private val from = stringFromRes(R.string.internal_feedbackSenderEmail)
    private val to = stringFromRes(R.string.internal_feedbackRecipientEmail)


    override fun sendCrashReport(crashReportMessage: String) {
        try { //because it's a crash report, no unhandled exception is allowed
            emailSender.sendMailAsync(from, to, "crashReport", crashReportMessage)
        } catch (e: Throwable) { }
    }

    override fun sendFeedback(stars: Int?, message: String?) {
        val body = "stars:" + (stars?.toString() ?: "null") + "\nmessage:" + (message ?: "null")

        emailSender.sendMailAsync(from, to,"user feedback", body)
    }


}