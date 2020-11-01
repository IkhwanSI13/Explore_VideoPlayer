package id.yukngoding.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_player.*

//Simple player
//Adaptive streaming
//Event listener
//Playlist
class PlayerActivity : AppCompatActivity() {

    var player: SimpleExoPlayer? = null

    var playWhenReady = true
    var currentWindow = 0
    var playbackPosition: Long = 0

    var TAG = PlayerActivity::class.java.name

    lateinit var playbackStatsListener: PlaybackStateListener

    class PlaybackStateListener : Player.EventListener {
        override fun onPlaybackStateChanged(state: Int) {
            var stateString: String
            when (state) {
                ExoPlayer.STATE_IDLE -> {
                    stateString = "ExoPlayer.STATE_IDLE      -"
                }
                ExoPlayer.STATE_BUFFERING -> {
                    stateString = "ExoPlayer.STATE_BUFFERING -"
                }
                ExoPlayer.STATE_READY -> {
                    stateString = "ExoPlayer.STATE_READY     -"
                }
                ExoPlayer.STATE_ENDED -> {
                    stateString = "ExoPlayer.STATE_ENDED     -"
                }
                else -> {
                    stateString = "UNKNOWN_STATE             -"
                }
            }
            Log.e("IKHWAN", "changed state to " + stateString)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playbackStatsListener = PlaybackStateListener()

        initializePlayer()
    }

    private fun initializePlayer() {
        //with adaptive streaming
        var trackSelector = DefaultTrackSelector(this)
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()

        //player = SimpleExoPlayer.Builder(this).build()
        player_view.player = player

        //normal Media Item
        val mediaItemMusic =
            MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
        val mediaItemVideo =
            MediaItem.fromUri("https://ikhwankoto.com/_image/tes_video.mov")

        //dash Media Item
        var mediaItemDash = MediaItem.Builder()
            .setUri("https://ikhwankoto.com/_image/tes_video.mov")
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .build()

        player?.let {
            it.setMediaItem(mediaItemVideo)
            //it.setMediaItem(mediaItemDash)

            //Add more thant one setMediaItem to create a PLAY LIST
            //it.addMediaItem(mediaItemMusic)

            it.playWhenReady = playWhenReady
            it.seekTo(currentWindow, playbackPosition)
            //Listener
            it.addListener(playbackStatsListener)
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
            it.removeListener(playbackStatsListener)
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