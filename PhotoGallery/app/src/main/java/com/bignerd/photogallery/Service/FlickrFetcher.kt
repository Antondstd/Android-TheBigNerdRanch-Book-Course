package com.bignerd.photogallery.Service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerd.photogallery.API.FlickrApi
import com.bignerd.photogallery.API.FlickrResponse
import com.bignerd.photogallery.API.PhotoInterceptor
import com.bignerd.photogallery.Model.GalleryItem
import com.bignerd.photogallery.Model.PhotoResponse
import com.bignerd.photogallery.PhotoDeserializer
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetcher"

class FlickrFetcher {
    private val flickrApi: FlickrApi

    val client = OkHttpClient.Builder()
        .addInterceptor(PhotoInterceptor())
        .build()

    private var flickrCall: Call<PhotoResponse>? = null

    init {
        flickrApi = Retrofit.Builder().baseUrl("https://flickr.com/").addConverterFactory(
            GsonConverterFactory.create(GsonBuilder().registerTypeAdapter(object :
                TypeToken<PhotoResponse>() {}.type, PhotoDeserializer()).create())
        ).client(client).build().create(FlickrApi::class.java)
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val bitmap = flickrApi.downloadPhoto(url).execute().body()?.byteStream()
            ?.use(BitmapFactory::decodeStream)
        return bitmap
    }

    fun dailyPhotos(page: Int):LiveData<MutableList<GalleryItem>>{
        return fetchPhotoMetadata(fetchPhotosRequest(page))
    }

    fun fetchPhotosRequest(page: Int = 1): Call<PhotoResponse> {
        return flickrApi.getDailyPhotos(page)
    }

    fun searchPhotosRequest(query: String,page: Int = 1): Call<PhotoResponse> {
        return flickrApi.searchPhotos(query,page)
    }

    fun searchPhotos(query: String,page: Int): LiveData<MutableList<GalleryItem>> {
        return fetchPhotoMetadata(searchPhotosRequest(query,page))
    }

    private fun fetchPhotoMetadata(flickrRequest: Call<PhotoResponse>): LiveData<MutableList<GalleryItem>> {
        val contentLiveData: MutableLiveData<MutableList<GalleryItem>?> =
            MutableLiveData<MutableList<GalleryItem>?>()

        Log.e(TAG, "Started fetching Search photos")
        flickrRequest.enqueue(object : Callback<PhotoResponse> {
            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                if (call.isCanceled)
                    Log.e(TAG, "The daily photo request canceled")
                else
                    Log.e(TAG, "Failed request to flickr homepage")
            }

            override fun onResponse(
                call: Call<PhotoResponse>,
                response: Response<PhotoResponse>
            ) {
                Log.e(TAG, "Successfully fetched Searched photos")
                var galleryItems = response.body()?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot { it.url.isBlank() }

                if (contentLiveData!!.value.isNullOrEmpty()) {
                    contentLiveData.value = mutableListOf()
                }

                contentLiveData.value!!.addAll(galleryItems)

            }
        }
        )
        return contentLiveData as LiveData<MutableList<GalleryItem>>
    }

    fun fetchContent(): LiveData<String> {
        val contentLiveData: MutableLiveData<String> = MutableLiveData()
        flickrApi.getContents().enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "Failed request to flickr homepage")
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "Response received ${response.body()}")
                contentLiveData.value = response.body()
            }
        }
        )
        return contentLiveData
    }

    fun fetchDailyPhotos(
        page: Int,
        liveData: MutableLiveData<MutableList<GalleryItem>>?
    ): LiveData<MutableList<GalleryItem>> {
        val contentLiveData: MutableLiveData<MutableList<GalleryItem>>?
        if (liveData == null)
            contentLiveData = MutableLiveData<MutableList<GalleryItem>>()
        else
            contentLiveData = liveData
        flickrCall = flickrApi.getDailyPhotos(page)
        Log.e(TAG, "Started fetching daily photos")
        flickrCall!!.enqueue(object : Callback<PhotoResponse> {
            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                if (call.isCanceled)
                    Log.e(TAG, "The daily photo request canceled")
                else
                    Log.e(TAG, "Failed request to flickr homepage")
            }

            override fun onResponse(
                call: Call<PhotoResponse>,
                response: Response<PhotoResponse>
            ) {
                Log.e(TAG, "Successfully fetched daily photos")
                var galleryItems = response.body()?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot { it.url.isBlank() }

                if (contentLiveData!!.value.isNullOrEmpty()) {
                    contentLiveData.value = mutableListOf()
                }
                contentLiveData.value!!.addAll(galleryItems)


            }
        }
        )
        return contentLiveData as LiveData<MutableList<GalleryItem>>
    }

    fun cancelCall() = flickrCall?.cancel()
}