package de.walterharder.dnsuser.de

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class ServiceAlarm : JobService() {
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    override fun onStartJob(parameters: JobParameters?): Boolean {
        alarmMgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(this, BR::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        alarmMgr?.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
            AlarmManager.INTERVAL_HALF_DAY,
            alarmIntent
        )
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}