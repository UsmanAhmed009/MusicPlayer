package musicplayer.cs371m.musicplayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.zcw.togglebutton.ToggleButton
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private val TAG = "SettingsActivity"
    private var toggleBtn: ToggleButton? = null
    private var noBtn: Button? = null
    private var yesBtn: Button? = null
    private var songsCount: TextView? = null
    private var yesNoVar: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        INIT()
        Listeners()
        setUpData()
    }

    private fun setUpData() {
        if (Constants.LOOP) {
            toggleBtn!!.setToggleOn()
        } else {
            toggleBtn!!.setToggleOff()
        }
        songs_count.text = " ${Constants.SONGS_COUNT}"
    }

    private fun Listeners() {
        toggleBtn!!.setOnToggleChanged(ToggleButton.OnToggleChanged {
            if (it) {
                Log.e(TAG, "Listeners: YES")
                yesNoVar = 1
            } else {
                Log.e(TAG, "Listeners: NO")
                yesNoVar = 0
            }
        })

        yesBtn!!.setOnClickListener {
            if (yesNoVar == 3) {
                finish()
                return@setOnClickListener
            } else {
                Constants.LOOP = (yesNoVar == 1)
            }
            finish()
        }

        noBtn!!.setOnClickListener {
            finish()
        }

    }

    private fun INIT() {
        toggleBtn = findViewById(R.id.toggle_btn)
        noBtn = findViewById(R.id.btn_no)
        yesBtn = findViewById(R.id.btn_yes)
        songsCount = findViewById(R.id.songs_count)
    }
}