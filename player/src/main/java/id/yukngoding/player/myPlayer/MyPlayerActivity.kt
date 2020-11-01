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
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import id.yukngoding.player.CheckPermission
import id.yukngoding.player.R
import kotlinx.android.synthetic.main.activity_my_player.*
import kotlinx.android.synthetic.main.my_playback_control.*

class MyPlayerActivity : AppCompatActivity(), MyPlayerContract.View, View.OnClickListener {
    private val BANDWIDTH_METER = DefaultBandwidthMeter()

    private var videoPresenter: MyPlayerPresenter? = null

    var courseId: Int = -1
    var courseLessonId: Int = -1
    var type: Int = -1

    //var mediaDao: MediaDao? = null
    //var contentEmbed: String? = null
    var contentEmbed = "https://ikhwankoto.com/_image/tes_video.mov"

    var orientationState: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    //start youtube
    //var initializedYouTubePlayer: YouTubePlayer? = null
    //var youtubeLastTime = 0f
    //end youtube

    //start exo
    var playbackPosition: Long = 0L
    var currentWindow: Int = 0

    //start exovideo
    private var isInPipMode = false
    private var videoPlayer: SimpleExoPlayer? = null
    //end exovideo
    //end exo

    private fun initBeforeCreateContent() {
        requestFullscreen()
    }

    private fun requestFullscreen() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

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

    fun initCreate() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        videoPresenter = MyPlayerPresenter(this, this)

        initView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("Ikhwan", "onConfigurationChanged")
        initView()
    }

    fun initView() {
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

            if (videoPlayer != null) {
                Log.e("Ikhwan", "initView videoPlayer != null")
                videoPlayer!!.setPlayWhenReady(true)
            } else {
                Log.e("Ikhwan", "initView videoPlayer != null else")
                //TODO FLOW 1
                setContent("")
//                if (mediaDao == null) {
//                    var contentId = courseId
//                    if (type == ContentDictionary.TYPE_DB_LESSON) {
//                        contentId = courseLessonId
//                    }
//                    videoPresenter!!.getContentById(contentId, type)
//                } else {
//                    setContent(mediaDao)
//                }
                //presenter
                //toView setContent
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.exo_screen -> {
                setOrientation()
            }
            R.id.exo_replay -> {
                //- 10 second
                playbackPosition = videoPlayer!!.getCurrentPosition() - 10000
                if (playbackPosition < 0L) {
                    playbackPosition = 0L
                }
                currentWindow = videoPlayer!!.getCurrentWindowIndex()
                setVideoPosition()
            }
            R.id.exo_forward -> {
                //+10 second
                playbackPosition = videoPlayer!!.getCurrentPosition() + 10000
                if (playbackPosition > videoPlayer!!.duration) {
                    playbackPosition = 0L
                }
                currentWindow = videoPlayer!!.getCurrentWindowIndex()
                setVideoPosition()
            }
        }
    }

    /**
     * Start Data
     * */

    override fun setContent(dao: String) {
        if (dao != null) {
            //TODO set url at this
            //contentEmbed = "https://ikhwankoto.com/_image/tes_video.mov"
            if (TextUtils.isEmpty(contentEmbed)) {
                Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show()
                super.finish()
            }

//            if (youtubeLastTime == 0f) {
//                youtubeLastTime = mediaDao!!.lastTime
//            }

//            if (playbackPosition == 0L) {
//                playbackPosition = mediaDao!!.lastTime.toLong()
//            }

            initVideo()
        } else if (!TextUtils.isEmpty(contentEmbed)) {
            setContentByUrl(contentEmbed)
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            super.finish()
        }
    }

    override fun setContentByUrl(contentUrl: String?) {
        //contentEmbed = contentUrl
        initVideo()
    }

    private fun initVideo() {
        if (!TextUtils.isEmpty(contentEmbed) && !contentEmbed.equals("")) {
            if ((contentEmbed!!.contains("youtube") || contentEmbed!!.contains("youtu.be"))
                && !contentEmbed!!.contains(
                    ".mp4"
                )
            ) {
                //initYoutube()
            } else {
                checkExoVideo()
            }
        }
    }

    /**
     * End Data
     * Start Lifecycle
     * */

    override fun onBackPressed() {
        if (orientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setOrientation()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //handleFinish()
            } else {
                super.finish()
            }
            super.finish()
        }
    }

    override fun finish() {
        if (orientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setOrientation()
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
        try {
            super.onResume()
            if (videoPlayer != null) {
                if (playbackPosition > 0L && !isInPipMode) {
                    portraitMode()
                    videoPlayer!!.seekTo(currentWindow, playbackPosition)
                }
                val handler = Handler()
                val runnable = Runnable {
                    orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    setRequestedOrientation(orientationState);
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
        try {
            super.onPause()
            if (Util.SDK_INT <= Build.VERSION_CODES.M) {
                pause()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onStop() {
        try {
            super.onStop()
            if (Util.SDK_INT > Build.VERSION_CODES.M) {
                pause()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
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
        releaseYoutube()
        releaseExoVideo()
        super.onDestroy()
    }

    /**
     * End Lifecycle
     * Start Video
     * */

    private fun landscapeMode() {
        orientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val handler = Handler()
        val runnable = Runnable {
            ll_parent.post {
                val w = ll_parent.width
                val h = ll_parent.height
                if (w > 0 && h > 0) {
//                    ll_exo_video.setLayoutParams(RelativeLayout.LayoutParams(w, h))
                    ll_exo_video.setLayoutParams(ConstraintLayout.LayoutParams(w, h))
                }
            }
        }
        handler.postDelayed(runnable, 300)
    }

    private fun portraitMode() {
        orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val handler = Handler()
        val runnable = Runnable {
            ll_parent.post {
                val w = ll_parent.width /*- UnitHelper.intToDp(32)*/
                val h = (w * 9 / 16).toInt()
                if (w > 0 && h > 0) {
//                    ll_exo_video.setLayoutParams(RelativeLayout.LayoutParams(w, h))
                    ll_exo_video.setLayoutParams(ConstraintLayout.LayoutParams(w, h))
                }
            }
        }
        handler.postDelayed(runnable, 300)
    }

    private fun pause() {
        pauseYoutube()
        pauseExoVideo()
        //saveLastTime()
    }

    /**
     * End Video
     * Start Exo Player
     * */

    private fun setVideoPosition() {
        videoPlayer!!.setPlayWhenReady(true)
        videoPlayer!!.seekTo(currentWindow, playbackPosition)
    }

    private fun checkExoVideo() {
        if (contentEmbed!!.contains("wistia.")) {
            videoPresenter!!.getWistiaMediaById(contentEmbed)
        }
        initExoVideo()
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

        //yp_player.visibility = View.GONE
        val handler = Handler()
        val runnable = Runnable {
            ll_exo_video.visibility = View.VISIBLE
        }
        handler.postDelayed(runnable, 300)

        exo_video.visibility = View.VISIBLE
        if (videoPlayer == null) {
            Log.e("Ikhwan", "initExoVideo videoplayer ==null")
            videoPlayer = ExoPlayerFactory.newSimpleInstance(
                this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(),
                DefaultLoadControl()
            )

            exo_video.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            exo_video.setControllerHideOnTouch(false)
            exo_video.controllerShowTimeoutMs = 2000
            exo_video.setPlayer(videoPlayer)
            videoPlayer!!.setPlayWhenReady(true)
            videoPlayer!!.seekTo(currentWindow, playbackPosition)
        } else {
            Log.e("Ikhwan", "initExoVideo videoplayer else")
            videoPlayer!!.setPlayWhenReady(true)
            videoPlayer!!.seekTo(currentWindow, playbackPosition)
        }
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build();
        videoPlayer!!.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true);

        var isValid = false
        if (!TextUtils.isEmpty(contentEmbed)) {
            Log.e("Ikhwan", "initExoVideo contentEmbed isNotEmpty")

//            val mediaSource = buildMediaSource(contentEmbed)
//            if (mediaSource != null) {
//                isValid = true
//                videoPlayer!!.addMediaSource(mediaSource)
//                videoPlayer!!.prepare()
//            }

            val mediaItemVideo =
                MediaItem.fromUri("https://ikhwankoto.com/_image/tes_video.mov")
            videoPlayer!!.setMediaItem(mediaItemVideo)

            //videoPlayer!!.playWhenReady
            videoPlayer!!.prepare()

            val mediaSession = MediaSessionCompat(this, packageName)
            val mediaSessionConnector = MediaSessionConnector(mediaSession)
            mediaSessionConnector.setPlayer(videoPlayer)
            mediaSession.isActive = true
        }

        if (isValid == false) {
            Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show()
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
        return null;
    }

    private fun pauseExoVideo() {
        if (videoPlayer != null) {
            playbackPosition = videoPlayer!!.getCurrentPosition()
            currentWindow = videoPlayer!!.getCurrentWindowIndex()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && packageManager
                    .hasSystemFeature(
                        PackageManager.FEATURE_PICTURE_IN_PICTURE
                    )
            ) {
                enterPIPMode()
            } else {
                videoPlayer!!.setPlayWhenReady(false);
            }
        }
    }

    private fun releaseExoVideo() {
        if (videoPlayer != null) {
            playbackPosition = videoPlayer!!.getCurrentPosition()
            currentWindow = videoPlayer!!.getCurrentWindowIndex()
            videoPlayer!!.release()
            videoPlayer = null
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            ) {
                finishAndRemoveTask()
            }
        }
    }

    fun enterPIPMode() {
        if (videoPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                    exo_video.useController = false

                    val handler = Handler()
                    val runnable = Runnable {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                this.enterPictureInPictureMode(
                                    with(PictureInPictureParams.Builder()) {
                                        val width = 16
                                        val height = 9
                                        setAspectRatio(Rational(width, height))
                                        build()
                                    })
                            } else {
                                this.enterPictureInPictureMode()
                            }
                        } catch (e: Exception) {
                            handleFinish()
                        }
                    }
                    handler.postDelayed(runnable, 300)


                }
            }
        } else {
            super.finish()
        }
    }

    private fun handleFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val activityManager =
                    applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val appTask = activityManager.getAppTasks()
                if (appTask.size == 1) {
                    val task = appTask.get(0)
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

    private fun setOrientation() {
        playbackPosition = videoPlayer!!.getCurrentPosition()
        currentWindow = videoPlayer!!.getCurrentWindowIndex()
        if (videoPlayer != null) {
            videoPlayer!!.setPlayWhenReady(false);
        }
        if (orientationState == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        setRequestedOrientation(orientationState);
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (isInPictureInPictureMode) {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            orientationState = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (newConfig != null) {
            playbackPosition = videoPlayer!!.getCurrentPosition()
            currentWindow = videoPlayer!!.getCurrentWindowIndex()
            isInPipMode = isInPictureInPictureMode
        }
    }

    /**
     * End Exo player
     * Start Youtube player
     * */
    //    private fun initYoutube() {
//        yp_player.visibility = View.VISIBLE
//        ll_exo_video.visibility = View.GONE
//        if (mediaDao != null) {
//            if (!TextUtils.isEmpty(contentEmbed) && !contentEmbed.equals("")) {
//                var youtubeUri = contentEmbed
//                if (!TextUtils.isEmpty(youtubeUri)) {
//                    if (youtubeUri!!.contains("is_watch")) {
//                        if (youtubeUri.contains("?v=")) {
//                            youtubeUri = youtubeUri.split("?v=")[1]
//                        }
//                        if (youtubeUri.contains("&v=")) {
//                            youtubeUri = youtubeUri.split("&v=")[1]
//                        }
//                    } else if (youtubeUri!!.contains("embed")) {
//                        val x = youtubeUri.split("/")
//                        youtubeUri = x.get(x.size - 1)
//                    }
//
//                    if (initializedYouTubePlayer == null) {
//                        yp_player.initialize({ youTubePlayer ->
//                            initializedYouTubePlayer = youTubePlayer
//                            initializedYouTubePlayer!!.addListener(object : AbstractYouTubePlayerListener() {
//                                override fun onReady() {
//                                    initializedYouTubePlayer!!.loadVideo(youtubeUri, youtubeLastTime)
//                                    initializedYouTubePlayer!!.play()
//                                }
//
//                                override fun onCurrentSecond(second: Float) {
//                                    youtubeLastTime = second
//                                }
//
//                                override fun onStateChange(state: Int) {
//                                    if (state == PlayerConstants.PlayerState.ENDED) {
//                                        youtubeLastTime = 0f
//                                    }
//                                    super.onStateChange(state)
//                                }
//
//                                override fun onError(error: Int) {
//
//                                }
//
//                            })
//                        }, true)
//                    } else {
//                        initializedYouTubePlayer!!.play()
//                    }
//
//                    val uiController = yp_player.getPlayerUIController()
//                    uiController.showYouTubeButton(false)
//                    uiController.setVideoTitle("")
//                    uiController.showPlayPauseButton(true)
//                    uiController.setCustomFullScreenButtonClickListener {
//                        setOrientation()
//                    }
//
//                }
//            }
//        }
//    }

    private fun pauseYoutube() {
//        if (initializedYouTubePlayer != null) {
//            initializedYouTubePlayer!!.pause()
//        }
    }

    private fun releaseYoutube() {
//        if (yp_player != null) {
//            yp_player!!.release()
//        }
    }

    /**
     * End Youtube player
     * */

    override fun resultWistiaMedia(url: String?) {}

    override fun resultCheckIsFav(isFav: Boolean) {}

    override fun resultSetFav(type: Int, boolean: Boolean) {}

    /**
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
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                        val uri = Uri.fromParts("package", this.getPackageName(), null)
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

}