package com.bignerd.photogallery.API

import com.bignerd.photogallery.Model.PhotoResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {
    @GET("/")
    fun getContents(): Call<String>

    @GET("services/rest/?method=flickr.interestingness.getList")
    fun getDailyPhotos(@Query(value = "page") page:Int = 1):Call<PhotoResponse>

    @GET
    fun downloadPhoto(@Url url: String):Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String,@Query(value = "page") page:Int = 1): Call<PhotoResponse>
}