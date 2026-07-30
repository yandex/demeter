package com.yandex.demeter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat.Builder
import androidx.core.content.ContextCompat
import com.yandex.demeter.api.UiDemeterPlugin
import com.yandex.demeter.internal.core.UiConfig
import com.yandex.demeter.profiler.ui.R
import com.yandex.demeter.internal.ui.MetricsActivity

class UiDemeterInitializer(
    private val context: Context,
    private val uiPlugins: List<UiDemeterPlugin>,
    private val showNotificationOnInit: Boolean = true,
) : Demeter.Initializer {
    override fun init(): Demeter.Core {
        val plugins = uiPlugins.map { it.plugin }

        return DemeterCoreInitializer.init(plugins).also {
            UiConfig.plugins = uiPlugins

            if (showNotificationOnInit) {
                showMetricsNotification()
            }
        }
    }

    fun showMetricsNotification() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (uiPlugins.isEmpty() || !NotificationPermission.canPost(Build.VERSION.SDK_INT, permissionGranted)) {
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = context.getString(R.string.adm_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            var channel = notificationManager.getNotificationChannel(notificationChannel)
            if (channel == null) {
                channel = NotificationChannel(notificationChannel, notificationChannel, importance)
                channel.description = notificationChannel
                channel.enableVibration(false)
                notificationManager.createNotificationChannel(channel)
            }
        }

        val builder = Builder(context, notificationChannel)
            .setSmallIcon(R.drawable.ic_timeline_24dp)
            .setContentTitle(notificationChannel)
            .setContentText("Click to see current metrics")
            .setAutoCancel(false)
        val resultIntent = Intent(context, MetricsActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(resultPendingIntent)
        notificationManager.notify(notificationChannel.hashCode(), builder.build())
    }
}
