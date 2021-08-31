package de.walterharder.dnsuser.de

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo =
            JobInfo.Builder(11, ComponentName(this@MainActivity, ServiceAlarm::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build()
        jobScheduler.schedule(jobInfo)

        val receiver = ComponentName(this, ServiceAlarm::class.java)
        this.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val btnServer = findViewById<Button>(R.id.btnServer)
        btnServer.setOnClickListener{
            val i = Intent(this, Server::class.java)
            startActivity(i)
        }
        val btnLed = findViewById<Button>(R.id.btnLed)
        btnLed.setOnClickListener{
            val i = Intent(this, Led::class.java)
            startActivity(i)
        }

        val btnSettings = findViewById<Button>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            val i = Intent(this, Settings::class.java)
            startActivity(i)
        }
        val btnNotice = findViewById<Button>(R.id.btnNotice)
        btnNotice.setOnClickListener {
            val i = Intent(this, AllTasks::class.java)
            startActivity(i)
        }
        // Check ob die Dateien Exestieren wenn nicht öffne die Einstellungen
        try {
            val fileFirstStart = File(this.filesDir, "firstStart")
            val firstStartText =
                FileInputStream(fileFirstStart).bufferedReader().use { it.readText() }
            if (firstStartText != "0") {
                val i = Intent(this, Settings::class.java)
                startActivity(i)
            }
        } catch (e: IOException) {
            val i = Intent(this, Settings::class.java)
            startActivity(i)
        }
        // ENDE
        // Lese Datan aus der Datai
        val fileFirst = File(this.filesDir, "firstStart")
        val firstText = FileInputStream(fileFirst).bufferedReader().use { it.readText() }
        val fileToken = File(this.filesDir, "token")
        val tokenText = FileInputStream(fileToken).bufferedReader().use { it.readText() }
        val fileURL = File(this.filesDir, "url")
        val urlText = FileInputStream(fileURL).bufferedReader().use { it.readText() }
        val fileAlarm = File(this.filesDir, "alarm")
        val alarmText = FileInputStream(fileAlarm).bufferedReader().use { it.readText() }
        if (alarmText == "1") {
            val i = Intent(this, ServiceAlarm::class.java)
            startService(i)
            //SET ALARM AS SERVICE
        }
        if (firstText == "0") {
            getData(tokenText, urlText)
            getFuture(tokenText, urlText)
            getNext(tokenText, urlText)
        }
        // ENDE
    }

    private fun getNext(token: String, adresse: String) {
        val jsonObject = JSONObject()
        jsonObject.put("key", token)
        jsonObject.put("rq", "/tasks/nextnext")
        val jsonString = jsonObject.toString()
        val address = URL(adresse)
        val httpURLConnection = address.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.setRequestProperty("Content-Type", "application/json")
        Thread {
            try {
                val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
                outputStreamWriter.write(jsonString)
                outputStreamWriter.flush()
                val responseCode = httpURLConnection.inputStream.bufferedReader().readText()
                runOnUiThread {
                    val tvNextView = findViewById<TextView>(R.id.tvNextView)
                    if (responseCode == "0") {
                        tvNextView.text = getString(R.string.tvNextEmpty)
                    } else {
                        tvNextView.text = responseCode
                    }
                }
            } catch (e: Exception) {
                //Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun getFuture(token: String, adresse: String) {
        val jsonObject = JSONObject()
        jsonObject.put("key", token)
        jsonObject.put("rq", "/tasks/next")
        val jsonString = jsonObject.toString()
        val address = URL(adresse)
        val httpURLConnection = address.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.setRequestProperty("Content-Type", "application/json")
        Thread {
            try {
                val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
                outputStreamWriter.write(jsonString)
                outputStreamWriter.flush()
                val responseCode = httpURLConnection.inputStream.bufferedReader().readText()
                runOnUiThread {
                    val tvFutureView = findViewById<TextView>(R.id.tvFutureView)
                    tvFutureView.text = responseCode
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val tvFutureView = findViewById<TextView>(R.id.tvFutureView)
                    tvFutureView.text = getString(R.string.connectionError)
                }
            }
        }.start()
    }

    private fun getData(token: String, adresse: String) {
        val jsonObject = JSONObject()
        jsonObject.put("key", token)
        jsonObject.put("rq", "/server/status")
        val jsonString = jsonObject.toString()
        val address = URL(adresse)
        val httpURLConnection = address.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.setRequestProperty("Content-Type", "application/json")
        Thread {
            try {
                val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
                outputStreamWriter.write(jsonString)
                outputStreamWriter.flush()
                val responseCode = httpURLConnection.inputStream.bufferedReader().readText()
                val obj = JSONObject(responseCode)
                val cpuTemp = obj.getString("cpuTemp")
                val freeHdd = obj.getString("free")
                val freeRam = obj.getString("freeRam")
                val rTime = obj.getString("runningTime")
                var apache2 = obj.getString("apache")
                if (apache2 == "1") {
                    apache2 = "OK"
                }
                var sql = obj.getString("slq")
                if (sql == "1") {
                    sql = "OK"
                }
                var ssh = obj.getString("ssh")
                if (ssh == "1") {
                    ssh = "OK"
                }
                runOnUiThread {
                    val tvCpuTempView = findViewById<TextView>(R.id.tvCpuTempView)
                    tvCpuTempView.text = cpuTemp
                    val tvSdFreeView = findViewById<TextView>(R.id.tvSdFreeView)
                    tvSdFreeView.text = freeHdd
                    val tvRamFreeView = findViewById<TextView>(R.id.tvRamFreeView)
                    tvRamFreeView.text = freeRam
                    val tvRunTimeView = findViewById<TextView>(R.id.tvRunTimeView)
                    tvRunTimeView.text = rTime
                    val tvApacheView = findViewById<TextView>(R.id.tvApacheView)
                    tvApacheView.text = apache2
                    val tvSshView = findViewById<TextView>(R.id.tvSshView)
                    tvSshView.text = ssh
                    val tvSqlView = findViewById<TextView>(R.id.tvSqlView)
                    tvSqlView.text = sql
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val tvCpuTempView = findViewById<TextView>(R.id.tvCpuTempView)
                    tvCpuTempView.text = getString(R.string.connectionError)
                    val tvSdFreeView = findViewById<TextView>(R.id.tvSdFreeView)
                    tvSdFreeView.text = getString(R.string.connectionError)
                    val tvRamFreeView = findViewById<TextView>(R.id.tvRamFreeView)
                    tvRamFreeView.text = getString(R.string.connectionError)
                    val tvRunTimeView = findViewById<TextView>(R.id.tvRunTimeView)
                    tvRunTimeView.text = getString(R.string.connectionError)
                    val tvApacheView = findViewById<TextView>(R.id.tvApacheView)
                    tvApacheView.text = getString(R.string.connectionError)
                    val tvSshView = findViewById<TextView>(R.id.tvSshView)
                    tvSshView.text = getString(R.string.connectionError)
                    val tvSqlView = findViewById<TextView>(R.id.tvSqlView)
                    tvSqlView.text = getString(R.string.connectionError)
                }
            }
        }.start()
    }
}