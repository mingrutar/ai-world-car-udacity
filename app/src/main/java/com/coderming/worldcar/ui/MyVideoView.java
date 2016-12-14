package com.coderming.worldcar.ui;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by linna on 12/4/2016.
 */
public class MyVideoView extends VideoView {
    private static final String LOG_TAG = MyVideoView.class.getSimpleName();

    private static final  int DEAFULT_VIDEO_WIDTH = 1280;
    private static final  int DEAFULT_VIDEO_HEIGHT = 720;

    private int mVideoWidth;
    private int mVideoHeight;

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyVideoView(Context context) {
        super(context);
    }
    @Override
    public void setVideoURI(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this.getContext(), uri);
        String str = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        mVideoWidth = (str != null)? Integer.parseInt(str) : DEAFULT_VIDEO_WIDTH;
        str = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        mVideoHeight = (str != null) ? Integer.parseInt(str) : DEAFULT_VIDEO_HEIGHT;
        super.setVideoURI(uri);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v(LOG_TAG, "onMeasure");
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                // Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                // Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else {
                // Log.i("@@@", "aspect ratio is correct: " +
                // width+"/"+height+"="+
                // mVideoWidth+"/"+mVideoHeight);
            }
        }
        Log.v(LOG_TAG, "setting size: w=" + width + ", h=" + height);
        setMeasuredDimension(width, height);
    }
}
