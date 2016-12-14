package com.coderming.worldcar.ui;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import com.coderming.worldcar.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MediaFragment extends Fragment implements SwitchableView {
    private static final String LOG_TAG = MediaFragment.class.getSimpleName();

    // Video play
    MyVideoView mVideoView;
    View mPlayGroup;
    AppCompatImageView mPlayVideo;
    MediaController mMediaController;

    public MediaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.play_video, container, false);

        mVideoView = (MyVideoView) rootView.findViewById(R.id.video_view) ;
        mPlayGroup = rootView.findViewById(R.id.video_play_control);
        mPlayVideo = (AppCompatImageView) rootView.findViewById(R.id.play_video_large);

        mMediaController = new MediaController(getContext()) {
            @Override
            public void hide() {
                super.hide();
            }
        };
        mMediaController.show(0);
        mMediaController.setAnchorView(mVideoView);
        Uri uri = Uri.parse("android.resource://"+getContext().getPackageName() +"/"+R.raw.sdc);

        mVideoView.setMediaController(mMediaController);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();

        if (savedInstanceState == null) {
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.v(LOG_TAG, "+++++setOnPreparedListener, savedInstanceState!=null is" + (savedInstanceState != null));
                    mp.setLooping(true);
                }
            });
        } else {
            mVideoView.start();
            mVideoView.setVisibility(View.VISIBLE);
            mPlayGroup.setVisibility(View.INVISIBLE);
        }

        mPlayVideo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.start();
                mVideoView.setVisibility(View.VISIBLE);
                mPlayGroup.setVisibility(View.INVISIBLE);
                mMediaController.show();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "+++++onResume, ");
        super.onResume();
        if (!mVideoView.isPlaying()) {
            Log.v(LOG_TAG, "+++++onResume video not play, ");
            mVideoView.requestFocus();
            mVideoView.start();
        }
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "+++++onStop, ");
        super.onStop();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
    }

    @Override
    public void onStart() {
        Log.v(LOG_TAG, "+++++onStart, ");
        super.onStart();
    }

    @Override
    public void onPause() {
        Log.v(LOG_TAG, "+++++onPause, ");
        super.onPause();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    public void switched(boolean inLargeView) {
        Log.v(LOG_TAG, "+++++switched, inLargeView="+inLargeView);
        mVideoView.start();
        if (mMediaController != null) {
            mMediaController.show();
        }
    }
}
