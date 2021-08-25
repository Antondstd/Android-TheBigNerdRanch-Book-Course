package com.bignerd.beatbox

import android.content.res.AssetFileDescriptor

private const val WAV = ".wav"
class Sound (val assetPath:String, var id:Int? = null)
{
    val name = assetPath.split("/").last().removeSuffix(WAV)
}