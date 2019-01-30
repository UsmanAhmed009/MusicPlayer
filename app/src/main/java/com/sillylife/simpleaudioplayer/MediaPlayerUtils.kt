package com.sillylife.simpleaudioplayer

import android.content.Context
import java.text.DateFormatSymbols
import java.util.*

object MediaPlayerUtils {

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        val secondsString: String

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours.toString() + ":"
        }

        if (minutes < 10) {
            finalTimerString += "0"
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0$seconds"
        } else {
            secondsString = "" + seconds
        }

        finalTimerString += minutes.toString() + ":" + secondsString

        // return timer string
        return finalTimerString
    }

    fun milliSecondsToSeconds(milliseconds: Long): Long {
        return milliseconds / 1000
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     */
    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Int {
        val percentage: Double?

        val currentSeconds = (currentDuration / 1000).toInt().toLong()
        val totalSeconds = (totalDuration / 1000).toInt().toLong()

        // calculating percentage
        percentage = currentSeconds.toDouble() / totalSeconds * 100

        // return percentage
        return percentage.toInt()
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     */
    fun progressToTimer(progress: Int, totalDuration: Int): Int {
        var totalDuration = totalDuration
        val currentDuration: Int
        totalDuration = totalDuration / 1000
        currentDuration = (progress.toDouble() / 100 * totalDuration).toInt()

        // return current duration in milliseconds
        return currentDuration * 1000
    }

    fun getTimeAgo(timestamp: String, context: Context): String? {
        val SECOND_MILLIS = 1000
        val MINUTE_MILLIS = 60 * SECOND_MILLIS
        val HOUR_MILLIS = 60 * MINUTE_MILLIS
        val DAY_MILLIS = 24 * HOUR_MILLIS

        var time = java.lang.Long.parseLong(timestamp)
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }

        val diff = now - time
        if (diff < MINUTE_MILLIS) {
            return context.getString(R.string.just_now)
        } else if (diff < 50 * MINUTE_MILLIS) {
            return context.resources
                    .getQuantityString(R.plurals.min_count_string,
                            diff.toInt() / MINUTE_MILLIS,
                            diff.toInt() / MINUTE_MILLIS)
        } else if (diff < 24 * HOUR_MILLIS) {
            return context.resources.getQuantityString(R.plurals.hour_count_string,
                    diff.toInt() / HOUR_MILLIS,
                    diff.toInt() / HOUR_MILLIS)
        } else {
            val myDate = Date(time)
            val calendar = GregorianCalendar()
            calendar.time = myDate

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val monthString = DateFormatSymbols().shortMonths[month - 1]
            return "$monthString $day, $year"
        }
    }

}