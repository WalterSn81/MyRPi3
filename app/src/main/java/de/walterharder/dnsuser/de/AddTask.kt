package de.walterharder.dnsuser.de

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AddTask : AppCompatActivity() {
    private var jahr = ""
    private var monat = ""
    private var tag = ""
    private var rpl = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        val textn = findViewById<EditText>(R.id.edTextNotice)
        val calView = findViewById<CalendarView>(R.id.calView)
        calView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            jahr = year.toString()
            monat = (month + 1).toString()
            tag = dayOfMonth.toString()
        }
        textn.setOnClickListener {
            textn.setText("")
        }
        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            val ntext = textn.text
            val cbReplay = findViewById<SwitchCompat>(R.id.cbReplay)
            if (ntext.isNotEmpty()) {
                if (tag == "") {
                    Toast.makeText(this, getString(R.string.dateIsEmpty), Toast.LENGTH_LONG).show()
                } else {
                    if (cbReplay.isChecked) {
                        rpl = 1
                    }
                    sendData(tag, monat, jahr, ntext.toString(), rpl)
                }
            } else {
                Toast.makeText(this, getString(R.string.textIsEmpty), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendData(tag: String, monat: String, jahr: String, textN: String, rpl: Int) {
        val fileToken = File(this.filesDir, "token")
        val tokenText = FileInputStream(fileToken).bufferedReader().use { it.readText() }
        val fileURL = File(this.filesDir, "url")
        val urlText = FileInputStream(fileURL).bufferedReader().use { it.readText() }
        Thread {
            val jsonObject = JSONObject()
            jsonObject.put("key", tokenText)
            val jsonObjectString = jsonObject.toString()
            val url =
                URL(urlText + "/add/" + tag + "/" + monat + "/" + jahr + "/" + rpl + "/" + textN)
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.setRequestProperty("Content-Type", "application/json")
            httpURLConnection.setRequestProperty("Accept", "application/json")
            httpURLConnection.doOutput = true
            httpURLConnection.doInput = true
            val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
            outputStreamWriter.write(jsonObjectString)
            outputStreamWriter.flush()
            try {
                val responseCode = httpURLConnection.inputStream.bufferedReader().readText()
                if (responseCode == "1") {
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.connectionError), Toast.LENGTH_LONG).show()
                finish()
            }
        }.start()
    }
}