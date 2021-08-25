package com.bignerd.photogallery.Service

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val TAG = "NotificationReceiver"
class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (resultCode != Activity.RESULT_OK)
            return
        val requestCode = intent.getIntExtra(PollWorker.REQUEST_CODE,0)
        val notification:Notification = intent.getParcelableExtra(PollWorker.NOTIFICATION)!!

        NotificationManagerCompat.from(context).notify(requestCode,notification)
    }
}