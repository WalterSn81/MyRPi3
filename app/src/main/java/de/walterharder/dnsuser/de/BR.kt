package de.walterharder.dnsuser.de

import android.app.*
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class BR : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Raspberry Pi"
            val descriptionText = "Nachrichten von Raspberry"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("4816", name, importance).apply {
                description = descriptionText
                lockscreenVisibility = VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val jobScheduler =
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val jobInfo = JobInfo.Builder(11, ComponentName(context, ServiceAlarm::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build()
                jobScheduler.schedule(jobInfo)
            }
        }
        val fileToken = File(context.filesDir, "token")
        val fileUrl = File(context.filesDir, "url")
        val tokenText = FileInputStream(fileToken).bufferedReader().use { it.readText() }
        val urlText = FileInputStream(fileUrl).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject()
        jsonObject.put(
            "key",
            tokenText
        )
        jsonObject.put("rq", "/tasks/nextnext")
        val jsonString = jsonObject.toString()
        val address = URL(urlText)
        val httpURLConnection = address.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.setRequestProperty("Content-Type", "application/json")
        Thread {
            try {
                val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
                outputStreamWriter.write(jsonString)
                outputStreamWriter.flush()
                val responseCode = httpURLConnection.inputStream.bufferedReader().readText()
                if (responseCode == "0") {
                    val builder = NotificationCompat.Builder(context, "4816")
                        .setSmallIcon(R.drawable.ic_action_name)
                        .setContentTitle("Nachricht von Raspberry Pi")
                        .setContentText("Alles OK.")
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                    with(NotificationManagerCompat.from(context)) {
                        notify(4816, builder.build())
                    }
                } else {
                    val builder = NotificationCompat.Builder(context, "4816")
                        .setSmallIcon(R.drawable.ic_action_name)
                        .setContentTitle("Nachricht von Raspberry Pi")
                        .setContentText(responseCode)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                    with(NotificationManagerCompat.from(context)) {
                        notify(4816, builder.build())
                    }
                }
            } catch (e: Exception) {
                val builder = NotificationCompat.Builder(context, "4816")
                    .setSmallIcon(R.drawable.ic_action_name)
                    .setContentTitle("Nachricht von Raspberry Pi")
                    .setContentText("Fehler bei der Verbindund zum Raspberry!")
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                with(NotificationManagerCompat.from(context)) {
                    notify(4816, builder.build())
                }
            }
        }.start()
    }
}