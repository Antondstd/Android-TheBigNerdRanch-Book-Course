package com.bignerd.beatbox

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerd.beatbox.databinding.ActivityMainBinding
import com.bignerd.beatbox.databinding.ListItemSoundBinding

private const val KEY_BEATBOX = "BEATBOX"
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {


    private val beatBoxViewModel: BeatBoxViewModel by lazy {
        ViewModelProvider(this).get(
            BeatBoxViewModel::class.java
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (beatBoxViewModel.beatBox == null)
            beatBoxViewModel.beatBox = BeatBox(assets)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = SoundAdapter(beatBoxViewModel.beatBox!!.sounds)
        }
    }

    private inner class SoundHolder(private val binding: ListItemSoundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.soundViewModel = SoundViewModel(beatBoxViewModel.beatBox!!)
        }

        fun bind(sound: Sound) {
            binding.apply {
                soundViewModel?.sound = sound
                executePendingBindings()
            }
        }
    }

    private inner class SoundAdapter(private val sounds: List<Sound>) :
        RecyclerView.Adapter<SoundHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundHolder {
            val binding = DataBindingUtil.inflate<ListItemSoundBinding>(
                layoutInflater,
                R.layout.list_item_sound,
                parent,
                false
            )
            return SoundHolder(binding)
        }

        override fun onBindViewHolder(holder: SoundHolder, position: Int) {
            val sound = sounds.get(position)
            holder.bind(sound)
        }

        override fun getItemCount(): Int = sounds.size
    }
}