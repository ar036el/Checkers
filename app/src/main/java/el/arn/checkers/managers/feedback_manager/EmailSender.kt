package el.arn.checkers.managers.feedback_manager

import android.os.AsyncTask
import android.util.Log
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender(
    private val username: String,
    private val password: String,
    private val host: String,
    private val port: String
) {


    fun sendMailAsync(from: String, to: String, subject: String, body: String) {
        SendMailAsyncTask { sendMail(from, to, subject, body) }.execute()
    }

    private fun sendMail(from: String, to: String, subject: String, body: String) {
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = host
        props["mail.smtp.port"] = port

        val session = Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

        try {
            val message: Message = MimeMessage(session)
            message.setFrom(InternetAddress(from))
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
            )
            message.subject = subject
            message.setText(body)
            Transport.send(message)

        } catch (e: MessagingException) {
            Log.e("EmailSender", e.toString())
        }
    }

    private class SendMailAsyncTask(private val doInBackground: () -> Unit) : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            doInBackground.invoke()
        }

    }

}
