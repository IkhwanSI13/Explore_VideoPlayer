package id.yukngoding.explore_exoplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv.setOnClickListener {
            var intent = Intent(
                this,
//                Class.forName("id.yukngoding.player.PlayerActivity")
//                Class.forName("id.yukngoding.player.CustomPlayerActivity")
                Class.forName("id.yukngoding.player.myPlayer.MyPlayerActivity")
            )
            startActivity(intent)
        }
    }
}