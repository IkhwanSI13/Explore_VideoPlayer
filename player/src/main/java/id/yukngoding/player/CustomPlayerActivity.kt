package id.yukngoding.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_player.*

class CustomPlayerActivity : AppCompatActivity() {

    var player: SimpleExoPlayer? = null

    var playWhenReady = true
    var currentWindow = 0
    var playbackPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_player)

        initializePlayer()
    }

    private fun initializePlayer() {
        //with adaptive streaming
        var trackSelector = DefaultTrackSelector(this)
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()

        //player = SimpleExoPlayer.Builder(this).build()
        player_view.player = player

        val mediaItemVideo =
            MediaItem.fromUri("https://ikhwankoto.com/_image/tes_video.mov")

        player?.let {
            it.setMediaItem(mediaItemVideo)

            it.playWhenReady = playWhenReady
            it.seekTo(currentWindow, playbackPosition)
            it.prepare()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        player_view.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }

    private fun releasePlayer() {
        player?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.release()
            player = null
        }
    }

    //lifecycle
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24)
            initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24 && player == null))
            initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

}