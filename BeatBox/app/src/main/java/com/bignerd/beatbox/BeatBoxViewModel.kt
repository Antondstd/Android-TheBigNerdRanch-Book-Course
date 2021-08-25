package com.bignerd.beatbox

import androidx.lifecycle.ViewModel

class BeatBoxViewModel():ViewModel() {
    var beatBox:BeatBox? = null
    var test:String? = null

    override fun onCleared() {
        beatBox?.release()
    }

}