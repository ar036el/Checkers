package el.arn.opencheckers.tools.feedback_manager


class FeedbackManager {

    private val mailSender = MailSender(
        "opencheckersfeedbacksender@gmail.com",
        "lalalala000"
    )
    private val sender = "openCheckersFeedbackSender"
    private val recipient = "opencheckersfeedback@gmail.com"


    fun sendCrashReport(crashReportMessage: String) {
        mailSender.sendMail("crashReport", crashReportMessage, sender, recipient)
    }

    fun sendFeedback(stars: Int?, message: String?) {
        val body = "stars:" + (stars?.toString() ?: "null") + " " + (message ?: "")

        mailSender.sendMail("feedback", body, sender, recipient)
    }


}