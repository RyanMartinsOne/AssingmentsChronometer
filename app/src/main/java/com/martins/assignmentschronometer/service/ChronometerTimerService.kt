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
import com.martins.assignmentschronometer.App
import com.martins.assignmentschronometer.MainActivity
import com.martins.assignmentschronometer.R

class ChronometerTimerService : Service() {

    companion object {
        const val ACTION_START = "com.martins.assignmentschronometer.action.START_TIMER"
        const val ACTION_PAUSE = "com.martins.assignmentschronometer.action.PAUSE_TIMER"
        const val ACTION_STOP = "com.martins.assignmentschronometer.action.STOP_TIMER"
        const val ACTION_TOGGLE = "com.martins.assignmentschronometer.action.TOGGLE_TIMER"
        const val ACTION_RESET = "com.martins.assignmentschronometer.action.RESET_TIMER"
        const val ACTION_FINISH = "com.martins.assignmentschronometer.action.FINISH_TIMER"

        const val EXTRA_BASE_ELAPSED_REALTIME = "extra_base_elapsed_realtime"

        private const val CHANNEL_ID = "chronometer_running_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {

                ACTION_STOP -> {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                ACTION_TOGGLE -> {
                    val sharedViewModel = (application as App).sharedViewModel
                    if (sharedViewModel.isRunning) {
                        sharedViewModel.pause()
                    } else {
                        sharedViewModel.resume()
                    }
                }

                ACTION_RESET -> {
                    (application as App).sharedViewModel.resetTimerKeepRunning()
                }

                ACTION_FINISH -> {
                    val app = application as App
                    val sharedViewModel = app.sharedViewModel
                    val weeklyPartsViewModel = app.weeklyPartsViewModel

                    if (sharedViewModel.activePart != null) {
                        sharedViewModel.savePartTimeAndResetForOverlay { updated ->
                            weeklyPartsViewModel.updatePart(updated)
                        }
                    } else {
                        sharedViewModel.reset()
                    }
                }

                ACTION_PAUSE -> {
                    val base = intent.getLongExtra(
                        EXTRA_BASE_ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime()
                    )
                    startForeground(NOTIFICATION_ID, buildNotification(base, isRunning = false))
                }

                else -> {
                    val base = intent?.getLongExtra(
                        EXTRA_BASE_ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime()
                    ) ?: SystemClock.elapsedRealtime()
                    startForeground(NOTIFICATION_ID, buildNotification(base, isRunning = true))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_NOT_STICKY
    }

    private fun buildNotification(baseElapsedRealtime: Long, isRunning: Boolean): Notification {
        createChannelIfNeeded()

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleAction = buildAction(
            icon = if (isRunning) R.drawable.pause else R.drawable.play,
            title = getString(if (isRunning) R.string.pause else R.string.resume),
            action = ACTION_TOGGLE
        )
        val resetAction = buildAction(
            icon = R.drawable.restart,
            title = getString(R.string.reset),
            action = ACTION_RESET
        )
        val finishAction = buildAction(
            icon = R.drawable.delete,
            title = getString(R.string.notification_action_finish),
            action = ACTION_FINISH
        )

        val elapsedSinceBase = SystemClock.elapsedRealtime() - baseElapsedRealtime
        val whenEpochMillis = System.currentTimeMillis() - elapsedSinceBase
        val elapsedText = formatElapsed(elapsedSinceBase)
        val sharedViewModel = (application as App).sharedViewModel

        val activePartName = sharedViewModel.activePart?.title

        val activeAssignmentName = sharedViewModel.selectedAssignment
            ?.titleRes
            ?.let(::getString)

        val activeItemName = activePartName
            ?.takeIf { it.isNotBlank() }
            ?: activeAssignmentName?.takeIf { it.isNotBlank() }

        val contentTitle = if (isRunning) {
            if (activeItemName != null) {
                getString(R.string.notification_title_running, activeItemName)
            } else {
                getString(R.string.notification_title_running_generic)
            }
        } else {
            getString(R.string.notification_title_paused)
        }

        val contentText = getString(R.string.timer_notification_channel_name)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.hourglass)
            .setContentTitle(contentTitle)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(toggleAction)
            .addAction(resetAction)
            .addAction(finishAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setRequestPromotedOngoing(true)

        if (isRunning) {
            builder
                .setUsesChronometer(true)
                .setContentText(contentText)
                .setChronometerCountDown(false)
                .setWhen(whenEpochMillis)
                .setShowWhen(true)
        } else {
            builder
                .setUsesChronometer(false)
                .setContentText(elapsedText)
                .setShortCriticalText(getString(R.string.notification_paused_chip))
        }

        return builder.build()
    }

    private fun formatElapsed(millis: Long): String {
        val totalSeconds = millis / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    private fun buildAction(icon: Int, title: String, action: String): NotificationCompat.Action {
        val intent = Intent(this, ChronometerTimerService::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(icon, title, pendingIntent)
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