package com.bignerd.photogallery.Service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.fragment.app.Fragment

abstract class VisibleFragment : Fragment() {
    private val onShowNotifiaction = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(requireContext(), "Got a broadcast ${intent.action}", Toast.LENGTH_LONG)
                .show()
            resultCode = Activity.RESULT_CANCELED
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(
            onShowNotifiaction,
            IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION),
            PollWorker.PERM_PRIVATE,
            null
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotifiaction)
    }
}