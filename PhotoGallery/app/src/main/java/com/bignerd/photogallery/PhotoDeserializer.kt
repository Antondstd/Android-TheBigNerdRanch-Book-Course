package com.bignerd.photogallery

import android.util.Log
import com.bignerd.photogallery.Model.GalleryItem
import com.bignerd.photogallery.Model.PhotoResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class PhotoDeserializer:JsonDeserializer<PhotoResponse> {
    private val TAG = this::class.simpleName

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        Log.i(TAG,"It's deserializing")
        val photoResponse = PhotoResponse()
        photoResponse.galleryItems = emptyList()
        val jsonList = json?.asJsonObject?.get("photos")?.asJsonObject?.get("photo")?.asJsonArray
        jsonList.let { photoResponse.galleryItems = Gson().fromJson(it,object : TypeToken<List<GalleryItem>>() {}.type) }
        return photoResponse
    }

}