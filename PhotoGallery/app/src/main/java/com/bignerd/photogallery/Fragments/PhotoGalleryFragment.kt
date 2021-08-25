package com.bignerd.photogallery.Fragments

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bignerd.photogallery.Model.GalleryItem
import com.bignerd.photogallery.Model.QueryPreferences
import com.bignerd.photogallery.PhotoGalleryViewModel
import com.bignerd.photogallery.Service.PollWorker
import com.bignerd.photogallery.Service.ThumbnailDownloader
import com.bignerd.photogallery.Service.VisibleFragment
import java.util.concurrent.TimeUnit
import com.bignerd.photogallery.R


private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {
    private lateinit var photoRecyclerView: RecyclerView
    private var callbacks:Callbacks? = null

    private val photoGalleryViewModel: PhotoGalleryViewModel by lazy {
        ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
    }

    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>
    private lateinit var lifecycleLiveData: LiveData<LifecycleOwner>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        val responseHandler = Handler()
        lifecycleLiveData = viewLifecycleOwnerLiveData


        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            photoHolder.bindImage(BitmapDrawable(resources, bitmap))
        }

        lifecycleLiveData.observe(this, Observer { lifecycleOwner ->
            thumbnailDownloader.viewLifecycleOwner = lifecycleOwner
            lifecycleOwner?.lifecycle?.addObserver(thumbnailDownloader.viewLifecycleObserver)
        })

        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()

        val workRequest =
            OneTimeWorkRequest.Builder(PollWorker::class.java).setConstraints(constraints).build()
        WorkManager.getInstance().enqueue(workRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        photoRecyclerView = view.findViewById(R.id.photo_recycle_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
//        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemsLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems -> photoRecyclerView.adapter = PhotoAdapter(galleryItems) })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling)
            R.string.stop_polling
        else
            R.string.start_polling
        toggleItem.setTitle(toggleItemTitle)

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    photoGalleryViewModel.combinedValues.page.value = 1
                    photoGalleryViewModel.fetchPhotos(query)

                    val inputManager =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(windowToken, 0)

                    return true
                }
            })

            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                } else {
                    val constraints =
                        Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
                    val periodicWorkRequest =
                        PeriodicWorkRequest.Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                            .setConstraints(constraints).build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK,ExistingPeriodicWorkPolicy.KEEP,periodicWorkRequest)
                    QueryPreferences.setPolling(requireContext(),true)
                }
                activity?.invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }


    private inner class PhotoHolder(itemImageView: ImageView) : RecyclerView.ViewHolder(itemImageView),View.OnClickListener {
        val bindImage: (Drawable) -> Unit = itemImageView::setImageDrawable
        private lateinit var galleryItem: GalleryItem
        init {
            itemView.setOnClickListener(this)
        }

        fun bindGalleryItem(item:GalleryItem){
            galleryItem = item
        }

        override fun onClick(v: View?) {
            callbacks?.onPictureSelected(galleryItem.photoPageUri)
//            startActivity(Intent(Intent.ACTION_VIEW,galleryItem.photoPageUri))
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            /*val textView = TextView(parent.context)
            return PhotoHolder(textView)*/
            return PhotoHolder(
                layoutInflater.inflate(
                    R.layout.list_item_gallery,
                    parent,
                    false
                ) as ImageView
            )
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems.get(position)
            holder.bindGalleryItem(galleryItem)
            val gallerySize = galleryItems.size
            Log.e(TAG, "Gallery size is $gallerySize")
            if (position == gallerySize - 1)
                photoGalleryViewModel.fetchNextPage(position)
            holder.bindImage(context?.getDrawable(R.drawable.ic_launcher_background)!!)
//            holder.bindImage() //TODO
            val image = thumbnailDownloader.checkCache(galleryItem.url)
            if (image != null)
                holder.bindImage(BitmapDrawable(resources, image))
            else {
                thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
                if (position + 1 < galleryItems.size && thumbnailDownloader.checkCache(
                        galleryItems[position + 1].url
                    ) == null
                ) {
                    for (i in position..position + 10) {
                        if (i >= gallerySize - 1) {
                            break
                        }
                        thumbnailDownloader.queueCacheThumbnail(galleryItems[i].url)
                    }
                }
                if (position - 10 > 0 && thumbnailDownloader.checkCache(galleryItems.get(position - 1).url) == null) {
                    for (i in position..position - 10) {
                        thumbnailDownloader.queueCacheThumbnail(galleryItems[i].url)
                    }
                }
            }
        }

        override fun getItemCount(): Int = galleryItems.size

    }

    interface Callbacks{
        fun onPictureSelected(uri:Uri)
    }
}