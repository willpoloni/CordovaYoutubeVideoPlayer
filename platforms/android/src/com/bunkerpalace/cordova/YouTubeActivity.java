package com.bunkerpalace.cordova;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import static com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import static com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import static com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import static com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import static com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import static com.google.android.youtube.player.YouTubePlayer.Provider;

import android.app.Application;
import android.content.res.Resources;

public class YouTubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener,
        PlayerStateChangeListener, View.OnClickListener, View.OnTouchListener  {

    private static final int RECOVERY_REQUEST = 1;
    private YouTubePlayerView youTubeView;
    private YouTubePlayer mPlayer;

    private String videoId;
    private String apiKey;

    private View mPlayButtonLayout;
    private TextView mPlayTimeTextView;

    private Handler mHandler = null;
    //private Handler mHandlerVisibility = null;
    private SeekBar mSeekBar;

    private boolean isPlayng = false;

    private int rIDPlayVideo;
    private int rIDPauseVideo;
    private int rIDCloseVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoId");
        apiKey = intent.getStringExtra("YouTubeApiId");

        Application app = this.getApplication();
        String package_name = app.getPackageName();
        Resources resources = app.getResources();
        /*
        setContentView(resources.getIdentifier("activity_custom_player", "layout", package_name));
         */
        setContentView(getResource("activity_custom_player", "layout"));

        rIDPlayVideo = getResource("play_video");
        rIDPauseVideo = getResource("pause_video");
        rIDCloseVideo = getResource("close_video");

        youTubeView = (YouTubePlayerView) findViewById(getResource("youtube_player_view"));
        youTubeView.initialize(apiKey, this);

		mPlayButtonLayout = findViewById(getResource("video_control"));

        mPlayTimeTextView = (TextView) findViewById(getResource("play_time"));
        mSeekBar = (SeekBar) findViewById(getResource("video_seekbar"));
        mSeekBar.setOnSeekBarChangeListener(mVideoSeekBarChangeListener);





        mHandler = new Handler();
        //mHandlerVisibility = new Handler();
    }


    private int getResource(String id) {
        return getResource(id, "id");
    }
    private int getResource(String id, String defType) {
        return getResources().getIdentifier(id, defType, getPackageName());
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
		if (null == player) return;
        mPlayer = player;

        displayCurrentTime();

        if (!wasRestored) {
            player.loadVideo(videoId);

        }

        player.setPlayerStyle(PlayerStyle.CHROMELESS);
        mPlayButtonLayout.setVisibility(View.VISIBLE);

		player.setPlayerStateChangeListener(this);
        player.setPlaybackEventListener(mPlaybackEventListener);
    }

	 PlaybackEventListener mPlaybackEventListener = new PlaybackEventListener() {
        @Override
        public void onBuffering(boolean arg0) {
        }

        @Override
        public void onPaused() {
            isPlayng = false;
            playPause();
            mHandler.removeCallbacks(runnable);
        }

        @Override
        public void onPlaying() {
            if (!isPlayng) {
                isPlayng = true;
                playPause();
                //mHandlerVisibility.postDelayed(runnableHideControl, 5000);
            }
            mHandler.postDelayed(runnable, 200);
            displayCurrentTime();
        }

        @Override
        public void onSeekTo(int arg0) {
            mHandler.postDelayed(runnable, 200);
        }

        @Override
        public void onStopped() {
            mHandler.removeCallbacks(runnable);
            //mHandlerVisibility.removeCallbacks(runnableHideControl);
        }
    };

	SeekBar.OnSeekBarChangeListener mVideoSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //long lengthPlayed = (mPlayer.getDurationMillis() * progress) / 100;

            if (fromUser)
                mPlayer.seekToMillis((int) progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

	private void playPause(){
	    if (mPlayer.isPlaying()) {
            findViewById(rIDPlayVideo).setVisibility(View.GONE);
            findViewById(rIDPauseVideo).setVisibility(View.VISIBLE);
        } else  {
            findViewById(rIDPauseVideo).setVisibility(View.GONE);
            findViewById(rIDPlayVideo).setVisibility(View.VISIBLE);
        }
    }

	 @Override
    public void onClick(View v) {
	     int currentId = v.getId();
         if (null != mPlayer)
             if (currentId == rIDPlayVideo) {
                 if (!mPlayer.isPlaying())
                     mPlayer.play();
             } else if (currentId == rIDPauseVideo) {
                 if (mPlayer.isPlaying())
                     mPlayer.pause();
             } else if (currentId == rIDCloseVideo) {

                 mPlayer.pause();
                 mHandler.removeCallbacks(runnable);
                 mPlayer.release();
                 finish();
             }

    }

    private void displayCurrentTime() {
        if (null == mPlayer) return;
        String formattedTime = formatTime(mPlayer.getDurationMillis() - mPlayer.getCurrentTimeMillis());
        mPlayTimeTextView.setText(formattedTime);

        if (mSeekBar.getMax() != mPlayer.getDurationMillis())
            mSeekBar.setMax(mPlayer.getDurationMillis());

        mSeekBar.setProgress(mPlayer.getCurrentTimeMillis());


    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "" : hours + ":") + String.format("%02d:%02d", minutes % 60, seconds % 60);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            displayCurrentTime();
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format("Error initializing YouTube player", errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onVideoEnded() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onError(
            com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) {
        updateLog("onError(): " + arg0.toString());
        finish();
    }


    @Override
    public void onAdStarted() {}

    @Override
    public void onLoaded(String arg0) {}

    @Override
    public void onLoading() {}

    @Override
    public void onVideoStarted() {
		displayCurrentTime();
	}

    private void updateLog(String text){
        Log.d("YouTubeActivity", text);
    };

    private static View findView(View view, Activity activity, String name) {
        int viewId = activity.getResources().getIdentifier(name, "id", activity.getPackageName());
        return view.findViewById(viewId);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.e("EV", "onTouch: ");
        return false;
    }
}
