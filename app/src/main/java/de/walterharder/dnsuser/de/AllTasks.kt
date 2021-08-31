package de.walterharder.dnsuser.de

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class AllTasks : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_tasks)

        val btnAddTask = findViewById<Button>(R.id.btnAddNotice)
        btnAddTask.setOnClickListener{
            val i = Intent(this,AddTask::class.java)
            startActivity(i)
        }
        val fileToken = File(this.filesDir, "token")
        val tokenText = FileInputStream(fileToken).bufferedReader().use { it.readText() }
        val fileURL = File(this.filesDir, "url")
        val urlText = FileInputStream(fileURL).bufferedReader().use { it.readText() }
        loadTasks(tokenText,urlText)
    }
    private fun loadTasks(token: String, adresse: String){
        val jsonObject = JSONObject()
        jsonObject.put("key", token)
        jsonObject.put("rq", "/tasks/alltasks")
        val jsonObjectString = jsonObject.toString()
        val urll = URL(adresse)
        val httpURLConnection = urll.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "GET"
        httpURLConnection.setRequestProperty("Content-Type", "application/json")
        httpURLConnection.setRequestProperty("Accept", "application/json")
        httpURLConnection.doOutput = true
        httpURLConnection.doInput = true
        Thread{
            try {
                val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
                outputStreamWriter.write(jsonObjectString)
                outputStreamWriter.flush()
                var responseCode = httpURLConnection.inputStream.bufferedReader().readText()
                responseCode = responseCode.replace("[", "")
                responseCode = responseCode.replace("]", "")
                responseCode = responseCode.replace("\"", "")
                val result = responseCode.split(",").map { it.trim() }
                runOnUiThread {
                    val proAdp = android.widget.ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        result
                    )
                    val listl = findViewById<ListView>(R.id.listTasks)
                    listl.adapter = proAdp
                    listl.setOnItemClickListener { _, _, position, _ ->
                        val element = listl.getItemAtPosition(position) as String
                        val dialog: AlertDialog
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Löschen ?")
                        builder.setMessage(element)
                        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> deleteTask(element)
                                DialogInterface.BUTTON_NEGATIVE -> Log.i("0", "0")
                            }
                        }
                        builder.setPositiveButton("Ja", dialogClickListener)
                        builder.setNegativeButton("Nein", dialogClickListener)
                        dialog = builder.create()
                        dialog.show()
                    }
                }
            }
            catch (e: Exception){
                //Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
            }
        }.start()
    }
    private fun deleteTask(element: String){
        val fileToken = File(this.filesDir, "token")
        val tokenText = FileInputStream(fileToken).bufferedReader().use { it.readText() }
        val fileURL = File(this.filesDir, "url")
        val urlText = FileInputStream(fileURL).bufferedReader().use { it.readText() }
        Thread {
            val jsonObject = JSONObject()
            jsonObject.put("key", tokenText)
            val jsonObjectString = jsonObject.toString()
            val url = URL(urlText + "/delete/" + element)
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.setRequestProperty("Content-Type", "application/json")
            httpURLConnection.setRequestProperty("Accept", "application/json")
            httpURLConnection.doOutput = true
            httpURLConnection.doInput = true
            val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
            outputStreamWriter.write(jsonObjectString)
            outputStreamWriter.flush()
            try {
                val responseCode = httpURLConnection.inputStream.bufferedReader().readText()
                if (responseCode == "1"){
                    finish()
                }
            } catch (e: Exception) {
                Log.i("0", "0")
            }
        }.start()
    }
}