package com.bignerd.photogallery.Service

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0
private const val MESSAGE_CACHE = 1

class ThumbnailDownloader<T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloader: (T, Bitmap) -> Unit,
) : HandlerThread(TAG) {

    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetcher = FlickrFetcher()
    var viewLifecycleOwner: LifecycleOwner? = null
    private val lruCache: LruCache<String, Bitmap> = LruCache(18 + 10 + 10)

    val executors = Executors.newFixedThreadPool(20)

    fun checkCache(url: String) = lruCache.get(url)

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    val fragmentLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.i(TAG, "Starting background")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            removeObserver()
            quit()
        }
    }

    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue() {
            Log.i(TAG, "Start Clearing")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    fun removeObserver() {
        viewLifecycleOwner?.lifecycle?.removeObserver(viewLifecycleObserver)
        Log.d(TAG, "Removing obserser $viewLifecycleObserver")
    }

    fun queueThumbnail(target: T, url: String) {
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    fun queueCacheThumbnail(url: String) {
        requestHandler.obtainMessage(MESSAGE_CACHE, url).sendToTarget()
    }

    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD)
                    handleRequest(msg.obj as T)
                if (msg.what == MESSAGE_CACHE)
                    handleCacheRequest(msg.obj as String)
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetcher.fetchPhoto(url) ?: return
        //Log.d(TAG, "HANDE REQUEST ULR ${url}")
        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit)
                return@Runnable
            requestMap.remove(target)
            onThumbnailDownloader(target, bitmap)
            lruCache.put(url, bitmap)
        })
    }

    private fun handleCacheRequest(url: String) {
        executors.execute(
            Runnable {
                //Log.d(TAG, "Executing ${url}")
                val bitmap = flickrFetcher.fetchPhoto(url) ?: return@Runnable
                lruCache.put(url, bitmap)
            }
        )
    }

}