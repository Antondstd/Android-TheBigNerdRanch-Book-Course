package com.bignerd.photogallery

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bignerd.photogallery.Fragments.PhotoGalleryFragment
import com.bignerd.photogallery.Fragments.PhotoPageFragment

class PhotoGalleryActivity : AppCompatActivity(), PhotoGalleryFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)
        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty)
            supportFragmentManager.beginTransaction().add(
                R.id.fragmentContainer,
                PhotoGalleryFragment.newInstance()
            ).commit()
    }

    override fun onPictureSelected(uri: Uri) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PhotoPageFragment.newInstance(uri),PhotoPageFragment.TAG)
            .addToBackStack(null).commit()
    }

    override fun onBackPressed() {
        val webViewFragment = supportFragmentManager.findFragmentByTag(PhotoPageFragment.TAG) as PhotoPageFragment
        if (webViewFragment.goBack())
            return
        super.onBackPressed()
    }
}