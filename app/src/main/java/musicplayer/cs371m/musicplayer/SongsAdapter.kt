package musicplayer.cs371m.musicplayer

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

class SongsAdapter(val context: Context?, private val songLists: ArrayList<Song>,
                   val listener: (Song, Int, View) -> Unit) : RecyclerView.Adapter<SongsAdapter.ViewHolder>(), Filterable {
    private var mCurrentPlayingPos = -1
    private var valueFilter: ValueFilter? = null
    var mSongList: ArrayList<Song> = songLists.clone() as ArrayList<Song>

    companion object {
        const val PAUSE = "pause"
        const val PLAY = "play"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_row, parent, false)
        return ViewHolder(view, context)
    }

    class ViewHolder(itemView: View, context: Context?) : RecyclerView.ViewHolder(itemView) {
        var songLayout : LinearLayout = itemView.findViewById(R.id.song_layout)
        var title: TextView = itemView.findViewById(R.id.title)
        var duration: TextView = itemView.findViewById(R.id.duration)
        var dateModified: TextView = itemView.findViewById(R.id.author)
        var location: TextView = itemView.findViewById(R.id.location)
        var addLayout: LinearLayout = itemView.findViewById(R.id.select_song)
        var playLayout: LinearLayout = itemView.findViewById(R.id.playLayout)
        var play: ImageView = itemView.findViewById(R.id.play)
        val mContext: Context? = context

        fun togglePlayButton(showPlayBtn: Boolean, isHighlight: Boolean) {
            play.setImageResource(if (showPlayBtn) R.drawable.ic_play_arrow_black else R.drawable.ic_pause_black)
//            title.setTextColor(if (isHighlight) ContextCompat.getColor(mContext!!, R.color.colorAccent) else ContextCompat.getColor(mContext!!, R.color.black))
    }

        fun bind(pos: Int, item: Song, listener: (Song, Int, View) -> Unit) = with(itemView) {
            val time = MusicPlayerUtils.milliSecondsToTimer(item.audioDuration)
            val array = item.audioUri?.split("/")
            val loc = array!![array.size - 3] + "/" + array[array.size - 2]

            title.text = item.audioTitle
            dateModified.text = MusicPlayerUtils.getTimeAgo(item.dateModified!!, context)
            location.text = loc
            duration.text = time
            playLayout.tag = item
            songLayout.setOnClickListener (View.OnClickListener
            {
                listener(item, pos, it)
            })
//            playLayout.setOnClickListener {
//                listener(item, pos, it)
//            }

//            addLayout.setOnClickListener {
//                listener(item, pos, it)
//            }
            togglePlayButton(!item.isPlay, item.isHighlight)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mSongList[position]
        holder.bind(position, item, listener)
    }

    override fun getItemCount(): Int {
        return mSongList.size
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

    fun getCurrentPlayingAudio(): Song {
        return mSongList[mCurrentPlayingPos]
    }

    fun getSongAtPos(pos : Int) : Song {
        return mSongList[pos]
    }

    fun resetPreviouslyPlayedAudio() {
        val item = mSongList.get(mCurrentPlayingPos)
        item.isPlay = false
        item.isHighlight = false
        notifyItemChanged(mCurrentPlayingPos, item)
    }

    inner class ValueFilter : Filter() {
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            mSongList = results!!.values as ArrayList<Song>
            notifyDataSetChanged()
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = songLists.size
                filterResults.values = songLists
            } else {
                val filterList: ArrayList<Song> = ArrayList<Song>()
                for (item in songLists) {
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