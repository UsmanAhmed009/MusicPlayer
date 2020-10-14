package musicplayer.cs371m.musicplayer

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import java.io.IOException

class MainContentFragment : Fragment() {

    interface OnLocalAudioPickerFrag {
        fun onAudioSelected(song: Song)
        fun onFileManagerAudioSelected(song: Song)
    }

    private val TAG = "LocalAudioPickerFragmen"
    private var mediaPlayerLayout: LinearLayout? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playerSeekbar: SeekBar? = null
    private var currentTimeTv: TextView? = null
    private var totalTimeTv: TextView? = null
    private var noAudioTv: TextView? = null
    private var audioText: TextView? = null
    private var fileManager: LinearLayout? = null
    private var playPause: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var searchView: SearchView? = null

    //private var back: ImageView? = null
    private var localSongsAdapter: SongsAdapter? = null
    private var audioID: Long = 0
    private var mMediaHandler: Handler? = null

    //private var mListener: OnLocalAudioPickerFrag? = null
    private val RC_AUDIO = 125

    // I added this
    private var loop: TextView? = null
    private var isLoop: Boolean = false
    private var preSong: ImageView? = null
    private var nextSong: ImageView? = null
    private var nextSongTxt: TextView? = null
    private var currSongTxt: TextView? = null

    companion object {
        fun newInstance(): MainContentFragment {
            return MainContentFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.content_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeViews(view)
        mediaPlayer = MediaPlayer()
        val layoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = layoutManager as RecyclerView.LayoutManager?

        val audioList = getAudioList()
        if (audioList != null) {
            localSongsAdapter = SongsAdapter(context, audioList) { localAudio, i, view ->
                when (view.id) {
                    // I am changing this, before it was R.id.playLayout
                    R.id.song_layout -> {
                        if (localSongsAdapter?.getCurrentPlayingPos()!! != i && localSongsAdapter?.getCurrentPlayingPos()!! > -1) {
                            localSongsAdapter?.resetPreviouslyPlayedAudio()
                        }
                        if (localAudio.isPlay) {
//                            this was  localAudio.isPlay = false i changed it below
                            localAudio.isPlay = true
//                            localAudioAdapter?.setCurrentPlayingPos(-1)
                        } else {
                            localAudio.isPlay = true
                            localAudio.isHighlight = true
                            localSongsAdapter?.setCurrentPlayingPos(i)
                        }
                        togglePlayer(i, localAudio)
                        setTextFields()
                    }
                    R.id.select_song -> {
                        //mListener!!.onAudioSelected(localAudio)
                    }
                }
            }
            recyclerView!!.visibility = View.VISIBLE
            noAudioTv!!.visibility = View.GONE
        } else {
            recyclerView!!.visibility = View.GONE
            noAudioTv!!.visibility = View.VISIBLE
        }
        recyclerView!!.adapter = localSongsAdapter

        playPause?.setOnClickListener {
            var pos: Int = localSongsAdapter?.getCurrentPlayingPos()!!
            if (pos == -1) {
                return@setOnClickListener
            }
            var song: Song = localSongsAdapter?.getCurrentPlayingAudio()!!
            song.isPlay = !it?.tag?.equals(true)!!
            togglePlayer(pos, song)
            setTextFields()
        }

        mediaPlayer!!.setOnCompletionListener {
            var pos: Int = localSongsAdapter?.getCurrentPlayingPos()!!
            if (pos > -1) {
                destroyCurrSong()
                if (isLoop) {
                    performLoop()
                    setTextFields()
                    return@setOnCompletionListener
                }
                if (localSongsAdapter?.getCurrentPlayingPos()!! == localSongsAdapter?.itemCount!!.minus(1)) {
                    playSongAtPos(0)
                    setTextFields()
                    return@setOnCompletionListener
                }
                playSongAtPos(++pos)
            }
            setTextFields()
        }

        loop!!.setOnClickListener {
            if (isLoop) {
                isLoop = false
                Constants.LOOP = false
                loop!!.setBackgroundColor(resources.getColor(R.color.white));
            } else {
                isLoop = true
                Constants.LOOP = true
                loop!!.setBackgroundColor(resources.getColor(R.color.red));
            }
        }

        preSong!!.setOnClickListener {
            playPreSong()
        }

        nextSong!!.setOnClickListener {
            playNextSong()
        }

    }

    private fun playPreSong() {
        var pos: Int = localSongsAdapter?.getCurrentPlayingPos()!!
        if (pos > -1) {
            destroyCurrSong()
            if (pos == 0) {
                playSongAtPos(localSongsAdapter?.itemCount!!.minus(1))
            } else {
                playSongAtPos(--pos)
            }
        }
        setTextFields()
    }

    private fun playNextSong() {
        var pos: Int = localSongsAdapter?.getCurrentPlayingPos()!!
        if (pos > -1) {
            destroyCurrSong()
            if (pos == (localSongsAdapter?.itemCount!!.minus(1))) {
                playSongAtPos(0)
            } else {
                playSongAtPos(++pos)
            }
        }
        setTextFields()
    }

    private fun destroyCurrSong() {
        mediaPlayer!!.pause()
        val pos: Int = localSongsAdapter?.getCurrentPlayingPos()!!
        val song: Song = localSongsAdapter?.getCurrentPlayingAudio()!!
        song.isHighlight = false
        song.isPlay = false
        togglePlayer(pos, song)
    }

    private fun playSongAtPos(pos: Int) {
        var nextSong: Song = localSongsAdapter?.getSongAtPos(pos)!!
        nextSong.isPlay = true
        nextSong.isHighlight = true
        localSongsAdapter?.setCurrentPlayingPos(pos)
        togglePlayer(pos, nextSong)
    }

    private fun initializeViews(view: View) {
        ////////////////////////////////////////////////////////////////////////
        loop = view.findViewById(R.id.loop)
        preSong = view.findViewById(R.id.play_pre_song)
        nextSong = view.findViewById(R.id.play_next_song)
        nextSongTxt = view.findViewById(R.id.next_song_txt)
        currSongTxt = view.findViewById(R.id.curr_song_txt)
        ////////////////////////////////////////////////////////////////////////
        mediaPlayerLayout = view.findViewById(R.id.mediaPlayerLayout)
        currentTimeTv = view.findViewById(R.id.current_time)
        fileManager = view.findViewById(R.id.filemanager)
        totalTimeTv = view.findViewById<TextView>(R.id.total_time)
        recyclerView = view.findViewById<RecyclerView>(R.id.list)
        searchView = view.findViewById(R.id.search)
        playerSeekbar = view.findViewById(R.id.player_seekbar)
        audioText = view.findViewById<TextView>(R.id.title)
        playPause = view.findViewById(R.id.play_pause)
        noAudioTv = view.findViewById(R.id.no_audio)
//        back = view.findViewById(R.id.back)
//        back!!.setOnClickListener {
//            activity!!.finish()
//        }
        mMediaHandler = Handler()
        searchView?.isActivated = false
        searchView?.queryHint = "Type your filename here"
        searchView?.onActionViewExpanded()
        searchView?.isIconified = false
        searchView?.clearFocus()
        searchView?.setOnQueryTextListener(searchOnTextChangeListener)
        fileManager?.setOnClickListener {
            val intent = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                Intent(Intent.ACTION_GET_CONTENT)
            } else {
                Intent(Intent.ACTION_OPEN_DOCUMENT)
            }

            intent.type = "audio/*"
            startActivityForResult(intent, RC_AUDIO)
//
            //val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            //intent.type = "audio/*"
            //this.startActivityForResult(Intent.createChooser(intent, "choose track..."), RC_AUDIO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        when (intentData) {
            null -> {
                //data is null for some reason
                //TODO log
            }
            else -> {
                if (requestCode == RC_AUDIO) {
                    // mListener!!.onFileManagerAudioSelected(LocalAudio(0, FileUploadUtils.getNameFromUri(intentData.data!!, this.activity!!), null, 0, intentData.data.toString(), null))
                }
            }
        }

    }

    private fun togglePlayer(pos: Int, song: Song) {

        mediaPlayerLayout!!.visibility = View.VISIBLE
        playPause?.tag = song.isPlay
        localSongsAdapter?.notifyItemChanged(pos, song)
        if (song.isPlay) {
            playPause!!.setImageResource(R.drawable.ic_pause_black)
            if (!mediaPlayer!!.isPlaying && song.audioId == audioID) {
                mediaPlayer!!.start()
                return
            }
            val totalEpisodeDuration = MusicPlayerUtils.milliSecondsToTimer(song.audioDuration)
            totalTimeTv!!.text = totalEpisodeDuration
            currentTimeTv!!.text = "00:00"
            playerSeekbar!!.max = song.audioDuration.toInt()
            audioText!!.text = song.audioTitle
            playerSeekbar!!.setOnSeekBarChangeListener(seekBarChangeListener)

            audioID = song.audioId

            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            try {
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer!!.setDataSource(activity!!, Uri.parse(song.audioUri))
                mediaPlayer!!.prepare()
                mediaPlayer!!.start()
                startSeekbarUpdate()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            playPause!!.setImageResource(R.drawable.ic_play_arrow_black)
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }
        }
//        This was not commented I (Usman) commented it
//        recyclerView?.scrollToPosition(pos)

    }

    private fun setTextFields() {
        Constants.SONGS_COUNT++
        val pos = localSongsAdapter?.getCurrentPlayingPos()!!
        if (pos == -1) return
        currSongTxt!!.text = localSongsAdapter?.getSongAtPos(pos)!!.audioTitle
        Log.e(TAG, "setTextFields: pos=  $pos")
        if (pos == localSongsAdapter?.itemCount!!.minus(1)) {
            nextSongTxt!!.text = localSongsAdapter?.getSongAtPos(0)!!.audioTitle
        } else {
            nextSongTxt!!.text = localSongsAdapter?.getSongAtPos(pos + 1)!!.audioTitle
        }
    }

    private fun startSeekbarUpdate() {
        mMediaHandler!!.postDelayed(mMediaSeekRunnable, 100)
    }

    private val mMediaSeekRunnable = object : Runnable {
        override fun run() {
            if (mMediaHandler != null) {
                activity!!.runOnUiThread {
                    playerSeekbar!!.progress = mediaPlayer!!.currentPosition
                    val time = MusicPlayerUtils.milliSecondsToTimer(mediaPlayer!!.currentPosition.toLong())
                    currentTimeTv!!.text = time
                }
                mMediaHandler!!.postDelayed(this, 100)
            }
        }
    }

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                val time = MusicPlayerUtils.milliSecondsToTimer(progress.toLong())
                currentTimeTv!!.text = time
                mediaPlayer!!.seekTo(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
    }

    private val searchOnTextChangeListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String?): Boolean {
            localSongsAdapter!!.filter.filter(newText)
            return false
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            if (!TextUtils.isEmpty(query)) {
                localSongsAdapter!!.filter.filter(query!!)
            }
            return false
        }
    }

    private fun getAudioList(): ArrayList<Song> {
        val audioList = ArrayList<Song>()
        val musicResolver = context!!.contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, MediaStore.Audio.Media.DATE_MODIFIED)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            val duration = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION)
            val location = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA)
            val date_Modified = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATE_MODIFIED)
            do {
                try {
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    val thisDuration = java.lang.Long.parseLong(musicCursor.getString(duration))
                    val audioUri = musicCursor.getString(location)
                    val dateModified = musicCursor.getString(date_Modified)
                    if (audioUri.endsWith(".mp3", true) ||
                            audioUri.endsWith(".mpeg", true) ||
                            audioUri.endsWith(".m4a", true)) {

                        val title = getAudioTitle(audioUri, thisTitle)

                        audioList.add(Song(thisId, title, thisArtist, thisDuration, audioUri, dateModified))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } while (musicCursor.moveToNext())
        }
        audioList.reverse()
        return audioList
    }

    private fun getAudioTitle(uri: String, title: String): String {
        val array = uri.split("/")
        val fileName = array[array.size - 1]

        val lastIndex = fileName.split(".")
        val audioTitle = title + "." + lastIndex[lastIndex.size - 1]

        var finalAudioTitle: String? = null

        finalAudioTitle = if (fileName.contains(audioTitle, true)) {
            fileName
        } else {
            "$title - $fileName"
        }
        return finalAudioTitle.toString()
    }

    override fun onDetach() {
        super.onDetach()
        mediaPlayer!!.stop()
        mMediaHandler!!.removeCallbacks(mMediaSeekRunnable)
        //mListener = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
//        if (context is OnLocalAudioPickerFrag) {
//            mListener = context
//        } else {
//            throw RuntimeException(context!!.toString() + " must implement OnLocalAudioPickerFrag")
//        }
    }

    fun performLoop() {
        if (isLoop) {
            Log.e(TAG, "isLoop: $isLoop")
            var pos: Int = localSongsAdapter?.getCurrentPlayingPos()!!
            var song: Song = localSongsAdapter?.getCurrentPlayingAudio()!!
            Log.e(TAG, "currLocalAudio: " + song.audioTitle + " -  pos: " + pos)
            song.isPlay = true
            song.isHighlight = true
            localSongsAdapter?.setCurrentPlayingPos(pos)
            togglePlayer(pos, song)
        }
    }

    override fun onResume() {
        if (Constants.LOOP) {
            isLoop = true
            loop!!.setBackgroundColor(resources.getColor(R.color.black));

        } else {
            isLoop = false
            loop!!.setBackgroundColor(resources.getColor(R.color.red));
        }
        super.onResume()
    }

}
