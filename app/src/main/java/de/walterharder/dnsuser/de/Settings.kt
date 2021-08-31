package de.walterharder.dnsuser.de

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.util.PatternsCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class Settings : AppCompatActivity() {
    private var rep = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val etAdresse = findViewById<EditText>(R.id.edAdresse)
        val etToken = findViewById<EditText>(R.id.edToken)
        val btnDeleteSettings = findViewById<Button>(R.id.btnDelete)
        val btnSaveSettings = findViewById<Button>(R.id.btnSave)
        val swAlarm = findViewById<SwitchCompat>(R.id.alarmDay)
        swAlarm.isChecked = true

        swAlarm.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked)
                rep = 0
        }

        btnDeleteSettings.setOnClickListener {
            etAdresse.setText("")
            etToken.setText("")
        }
        btnSaveSettings.setOnClickListener {
            if (etAdresse.text.isNotEmpty() && etToken.text.isNotEmpty()) {
                if (PatternsCompat.WEB_URL.matcher(etAdresse.text.toString()).matches()) {
                    testSettings(etAdresse.text.toString(), etToken.text.toString(),rep)
                } else {
                    Toast.makeText(this, getString(R.string.noValidUrl), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.feldsEmpty), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun testSettings(adresse: String, token: String,rep: Int) {
        val jsonObject = JSONObject()
        jsonObject.put(
            "key",
            token
        )
        jsonObject.put("rq", "/server/new")
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
                if (responseCode == "1") {
                    runOnUiThread {
                        Toast.makeText(this,getString(R.string.allOk),Toast.LENGTH_LONG).show()
                        saveFile(address,token,rep)
                        Toast.makeText(this,getString(R.string.saveOk),Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
                else {
                    runOnUiThread {
                        Toast.makeText(this,getString(R.string.wrongToken),Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this,getString(R.string.connectionError),Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    private fun saveFile(adresse: URL, token: String, alarmDay: Int){
        val fileFirst = File(this.filesDir,"firstStart")
        val fileURL = File(this.filesDir,"url")
        val fileToken = File(this.filesDir,"token")
        val fileAlarm = File(this.filesDir,"alarm")
        FileOutputStream(fileAlarm).use {
            it.write(alarmDay.toString().toByteArray())
        }
        FileOutputStream(fileFirst).use {
            it.write("0".toByteArray())
        }
        FileOutputStream(fileURL).use {
            it.write(adresse.toString().toByteArray())
        }
        FileOutputStream(fileToken).use {
            it.write(token.toByteArray())
        }
    }
}