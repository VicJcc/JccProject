package com.taole.myapplication.CameraSizeTest;

import android.media.MediaFormat;

/**
 * 音视频编码类
 * TODO 以后要整合到这个类里来
 * Created by taole on 15/5/18.
 */
public class TLMediaCodec {

    public static int TIMEOUT_USEC = 10000;


    public static final String MIMETYPE = MediaFormat.MIMETYPE_VIDEO_AVC; // "video/avc"; // H.264 Advanced Video Coding
    public static final int FRAME_RATE_MIN = 15;               // 15fps
    public static final int FRAME_RATE_MAX = 20;               // 20fps
    public static final int IFRAME_INTERVAL = 1;           // 1 seconds between I-frames
    public static final float BITRATE_RATIO = 2;            // width * height * ratio ,  2 同iOS同步

    public static final int VIDEO_WIDTH = 320;
    public static final int VIDEO_HEIGHT = 568;

    private static volatile long incTime = 0;
    private static Object lock = new Object();

    public static long getTickCount() {

        synchronized (lock) {
            long current = System.nanoTime();
            if (current < incTime) {
                current = incTime + 1;
            }

            incTime = current;
            return current;
        }
    }

}
