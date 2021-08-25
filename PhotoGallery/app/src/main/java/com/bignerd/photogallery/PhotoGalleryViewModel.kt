package com.bignerd.photogallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.bignerd.photogallery.Model.GalleryItem
import com.bignerd.photogallery.Model.QueryPreferences
import com.bignerd.photogallery.Service.FlickrFetcher


private val TAG = "PhotoGalleryViewModel"

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    var galleryItemsLiveData: LiveData<MutableList<GalleryItem>>
    val flickrFetcher: FlickrFetcher = FlickrFetcher()
    var currentMaxPage = MutableLiveData<Int>()
    var test = 1
    val searchTerm: String
        get() = combinedValues.query.value ?: ""
    val combinedValues = SearchHolder()

    init {
//        galleryItemsLiveData = flickrFetcher.fetchDailyPhotos(currentMaxPage, null)
//        galleryItemsLiveData = flickrFetcher.searchPhotos("naked")
        currentMaxPage.value = 1
        combinedValues.query.value = QueryPreferences.getStoredQuery(app)
        Log.d(
            TAG,
            "SAVED QUERY IS ${combinedValues.query.value} and ${QueryPreferences.getStoredQuery(app)}"
        )
        galleryItemsLiveData = Transformations.switchMap(combinedValues) { pair ->
            val searchText = pair.second
            val curPage = pair.first
            val List = mutableListOf<GalleryItem>()
            if (searchText?.isBlank() == true) {
                flickrFetcher.fetchDailyPhotos(curPage!!, null)
            } else
                flickrFetcher.searchPhotos(searchText!!,curPage!!)
        }
    }

    fun fetchNextPage(id: Int) {
        Log.d(TAG, "current id $id and nextPage would be ${combinedValues.page.value!! + 1}")
        if (true) {
            Log.d(TAG, "Opa, next page Loading")
            combinedValues.nextPage()
        }
        //flickrFetcher.fetchDailyPhotos(currentMaxPage, galleryItemsLiveData)
    }

    fun fetchPhotos(query: String = "") {
//        mutableSearchTerm.value = query
        combinedValues.query.value = query
        QueryPreferences.setStoredQuery(app, query)
    }

    override fun onCleared() {
        super.onCleared()
        flickrFetcher.cancelCall()
    }

    class SearchHolder() : MediatorLiveData<Pair<Int?, String?>>() {
        var page: MutableLiveData<Int> = MutableLiveData<Int>()
        var query: MutableLiveData<String> = MutableLiveData<String>()

        init {
            page.value = 1
            query.value = ""
            setValue(Pair(page.value, query.value))
            addSource(page) { setValue(Pair(page.value, query.value)) }
            addSource(query) { setValue(Pair(page.value, query.value)) }
        }

        fun nextPage() {
            page.value = page.value?.plus(1)
        }
    }
}