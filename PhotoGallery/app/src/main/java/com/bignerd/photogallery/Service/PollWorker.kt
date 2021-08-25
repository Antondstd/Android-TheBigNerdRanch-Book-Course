package com.bignerd.photogallery.Service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bignerd.photogallery.Model.GalleryItem
import com.bignerd.photogallery.Model.QueryPreferences
import com.bignerd.photogallery.NOTIFICATION_CHANNEL_ID
import com.bignerd.photogallery.PhotoGalleryApplication
import com.bignerd.photogallery.R

private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val galleryItems: List<GalleryItem>? = if (query.isEmpty()) {
            FlickrFetcher().fetchPhotosRequest().execute().body()?.galleryItems
        } else {
            FlickrFetcher().searchPhotosRequest(query).execute().body()?.galleryItems
        }
        if (galleryItems.isNullOrEmpty())
            return Result.success()
        val resultId = galleryItems.first().id
        if (resultId == lastResultId) {

        } else {
            QueryPreferences.setLastResultId(context, resultId)


            val intent = PhotoGalleryApplication.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources
            val notification =
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).setTicker(
                    resources.getString(
                        R.string.new_pictures_title
                    )
                ).setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pendingIntent).setAutoCancel(true).build()

//            NotificationManagerCompat.from(context).notify(0, notification)
//            context.sendBroadcast(Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE)

            showBackgroundNotification(0,notification)
        }
        return Result.success()
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        context.sendOrderedBroadcast(
            Intent(ACTION_SHOW_NOTIFICATION).putExtra(REQUEST_CODE, requestCode).putExtra(
                NOTIFICATION, notification
            ), PERM_PRIVATE
        )

    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION =
            "com.bignerd.photogallery.SHOW_NOTIFICATION"
        const val PERM_PRIVATE = "com.bignerd.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}