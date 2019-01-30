package com.sillylife.simpleaudioplayer

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

class AudioAdapter(val context: Context?, private val localAudioLists: ArrayList<LocalAudio>, val listener: (LocalAudio, Int, View) -> Unit) : RecyclerView.Adapter<AudioAdapter.ViewHolder>(), Filterable {
    private var mCurrentPlayingPos = -1
    private var valueFilter: ValueFilter? = null
    var mLocalAudioList: ArrayList<LocalAudio> = localAudioLists.clone() as ArrayList<LocalAudio>

    companion object {
        const val PAUSE = "pause"
        const val PLAY = "play"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio_layout, parent, false)
        return ViewHolder(view, context)
    }

    class ViewHolder(itemView: View, context: Context?) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var duration: TextView = itemView.findViewById(R.id.duration)
        var dateModified: TextView = itemView.findViewById(R.id.author)
        var location: TextView = itemView.findViewById(R.id.location)
        var addLayout: LinearLayout = itemView.findViewById(R.id.select_song)
        var playLayout: LinearLayout = itemView.findViewById(R.id.playLayout)
        var play: ImageView = itemView.findViewById(R.id.play)
        val mContext: Context? = context

        fun togglePlayButton(showPlayBtn: Boolean, isHighlight:Boolean) {
            play.setImageResource(if(showPlayBtn) R.drawable.ic_play_arrow_black else R.drawable.ic_pause_black)
            title.setTextColor(if(isHighlight) ContextCompat.getColor(mContext!!, R.color.colorAccent) else ContextCompat.getColor(mContext!!, R.color.black))
        }

        fun bind(pos: Int, item: LocalAudio, listener: (LocalAudio, Int, View) -> Unit) = with(itemView) {
            val time = MediaPlayerUtils.milliSecondsToTimer(item.audioDuration)
            val array = item.audioUri?.split("/")
            val loc = array!![array.size - 3] + "/" + array[array.size - 2]

            title.text = item.audioTitle
            dateModified.text = MediaPlayerUtils.getTimeAgo(item.dateModified!!, context)
            location.text = loc
            duration.text = time
            playLayout.tag = item
            playLayout.setOnClickListener {
                listener(item, pos, it)
            }
            addLayout.setOnClickListener {
                listener(item, pos, it)
            }
            togglePlayButton(!item.isPlay, item.isHighlight)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mLocalAudioList[position]
        holder.bind(position, item, listener)
    }

    override fun getItemCount(): Int {
        return mLocalAudioList.size
    }

    override fun getFilter(): Filter {
        if (valueFilter == null) {
            valueFilter = ValueFilter()
        }
        return valueFilter!!
    }

    fun setCurrentPlayingPos(pos: Int) {
        mCurrentPlayingPos = pos
    }

    fun getCurrentPlayingPos(): Int {
        return mCurrentPlayingPos;
    }

    fun getCurrentPlayingAudio(): LocalAudio {
        return mLocalAudioList[mCurrentPlayingPos]
    }

    fun resetPreviouslyPlayedAudio() {
        val item = mLocalAudioList.get(mCurrentPlayingPos)
        item.isPlay = false
        item.isHighlight = false
        notifyItemChanged(mCurrentPlayingPos, item)
    }

    inner class ValueFilter : Filter() {
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            mLocalAudioList = results!!.values as ArrayList<LocalAudio>
            notifyDataSetChanged()
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = localAudioLists.size
                filterResults.values = localAudioLists
            } else {
                val filterList: ArrayList<LocalAudio> = ArrayList<LocalAudio>()
                for (item in localAudioLists) {
                    if (item.audioTitle?.contains(constraint!!, ignoreCase = true)!!) {
                        filterList.add(item)
                    }
                }
                filterResults.count = filterList.size
                filterResults.values = filterList
            }
            return filterResults
        }

    }
}