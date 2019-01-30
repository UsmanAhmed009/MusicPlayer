package com.sillylife.simpleaudioplayer

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

import java.io.IOException

class LocalAudioPickerFragment : Fragment() {

    interface OnLocalAudioPickerFrag {
        fun onAudioSelected(localAudio: LocalAudio)
        fun onFileManagerAudioSelected(localAudio: LocalAudio)
    }

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
    private var localAudioAdapter: AudioAdapter? = null
    private var audioID: Long = 0
    private var mMediaHandler: Handler? = null
    //private var mListener: OnLocalAudioPickerFrag? = null
    private val RC_AUDIO = 125

    companion object {
        fun newInstance(): LocalAudioPickerFragment {
            return LocalAudioPickerFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audio_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeViews(view)
        mediaPlayer = MediaPlayer()
        val layoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = layoutManager as RecyclerView.LayoutManager?

        val audioList = getAudioList()
        if (audioList != null) {
            localAudioAdapter = AudioAdapter(context, audioList) { localAudio, i, view ->
                when (view.id) {
                    R.id.playLayout -> {

                        if (localAudioAdapter?.getCurrentPlayingPos()!! != i && localAudioAdapter?.getCurrentPlayingPos()!! > -1) {
                            localAudioAdapter?.resetPreviouslyPlayedAudio()
                        }
                        if (localAudio.isPlay) {
                            localAudio.isPlay = false
//                            localAudioAdapter?.setCurrentPlayingPos(-1)
                        } else {
                            localAudio.isPlay = true
                            localAudio.isHighlight = true
                            localAudioAdapter?.setCurrentPlayingPos(i)
                        }
                        togglePlayer(i, localAudio)
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
        recyclerView!!.adapter = localAudioAdapter

        playPause?.setOnClickListener {
            var pos: Int = localAudioAdapter?.getCurrentPlayingPos()!!
            var localAudio: LocalAudio = localAudioAdapter?.getCurrentPlayingAudio()!!
            localAudio.isPlay = !it?.tag?.equals(true)!!
            togglePlayer(pos, localAudio)
        }
        mediaPlayer!!.setOnCompletionListener {
            mediaPlayer!!.pause()
            var pos: Int = localAudioAdapter?.getCurrentPlayingPos()!!
            if (pos > -1) {
                var localAudio: LocalAudio = localAudioAdapter?.getCurrentPlayingAudio()!!
                localAudio.isPlay = false
                togglePlayer(pos, localAudio)
            }
        }
    }

    private fun initializeViews(view: View) {
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

    private fun togglePlayer(pos: Int, localAudio: LocalAudio) {

        mediaPlayerLayout!!.visibility = View.VISIBLE
        playPause?.tag = localAudio.isPlay
        localAudioAdapter?.notifyItemChanged(pos, localAudio)
        if (localAudio.isPlay) {

            //playPause!!.setImageResource(R.drawable.ic_round_pause_button)

            if (!mediaPlayer!!.isPlaying && localAudio.audioId == audioID) {
                mediaPlayer!!.start()
                return
            }

            val totalEpisodeDuration = MediaPlayerUtils.milliSecondsToTimer(localAudio.audioDuration)
            totalTimeTv!!.text = totalEpisodeDuration
            currentTimeTv!!.text = "00:00"
            playerSeekbar!!.max = localAudio.audioDuration.toInt()
            audioText!!.text = localAudio.audioTitle
            playerSeekbar!!.setOnSeekBarChangeListener(seekBarChangeListener)

            audioID = localAudio.audioId

            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            try {
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer!!.setDataSource(activity!!, Uri.parse(localAudio.audioUri))
                mediaPlayer!!.prepare()
                mediaPlayer!!.start()
                startSeekbarUpdate()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
     //       playPause!!.setImageResource(R.drawable.ic_play_button_2)
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }
        }
        recyclerView?.scrollToPosition(pos)

    }

    private fun startSeekbarUpdate() {
        mMediaHandler!!.postDelayed(mMediaSeekRunnable, 100)
    }

    private val mMediaSeekRunnable = object : Runnable {
        override fun run() {
            if (mMediaHandler != null) {
                activity!!.runOnUiThread {
                    playerSeekbar!!.progress = mediaPlayer!!.currentPosition
                    val time = MediaPlayerUtils.milliSecondsToTimer(mediaPlayer!!.currentPosition.toLong())
                    currentTimeTv!!.text = time
                }
                mMediaHandler!!.postDelayed(this, 100)
            }
        }
    }

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                val time = MediaPlayerUtils.milliSecondsToTimer(progress.toLong())
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
            localAudioAdapter!!.filter.filter(newText)
            return false
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            if (!TextUtils.isEmpty(query)) {
                localAudioAdapter!!.filter.filter(query!!)
            }
            return false
        }
    }

    private fun getAudioList(): ArrayList<LocalAudio> {
        val audioList = ArrayList<LocalAudio>()
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

                        audioList.add(LocalAudio(thisId, title, thisArtist, thisDuration, audioUri, dateModified))
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
}
