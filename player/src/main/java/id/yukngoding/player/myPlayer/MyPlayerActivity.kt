package id.yukngoding.player.myPlayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import id.yukngoding.player.CheckPermission
import id.yukngoding.player.R
import kotlinx.android.synthetic.main.activity_my_player.*
import kotlinx.android.synthetic.main.my_playback_control.*

class MyPlayerActivity : AppCompatActivity(), MyPlayerContract.View, View.OnClickListener {

    private var videoPresenter: MyPlayerPresenter? = null

    //var mediaDao: MediaDao? = null
    var contentEmbed: String? = null

    var orientationState: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    //Start Exo
    var playbackPosition: Long = 0L
    var currentWindow: Int = 0

    private var isInPipMode = false
    private var videoPlayer: SimpleExoPlayer? = null
    //End Exo

    //Media Source
    private val BANDWIDTH_METER = DefaultBandwidthMeter()

    private fun initBeforeCreateContent() {
        requestFullscreen()
    }

    private fun requestFullscreen() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        supportActionBar?.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBeforeCreateContent()
        setContentView(R.layout.activity_my_player)

        exo_screen.setOnClickListener(this)
        exo_replay.setOnClickListener(this)
        exo_forward.setOnClickListener(this)
        initCreate()
    }

    private fun initCreate() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        videoPresenter = MyPlayerPresenter(this, this)

        initView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("Ikhwan", "onConfigurationChanged")
        initView()
    }

    private fun initView() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1 &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            Log.e("Ikhwan", "initView need permission")
            callPiePermission()
        } else {
            Log.e("Ikhwan", "initView not need permission")

            if (orientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                Log.e("Ikhwan", "initView landscape")
                landscapeMode()
            } else {
                Log.e("Ikhwan", "initView potrait")
                portraitMode()
            }

            videoPlayer?.let {
                Log.e("Ikhwan", "initView videoPlayer != null")
                it.playWhenReady = true
            } ?: run {
                Log.e("Ikhwan", "initView videoPlayer == null")
                //TODO change with page data
                setContent("https://ikhwankoto.com/_image/tes_video.mov")
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.exo_screen -> {
                switchOrientation()
            }
            R.id.exo_replay -> { //- 10 second
                videoPlayer?.let {
                    playbackPosition = it.currentPosition - 10000
                    if (playbackPosition < 0L) {
                        playbackPosition = 0L
                    }
                    currentWindow = it.currentWindowIndex
                    setVideoPosition()
                }
            }
            R.id.exo_forward -> { //+10 second
                videoPlayer?.let {
                    playbackPosition = it.currentPosition + 10000
                    if (playbackPosition > it.duration) {
                        playbackPosition = 0L
                    }
                    currentWindow = it.currentWindowIndex
                    setVideoPosition()
                }
            }
        }
    }

    /**
     * Start Data
     * */

    //this method called from presenter, but to simplify it call from this class
    override fun setContent(dao: String?) {
        dao?.let {
            //Set data from API
            contentEmbed = dao

            //videoPresenter.saveDaoHistory()

            if (TextUtils.isEmpty(contentEmbed)) {
                Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show()
                super.finish()
            }
            //Todo set play position with the data from API
//            if (playbackPosition == 0L) {
//                playbackPosition = mediaDao!!.lastTime.toLong()
//            }
            initExoVideo()
        } ?: run {
            if (TextUtils.isEmpty(contentEmbed)) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                super.finish()
            } else {
                initExoVideo()
            }
        }
    }

    /**
     * End Data
     * Start Lifecycle
     * */

    override fun finish() {
        Log.e("Ikhwan", "lifecycle finish")
        if (orientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            switchOrientation()
        } else {
            if (videoPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            ) {
                enterPIPMode()
            } else {
                super.finish()
            }
        }
    }

    public override fun onStart() {
        Log.e("Ikhwan", "lifecycle onStart")
        try {
            super.onStart()
            val handler = Handler()
            val runnable = Runnable {
                initView()
            }
            handler.postDelayed(runnable, 300)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onResume() {
        Log.e("Ikhwan", "lifecycle onResume")
        try {
            super.onResume()
            if (videoPlayer != null) {
                if (playbackPosition > 0L && !isInPipMode) {
                    portraitMode()
                    videoPlayer!!.seekTo(currentWindow, playbackPosition)
                }
                val handler = Handler(Looper.getMainLooper())
                val runnable = Runnable {
                    orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    requestedOrientation = orientationState
                }
                handler.postDelayed(runnable, 300)
                try {
                    if (exo_video != null) {
                        exo_video.useController = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onPause() {
        Log.e("Ikhwan", "lifecycle onPause")
        try {
            super.onPause()
            if (Util.SDK_INT <= Build.VERSION_CODES.M) {
                pauseVideo()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onStop() {
        Log.e("Ikhwan", "lifecycle onStop")
        try {
            super.onStop()
            if (Util.SDK_INT > Build.VERSION_CODES.M) {
                pauseVideo()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (orientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            switchOrientation()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                handleFinish()
            } else {
                super.finish()
            }
            super.finish()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (videoPlayer != null) {
            enterPIPMode()
        }
    }

    override fun onDestroy() {
        //saveLastTime()
        releaseExoVideo()
        super.onDestroy()
    }

    private fun switchOrientation() {
        videoPlayer?.let {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.playWhenReady = false
        }
        if (orientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        requestedOrientation = orientationState
    }

    /**
     * End Lifecycle
     * Start Video
     * */

    private fun landscapeMode() {
        orientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            ll_parent.post {
                val w = ll_parent.width
                val h = ll_parent.height
                if (w > 0 && h > 0) {
                    ll_exo_video.layoutParams = ConstraintLayout.LayoutParams(w, h)
                }
            }
        }
        handler.postDelayed(runnable, 300)
    }

    private fun portraitMode() {
        orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            ll_parent.post {
                val w = ll_parent.width /*- UnitHelper.intToDp(32)*/
                val h = (w * 9 / 16).toInt()
                if (w > 0 && h > 0) {
                    ll_exo_video.layoutParams = ConstraintLayout.LayoutParams(w, h)
                }
            }
        }
        handler.postDelayed(runnable, 300)
    }

    private fun pauseVideo() {
        pauseExoVideo()
        //saveLastTime()
    }

    /**
     * End Video
     * Start Exo Player
     * */

    private fun setVideoPosition() {
        videoPlayer?.playWhenReady = true
        videoPlayer?.seekTo(currentWindow, playbackPosition)
    }

    @SuppressLint("ServiceCast")
    private fun initExoVideo() {
        Log.e("Ikhwan", "initExoVideo run")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
        ) {
            //todo delete this, and try pip
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            if (appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA
                    ).uid, packageName
                )
                == AppOpsManager.MODE_ALLOWED
            ) {
            }
        }

        if (videoPlayer == null) {
            Log.e("Ikhwan", "initExoVideo videoplayer ==null")
            //todo test up
//            videoPlayer = ExoPlayerFactory.newSimpleInstance(
//                this,
//                DefaultRenderersFactory(this),
//                DefaultTrackSelector(),
//                DefaultLoadControl()
//            )
            videoPlayer = SimpleExoPlayer.Builder(this).build()

            exo_video.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            exo_video.controllerHideOnTouch = false
            exo_video.controllerShowTimeoutMs = 2000
            exo_video.player = videoPlayer
            setVideoPosition()
        } else {
            Log.e("Ikhwan", "initExoVideo videoplayer else")
            videoPlayer?.playWhenReady = true
            videoPlayer?.seekTo(currentWindow, playbackPosition)
        }
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build();
        videoPlayer!!.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true);

        if (!TextUtils.isEmpty(contentEmbed)) {
            Log.e("Ikhwan", "initExoVideo contentEmbed isNotEmpty")

            /**
             * Use Media Source
             * */
//            val mediaSource = buildMediaSource(contentEmbed)
//            if (mediaSource != null) {
//                videoPlayer!!.setMediaSource(mediaSource)
//            }

            /**
             * Use Media Item
             * */
            val mediaItemVideo =
                MediaItem.fromUri("https://ikhwankoto.com/_image/sample_video.mp4")
            videoPlayer?.setMediaItem(mediaItemVideo)

            videoPlayer?.playWhenReady
            videoPlayer?.prepare()

            //todo check
            //mostly used for android tv / android auto like controlling android remotely
            //https://medium.com/google-exoplayer/the-mediasession-extension-for-exoplayer-82b9619deb2d
            //https://developer.android.com/codelabs/supporting-mediasession#5
            val mediaSession = MediaSessionCompat(this, packageName)
            val mediaSessionConnector = MediaSessionConnector(mediaSession)
            mediaSessionConnector.setPlayer(videoPlayer)
            mediaSession.isActive = true
        }
    }

    private fun buildMediaSource(string: String?): MediaSource? {
        if (!TextUtils.isEmpty(string)) {
            val userAgent = "Explore Exo Player"
            val uri = Uri.parse(string)
            if (uri.lastPathSegment!!.contains("mp3") || uri.lastPathSegment!!.contains("mp4")) {
//                return ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
//                    .createMediaSource(uri)
            } else if (uri.lastPathSegment!!.contains("m3u8")) {
//                return HlsMediaSource.Factory(DefaultHttpDataSourceFactory(userAgent))
//                    .createMediaSource(uri)
            } else {
                val dashChunkSourceFactory = DefaultDashChunkSource.Factory(
                    DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER)
                )
                val manifestDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
                return DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
                    .createMediaSource(uri)
            }
        }
        return null
    }

    private fun pauseExoVideo() {
        videoPlayer?.let {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && packageManager
                    .hasSystemFeature(
                        PackageManager.FEATURE_PICTURE_IN_PICTURE
                    )
            ) {
                enterPIPMode()
            } else {
                it.playWhenReady = false
            }
        }
    }

    private fun releaseExoVideo() {
        videoPlayer?.let {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.release()
            videoPlayer = null
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            ) {
                finishAndRemoveTask()
            }
        }
    }

    private fun enterPIPMode() {
        videoPlayer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            ) {
                exo_video.useController = false

                val handler = Handler(Looper.getMainLooper())
                val runnable = Runnable {
                    try {
                        @Suppress("DEPRECATION")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            this.enterPictureInPictureMode(
                                with(PictureInPictureParams.Builder()) {
                                    val width = 16
                                    val height = 9
                                    setAspectRatio(Rational(width, height))
                                    build()
                                })
                        } else {
                            //For API 24 and 25
                            this.enterPictureInPictureMode()
                        }
                    } catch (e: Exception) {
                        handleFinish()
                    }
                }
                handler.postDelayed(runnable, 300)
            }
        } ?: run {
            super.finish()
        }
    }

    private fun handleFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                //todo for what
                val activityManager =
                    applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val appTask = activityManager.appTasks
                if (appTask.size == 1) {
                    val task = appTask[0]
                    super.finish()
                    if (task.taskInfo.baseActivity == null) {
                        //MainActivity.startThisActivity(this@VideoActivity)
                    } else {
                        super.finish()
                    }
                } else {
                    super.finish()
                }
            } catch (x: IllegalStateException) {
                x.printStackTrace()
                super.finish()
            }
        } else {
            super.finish()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (isInPictureInPictureMode) {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Log.e("Ikhwan", "onPictureInPictureModeChanged landscape")
        } else {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Log.e("Ikhwan", "onPictureInPictureModeChanged potrait")
        }
        if (newConfig != null) {
            videoPlayer?.let {
                playbackPosition = it.currentPosition
                currentWindow = it.currentWindowIndex
            }
            isInPipMode = isInPictureInPictureMode
        }
    }

    /**
     * End Exo player
     * START Permission
     * */

    private fun callPiePermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            val checkPermission = CheckPermission(this)
            checkPermission.checkById(CheckPermission.REQUEST_CODE_ASK_FOREGROUND)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            CheckPermission.REQUEST_CODE_ASK_FOREGROUND -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.FOREGROUND_SERVICE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        initView()
                    }
                } else {
                    var rationale = 0
                    for (i in permissions.indices) {
                        val permission = permissions[i];
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val showRationale = shouldShowRequestPermissionRationale(permission)
                                if (!showRationale) {
                                    rationale++
                                }
                            }
                        }
                    }
                    if (rationale > 0) {
                        Toast.makeText(
                            this,
                            "Untuk melanjutkan, terima permintaan akses",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", this.packageName, null)
                        intent.data = uri
                        this.startActivity(intent)
                    }
                }
                return
            }
        }
    }

    /**
     * END Permission
     * */

    private fun saveLastTime() {
        //save with playbackPosition.toFloat()
    }

}