package com.coco.celestia

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun sendEmail(to: String, subject: String, body: String) {
    CoroutineScope(Dispatchers.IO).launch {
        val username = ""
        val password = ""

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                this.subject = subject
                setText(body)
            }

            Transport.send(message)
            withContext(Dispatchers.Main) {
                println("Email sent successfully!")
            }
        } catch (e: MessagingException) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                println("Failed to send email: ${e.message}")
            }
        }
    }
}

