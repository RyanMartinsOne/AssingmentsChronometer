package com.martins.assignmentschronometer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.martins.assignmentschronometer.MainActivity
import com.martins.assignmentschronometer.R

/**
 * Keeps the app process alive (foreground priority) while a chronometer is
 * running, so the OS does not kill it when the screen is locked or the app
 * is backgrounded. Shows an ongoing notification using Android's native
 * chronometer view, so the displayed time is driven by the system itself
 * and does not depend on this service doing any ticking of its own.
 */
class ChronometerTimerService : Service() {

    companion object {
        const val ACTION_START = "com.martins.assignmentschronometer.action.START_TIMER"
        const val ACTION_STOP = "com.martins.assignmentschronometer.action.STOP_TIMER"

        /** SystemClock.elapsedRealtime() timestamp the chronometer should count up from. */
        const val EXTRA_BASE_ELAPSED_REALTIME = "extra_base_elapsed_realtime"

        private const val CHANNEL_ID = "chronometer_running_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                val baseElapsedRealtime = intent?.getLongExtra(
                    EXTRA_BASE_ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime()
                ) ?: SystemClock.elapsedRealtime()
                startForeground(NOTIFICATION_ID, buildNotification(baseElapsedRealtime))
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(baseElapsedRealtime: Long): Notification {
        createChannelIfNeeded()

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setUsesChronometer relies on `when` as a wall-clock reference point;
        // we translate the elapsedRealtime base into wall-clock time so the
        // system-drawn chronometer keeps counting on its own, with no timer
        // logic living inside this service.
        val alreadyElapsedMillis = SystemClock.elapsedRealtime() - baseElapsedRealtime
        val whenMillis = System.currentTimeMillis() - alreadyElapsedMillis

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.record_filled)
            .setContentTitle(getString(R.string.timer_notification_title))
            .setContentText(getString(R.string.timer_notification_message))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .setWhen(whenMillis)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // Asks the system to promote this notification as a Live Update
            // (Android 16+). On devices where that's supported — e.g. Samsung
            // One UI 8+ — this is what makes it eligible to also show up in
            // the Now Bar, with no Samsung-specific code needed. This call is
            // a safe no-op on older OS versions.
            .setRequestPromotedOngoing(true)
            .build()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.timer_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
                manager.createNotificationChannel(channel)
            }
        }
    }
}