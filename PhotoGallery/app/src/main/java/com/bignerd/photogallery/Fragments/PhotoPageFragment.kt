package com.bignerd.photogallery.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bignerd.photogallery.R
import com.bignerd.photogallery.Service.VisibleFragment

private const val ARG_URI = "photo_page_url"

class PhotoPageFragment:VisibleFragment() {
    private lateinit var uri: Uri
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = arguments?.getParcelable<Uri>(ARG_URI) ?: Uri.EMPTY
        Log.e(TAG, "The URI is $uri")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_page,container,false)
        webView = view.findViewById(R.id.web_view)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(uri.toString())
        return view
    }

    fun goBack():Boolean{
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return false
    }

    companion object{
        const val TAG = "PhotoPageFragment"

        fun newInstance(uri: Uri):PhotoPageFragment{
            return PhotoPageFragment().apply {
                arguments = Bundle().apply { putParcelable(ARG_URI,uri) }
            }
        }
    }
}