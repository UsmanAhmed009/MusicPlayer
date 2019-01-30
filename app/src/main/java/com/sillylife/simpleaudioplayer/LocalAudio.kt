package com.sillylife.simpleaudioplayer

import android.os.Parcel
import android.os.Parcelable

class LocalAudio : Parcelable {

    var audioId: Long = 0
    var audioTitle: String? = null
    var audioArtist: String? = null
    var audioDuration: Long = 0
    var audioUri: String? = null
    var dateModified: String? = null
    var isPlay = false
    var isHighlight = false

    constructor(audioId: Long, audioTitle: String, audioArtist: String, audioDuration: Long, audioUri: String, dateModified: String) {
        this.audioId = audioId
        this.audioTitle = audioTitle
        this.audioArtist = audioArtist
        this.audioDuration = audioDuration
        this.audioUri = audioUri
        this.dateModified = dateModified
    }

    protected constructor(`in`: Parcel) {
        audioId = `in`.readLong()
        audioTitle = `in`.readString()
        audioArtist = `in`.readString()
        audioDuration = `in`.readLong()
        audioUri = `in`.readString()
        dateModified = `in`.readString()
        isPlay = `in`.readByte().toInt() != 0
        isHighlight = `in`.readByte().toInt() != 0
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(audioId)
        dest.writeString(audioTitle)
        dest.writeString(audioArtist)
        dest.writeLong(audioDuration)
        dest.writeString(audioUri)
        dest.writeString(dateModified)
        dest.writeByte((if (isPlay) 1 else 0).toByte())
        dest.writeByte((if (isHighlight) 1 else 0).toByte())
    }

    companion object CREATOR : Parcelable.Creator<LocalAudio> {
        override fun createFromParcel(parcel: Parcel): LocalAudio {
            return LocalAudio(parcel)
        }

        override fun newArray(size: Int): Array<LocalAudio?> {
            return arrayOfNulls(size)
        }
    }

}
