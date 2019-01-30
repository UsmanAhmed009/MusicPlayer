package com.sillylife.simpleaudioplayer

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var mFragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        mFragmentManager = supportFragmentManager

        showAudioPicker()

    }

    private fun showAudioPicker() {
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.container, LocalAudioPickerFragment.newInstance())
        fragmentTransaction.commitAllowingStateLoss()
    }
}