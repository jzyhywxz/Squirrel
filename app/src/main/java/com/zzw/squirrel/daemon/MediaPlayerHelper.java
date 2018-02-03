package com.zzw.squirrel.daemon;

import android.content.Context;
import android.media.MediaPlayer;

import com.zzw.squirrel.R;
import com.zzw.squirrel.util.LimitedLog;

/**
 * Created by zzw on 2018/2/2.
 */

public class MediaPlayerHelper {
    private static final String CLASS_NAME = MediaPlayerHelper.class.getSimpleName();
    private Context context;
    private MediaPlayer mediaPlayer;

    public MediaPlayerHelper(Context context) {
        this.context = context;
    }

    public synchronized void start() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.silent);
            mediaPlayer.setLooping(true);
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();

            LimitedLog.d(CLASS_NAME + "#start");
        }
    }

    public synchronized void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();

                LimitedLog.d(CLASS_NAME + "#stop");
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
