package com.ambarella.remotecamera;

import android.app.Activity;
import android.app.Fragment;

import com.ambarella.remotecamera.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import com.ambarella.remotecamera.fragments.widget.MediaController;
import com.ambarella.remotecamera.fragments.widget.VideoView;


public class LiveViewWithAudio extends Activity {
    private VideoView mVideoView;
    private View mBufferingIndicator;
    private MediaController mMediaController;

    private String mVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_live_view_fragment);

        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoPath = "rtsp://192.168.42.1/live";

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction) && intentAction.equals(Intent.ACTION_VIEW)) {
            mVideoPath = intent.getDataString();
        }

        mBufferingIndicator = findViewById(R.id.buffering_indicator);
        mMediaController = new MediaController(this);

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setMediaBufferingIndicator(mBufferingIndicator);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.requestFocus();
        mVideoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        IjkMediaPlayer.native_profileEnd();
    }
}
/*Fragment {
    private VideoView mVideoView;
    private View mBufferingIndicator;
    private MediaController mMediaController;
    private String mVideoPath;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.audio_live_view_fragment, container, false);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mVideoPath = "rtsp://192.168.42.1/live";
        mBufferingIndicator =  view.findViewById(R.id.buffering_indicator);
        mMediaController = new MediaController(getActivity());

        mVideoView = (VideoView) view.findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setMediaBufferingIndicator(mBufferingIndicator);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.requestFocus();
        mVideoView.start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        IjkMediaPlayer.native_profileEnd();
    }
}
*/