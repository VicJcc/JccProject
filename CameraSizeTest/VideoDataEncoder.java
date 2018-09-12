package com.taole.myapplication.CameraSizeTest;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;

import com.taole.myapplication.MyApplication;
import com.taole.myapplication.Utils.ArrayUtils;
import com.taole.myapplication.Utils.TLLogger;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by taole on 16/3/2.
 */
public class VideoDataEncoder {

    private final String TAG = "VideoDataEncoder";
    public static int TIMEOUT_USEC = 10000;

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

    //TODO 如果以后做扩展性， 要改
    private int currentIndex = -1;

    protected MediaCodec mMediaCodec;
    protected MediaCodec.BufferInfo mBufferInfo;

    private final boolean bWriteVideoSample = true;
    private FileOutputStream mTestOutputStream;

    private int mHeight;
    private int mWidth;



    public VideoDataEncoder(int width, int height) {
        mHeight = height;
        mWidth = width;
        mBufferInfo = new MediaCodec.BufferInfo();
//        initAVCEncoderCaps();
        if (bWriteVideoSample) {
            String path = MyApplication.getIns().getApplicationContext().getExternalFilesDir("files").toString() + "/haodiao" + System.currentTimeMillis() + ".h264";
            try {
                mTestOutputStream = new FileOutputStream(path, false);
            }
            catch (Exception exp) {
                mTestOutputStream = null;
                TLLogger.trace(TAG, "video encode file error: " + exp.toString());
            }
        }

        MediaFormat format = MediaFormat.createVideoFormat(TLMediaCodec.MIMETYPE, mHeight, mWidth);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                19);
        format.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 75);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        try {
            List localList = getEndoderInfos(TLMediaCodec.MIMETYPE);
            int mColorFormat = 21;
            int i = 0;
            for (int j = 0; j < localList.size(); j++) {
                MediaCodecInfo.CodecCapabilities localCodecCapabilities1 = getCodecCapabilities((MediaCodecInfo) localList.get(j), TLMediaCodec.MIMETYPE);
                if (ArrayUtils.contains(localCodecCapabilities1.colorFormats, 21)) {
                    mColorFormat = 21;
                    i = j;
                    break;
                }
                if (ArrayUtils.contains(localCodecCapabilities1.colorFormats, 19)) {
                    mColorFormat = 19;
                    i = j;
                    break;
                }
            }
            format.setInteger("color-format", mColorFormat);
            int k = 30;
            if (TLMediaCodec.FRAME_RATE_MAX * k > 255) {
                k = 255 / TLMediaCodec.FRAME_RATE_MAX;
            }

            if (Build.VERSION.SDK_INT < 19)
                format.setInteger("i-frame-interval", k);
            else {
                format.setInteger("i-frame-interval", k);
            }

            MediaCodecInfo.CodecCapabilities localCodecCapabilities2 = getCodecCapabilities((MediaCodecInfo) localList.get(i), TLMediaCodec.MIMETYPE);

            int m = 16;
            for (int n = 0; n < localCodecCapabilities2.profileLevels.length; n++) {
                switch (localCodecCapabilities2.profileLevels[n].profile) {
                    case 1:
                        format.setInteger("profile", 1);
                        if (m < localCodecCapabilities2.profileLevels[n].level) {
                            m = localCodecCapabilities2.profileLevels[n].level;
                        }
                        format.setInteger("level", m);
                }

            }

//            if (TLMediaCodec.FRAME_RATE_MAX > 0)
//                this.mFrameInverval = (1000000 / TLMediaCodec.FRAME_RATE_MAX);
//            else {
//                this.mFrameInverval = 40000;
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
        data = new byte[width*height*3/2];

        try {
            mMediaCodec = MediaCodec.createEncoderByType(TLMediaCodec.MIMETYPE);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        } catch (Exception exp) {

            exp.printStackTrace();
            TLLogger.trace("TLTEST fuck", "fuck exp: " + exp.toString());
        }

    }


    public void encode(byte[] data)
    {
        if (null != mMediaCodec) {
            mediaCodecEncode(data);
        }
    }

    private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height)
    {
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width*height);
        System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height, width*height/4);
        System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4, width*height/4);
    }

    private static void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height)
    {
        if (yuv420sp == null ||yuv420 == null)
            return;
        int framesize = width*height;
        int i = 0, j = 0;
//copy y
        for (i = 0; i < framesize; i++)
        {
            yuv420[i] = yuv420sp[i];
        }
        i = 0;
        for (j = 0; j < framesize/2; j+=2)
        {
            yuv420[i + framesize*5/4] = yuv420sp[j+framesize];
            i++;
        }
        i = 0;
        for(j = 1; j < framesize/2;j+=2)
        {
            yuv420[i+framesize] = yuv420sp[j+framesize];
            i++;
        }
    }

    public static void rotateNV21(byte[] input, byte[] output, int width, int height, int rotation) {
        boolean swap = (rotation == 90 || rotation == 270);
        boolean yflip = (rotation == 90 || rotation == 180);
        boolean xflip = (rotation == 270 || rotation == 180);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int xo = x, yo = y;
                int w = width, h = height;
                int xi = xo, yi = yo;
                if (swap) {
                    xi = w * yo / h;
                    yi = h * xo / w;
                }
                if (yflip) {
                    yi = h - yi - 1;
                }
                if (xflip) {
                    xi = w - xi - 1;
                }
                output[w * yo + xo] = input[w * yi + xi];
                int fs = w * h;
                int qs = (fs >> 2);
                xi = (xi >> 1);
                yi = (yi >> 1);
                xo = (xo >> 1);
                yo = (yo >> 1);
                w = (w >> 1);
                h = (h >> 1);
                // adjust for interleave here
                int ui = fs + (w * yi + xi) * 2;
                int uo = fs + (w * yo + xo) * 2;
                // and here
                int vi = ui + 1;
                int vo = uo + 1;
                output[uo] = input[ui];
                output[vo] = input[vi];
            }
        }
    }

    /**
     * 顺时针90度
     * @param src 原始数据
     * @param des 保存数据
     * @param width 原始数据宽
     * @param height 原始数据高
     */
    private void jccRotateYUV420SP_90(byte[] src,byte[] des,int width,int height){
        if (src == null ||des == null)
            return;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width * (height - j - 1) + i];
                k++;
            }
        }

        // 旋转UV
        for(int i=0;i<width;i+=2) {
            for(int j=0;j<height/2;j++)
            {
                des[k] = src[width * height + width * (height / 2 - j - 1) + i];
                des[k+1]=src[width * height + width * (height / 2 - j - 1) + i + 1];
                k+=2;
            }
        }
    }
    // 顺时针180度
    private void jccRotateYUV420SP_180(byte[] src,byte[] des,int width,int height){
        if (src == null ||des == null)
            return;
        //旋转Y
        int k = 0;
        for(int i=0;i<width*height;i++) {
            des[k] = src[width * height - i - 1];
            k++;
        }

        // 旋转UV
        for(int i=0;i<width*height/2;i+=2) {
            des[k] = src[width * height + width * height / 2 - i - 2];
            des[k + 1] = src[width * height + width * height / 2 - i - 2 + 1];
            k+=2;
        }
    }
    // 顺时针270度
    private void jccRotateYUV420SP_270(byte[] src,byte[] des,int width,int height){
        if (src == null ||des == null)
            return;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width * (j + 1) - (i + 1)];
                k++;
            }
        }

        // 旋转UV
        for(int i=0;i<width;i+=2) {
            for(int j=0;j<height/2;j++)
            {
                des[k] = src[width * height + width * (j + 1) - (i + 2)];
                des[k+1]=src[width * height + width * (j + 1) - (i + 2) + 1];
                k+=2;
            }
        }
    }

    private void cutData(byte[] src,byte[] des,int width,int height){





    }

    private byte[] data = null;

    private void mediaCodecEncode(byte[] yv12bytes) {

//        swapYV12toI420(yv12bytes, data, mWidth, mHeight);
        byte[] newData = new byte[data.length];
//        rotateNV21(yv12bytes, newData, mWidth, mHeight, 90);
//        rotateYUV240SP(yv12bytes, newData, mWidth, mHeight);
        jccRotateYUV420SP_90(yv12bytes,newData,mWidth, mHeight);
        YUV420SP2YUV420(newData, data, mHeight, mWidth);
//        System.arraycopy(newData, 0, data, 0,newData.length);
        cutData(newData, data, mHeight, mWidth);

        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();

        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputBufferIndex < 0) {
            return;
        }

        ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
        inputBuffer.clear();

        int capacity = inputBuffer.capacity();
        int validLength = data.length;


        TLLogger.trace("JCCTLTEST capacity", capacity+"");
        TLLogger.trace("JCCTLTEST validLength", validLength+"");


        if (validLength > capacity) {

            byte[] currentData = new byte[capacity];
            byte[] nextData = new byte[validLength - capacity];

            System.arraycopy(data, 0,  currentData, 0, currentData.length);
            validLength = capacity;

            inputBuffer.put(currentData);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, validLength, getTickCount() / 1000l, 0);
            drainEncoder(false, outputBuffers);

            System.arraycopy(data, capacity, nextData, 0, nextData.length);
            encode(nextData);
        }
        else {

            inputBuffer.put(data);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, validLength, getTickCount() / 1000l, 0);
            drainEncoder(false, outputBuffers);
        }
    }


    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     * <p>
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     */
    //Notice 部分机型无法通过 signalEndOfInputStream 通知停止
    private long mWorkaroundInterval = -1;

    private byte[] spsByte = null;
    private byte[] ppsByte = null;
    private static final String ppsKey = "csd-1";
    private static final String spsKey = "csd-0";

    public void drainEncoder(boolean endOfStream, ByteBuffer[] encoderOutputBuffers) {
        TLLogger.trace(TAG, "drainEncoder(" + endOfStream + ")");

        if (null == mMediaCodec || null == encoderOutputBuffers) {
            TLLogger.trace(TAG, "drainEncoder encoder null");
            return;
        }

        if (endOfStream) {
            TLLogger.trace(TAG, "sending EOS to encoder");
            mWorkaroundInterval = 0;
            mMediaCodec.signalEndOfInputStream();
        }

        while (true) {

            //Notice workaround code for signalEndOfInputStream Issue
            if (mWorkaroundInterval >= 0) {
                long current = System.currentTimeMillis();
                if (0 == mWorkaroundInterval) {
                    mWorkaroundInterval = current;
                }
                else {
                    if (current - mWorkaroundInterval > 500) {
                        mWorkaroundInterval = -1;
                        break;
                    }
                }
            }

            int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TLMediaCodec.TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    mWorkaroundInterval = -1;
                    break;      // out of while
                } else {
                    TLLogger.trace(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mMediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once

                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                TLLogger.trace(TAG, "encoder output format changed: " + newFormat);

                ByteBuffer pps = newFormat.getByteBuffer(ppsKey);
                ByteBuffer sps = newFormat.getByteBuffer(spsKey);
                spsByte = sps.array();
                ppsByte = pps.array();

            } else if (encoderStatus < 0) {
                TLLogger.trace(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    TLLogger.trace(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {

                    int size = encodedData.remaining();
                    TLLogger.trace(TAG+"JCC", "fuck bufferInfo size: " + mBufferInfo.size);
                    TLLogger.trace(TAG, "buffer remaining: " + size);
                    //prepare send data

                    if (mBufferInfo.size < size) {
                        size = mBufferInfo.size;
                    }
                    byte[] outData = new byte[size];
                    encodedData.get(outData);

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + size);

                    ////////Send rtp
                    ByteArrayOutputStream sendBuffer = new ByteArrayOutputStream();

                    boolean keyFrame = false;
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) == 1) {
                        keyFrame = true;
                        if (null != spsByte) {
                            sendBuffer.write(spsByte, 0, spsByte.length);
                            if (null != ppsByte) {
                                sendBuffer.write(ppsByte, 0, ppsByte.length);
                            }
                        }
                    }

                    sendBuffer.write(outData, 0, outData.length);
                    byte[] sendData = sendBuffer.toByteArray();
//                    TLJobManager.addJobInBackground(new TLVideoSendRtpJob(sendData, TLConst.DEFAULT_AV_CHANNEL_INDEX, keyFrame));
                    TLLogger.trace("fuck TLTEST TLVideoSendRtpJob", sendData.length+"");
                    //////////Send end

                    ////////test write
                    if (bWriteVideoSample) {
                        try {
                            if (mBufferInfo.offset != 0)
                            {
                                mTestOutputStream.write(sendData, mBufferInfo.offset, sendData.length
                                        - mBufferInfo.offset);
                            }
                            else
                            {
                                mTestOutputStream.write(sendData, 0, sendData.length);
                            }
                            mTestOutputStream.flush();
                        }
                        catch (Exception exp) {
                            exp.printStackTrace();
                        }
                    }
                }

                mMediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        TLLogger.trace(TAG, "reached end of stream unexpectedly");
                    } else {
                        TLLogger.trace(TAG, "end of stream reached");
                    }
                    mWorkaroundInterval = -1;
                    break;      // out of while
                }
            }
        }
    }







    public void release() {

        if (null != mMediaCodec) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }




















    static AVCCaps gAVCEncoderCaps;
    static class AVCCaps
    {
        public int width = 0;
        public int height = 0;
        public int profile = 3;
    }
    @SuppressLint({"NewApi"})
        public static List<MediaCodecInfo> getDecoderInfos(String paramString) {
        ArrayList localArrayList = new ArrayList();
        int i = MediaCodecList.getCodecCount();
        for (int j = 0; j < i; j++) {
            MediaCodecInfo localMediaCodecInfo = MediaCodecList.getCodecInfoAt(j);

            if (!localMediaCodecInfo.isEncoder()) {
                if ((!localMediaCodecInfo.getName().contains(".sw.")) && (!localMediaCodecInfo.getName().contains(".SW."))) {
                    if ((!localMediaCodecInfo.getName().contains("google")) && (!localMediaCodecInfo.getName().contains("Google")) && (!localMediaCodecInfo.getName().contains("GOOGLE"))) {
                        String[] arrayOfString = localMediaCodecInfo.getSupportedTypes();

                        for (int k = 0; k < arrayOfString.length; k++)
                            if (arrayOfString[k].equalsIgnoreCase(paramString))
                                localArrayList.add(localMediaCodecInfo);
                    }
                }
            }
        }
        return localArrayList;
    }
    public static MediaCodecInfo.CodecCapabilities getCodecCapabilities(MediaCodecInfo paramMediaCodecInfo, String paramString)
    {
        MediaCodecInfo.CodecCapabilities localCodecCapabilities = paramMediaCodecInfo.getCapabilitiesForType(paramString);
        return localCodecCapabilities;
    }

    private static void initAVCEncoderCaps()
    {
        List localList = getDecoderInfos(TLMediaCodec.MIMETYPE);
        MediaCodecInfo.CodecCapabilities localCodecCapabilities = getCodecCapabilities((MediaCodecInfo)localList.get(0), TLMediaCodec.MIMETYPE);

        gAVCEncoderCaps = new AVCCaps();

        for (int i = 0; i < localCodecCapabilities.profileLevels.length; i++)
        switch (localCodecCapabilities.profileLevels[i].profile)
        {
            case 1:
            gAVCEncoderCaps.profile = 3;
            setLevel(localCodecCapabilities.profileLevels[i], gAVCEncoderCaps);
        }
    }

    private static void setLevel(MediaCodecInfo.CodecProfileLevel paramCodecProfileLevel, AVCCaps paramAVCCaps) {
        switch (paramCodecProfileLevel.level) {
            case 1:
                if ((paramAVCCaps.width < 176) || (paramAVCCaps.height < 144)) {
                    paramAVCCaps.width = 176;
                    paramAVCCaps.height = 144;
                }
                break;
            case 4:
                if ((paramAVCCaps.width < 352) || (paramAVCCaps.height < 288)) {
                    paramAVCCaps.width = 352;
                    paramAVCCaps.height = 288;
                }
                break;
            case 8:
                if ((paramAVCCaps.width < 352) || (paramAVCCaps.height < 288)) {
                    paramAVCCaps.width = 352;
                    paramAVCCaps.height = 288;
                }
                break;
            case 16:
                if ((paramAVCCaps.width < 352) || (paramAVCCaps.height < 288)) {
                    paramAVCCaps.width = 352;
                    paramAVCCaps.height = 288;
                }
                break;
            case 2:
                if ((paramAVCCaps.width < 352) || (paramAVCCaps.height < 288)) {
                    paramAVCCaps.width = 352;
                    paramAVCCaps.height = 288;
                }
                break;
            case 32:
                if ((paramAVCCaps.width < 352) || (paramAVCCaps.height < 288)) {
                    paramAVCCaps.width = 352;
                    paramAVCCaps.height = 288;
                }
                break;
            case 64:
                if ((paramAVCCaps.width < 352) || (paramAVCCaps.height < 576)) {
                    paramAVCCaps.width = 352;
                    paramAVCCaps.height = 576;
                }
                break;
            case 128:
                if ((paramAVCCaps.width < 720) || (paramAVCCaps.height < 576)) {
                    paramAVCCaps.width = 720;
                    paramAVCCaps.height = 576;
                }
                break;
            case 256:
                if ((paramAVCCaps.width < 720) || (paramAVCCaps.height < 576)) {
                    paramAVCCaps.width = 720;
                    paramAVCCaps.height = 576;
                }
                break;
            case 512:
                if ((paramAVCCaps.width < 1280) || (paramAVCCaps.height < 720)) {
                    paramAVCCaps.width = 1280;
                    paramAVCCaps.height = 720;
                }
                break;
            case 1024:
                if ((paramAVCCaps.width < 1280) || (paramAVCCaps.height < 1024)) {
                    paramAVCCaps.width = 1280;
                    paramAVCCaps.height = 1024;
                }
                break;
            case 2048:
                if ((paramAVCCaps.width < 2048) || (paramAVCCaps.height < 1024)) {
                    paramAVCCaps.width = 2048;
                    paramAVCCaps.height = 1024;
                }
                break;
            case 4096:
                if ((paramAVCCaps.width < 2048) || (paramAVCCaps.height < 1024)) {
                    paramAVCCaps.width = 2048;
                    paramAVCCaps.height = 1024;
                }
                break;
            case 8192:
                if ((paramAVCCaps.width < 2048) || (paramAVCCaps.height < 1088)) {
                    paramAVCCaps.width = 2048;
                    paramAVCCaps.height = 1088;
                }
                break;
            case 16384:
                if ((paramAVCCaps.width < 3680) || (paramAVCCaps.height < 1536)) {
                    paramAVCCaps.width = 3680;
                    paramAVCCaps.height = 1536;
                }
                break;
            case 32768:
                if ((paramAVCCaps.width < 4096) || (paramAVCCaps.height < 2304)) {
                    paramAVCCaps.width = 4096;
                    paramAVCCaps.height = 2304;
                }
                break;
        }
    }


    public static List<MediaCodecInfo> getEndoderInfos(String paramString) {
        ArrayList localArrayList = new ArrayList();
        int i = MediaCodecList.getCodecCount();
        for (int j = 0; j < i; j++) {
            MediaCodecInfo localMediaCodecInfo = MediaCodecList.getCodecInfoAt(j);

            if (localMediaCodecInfo.isEncoder()) {
                if ((!localMediaCodecInfo.getName().contains(".sw.")) && (!localMediaCodecInfo.getName().contains(".SW."))) {
                    if ((!localMediaCodecInfo.getName().contains("google")) && (!localMediaCodecInfo.getName().contains("Google")) && (!localMediaCodecInfo.getName().contains("GOOGLE"))) {
                        String[] arrayOfString = localMediaCodecInfo.getSupportedTypes();

                        for (int k = 0; k < arrayOfString.length; k++)
                            if (arrayOfString[k].equalsIgnoreCase(paramString))
                                localArrayList.add(localMediaCodecInfo);
                    }
                }
            }
        }
        return localArrayList;
    }

//    void createEncCodec() {
//        if (this.mCodec == null)
//            try {
//                List localList = getEndoderInfos(TLMediaCodec.MIMETYPE);
//                mColorFormat = 21;
//                int i = 0;
//                this.mCodec = new AndroidCodec();
//                MediaFormat localMediaFormat = MediaFormat.createVideoFormat(this.mMime, this.mWidth, this.mHeight);
//                localMediaFormat.setInteger("color-format", this.mColorFormat);
//                localMediaFormat.setInteger("frame-rate", this.mFrameRate);
//                localMediaFormat.setInteger("bitrate", this.mBitRate);
//                int k = 30;
//                if (this.mFrameRate * k > 255) {
//                    k = 255 / this.mFrameRate;
//                }
//
//                if (Build.VERSION.SDK_INT < 19)
//                    localMediaFormat.setInteger("i-frame-interval", k);
//                else {
//                    localMediaFormat.setInteger("i-frame-interval", k);
//                }
//
//                MediaCodecInfo.CodecCapabilities localCodecCapabilities2 = AndroidCodec.getCodecCapabilities((MediaCodecInfo) localList.get(i), this.mMime);
//
//                int m = 16;
//                for (int n = 0; n < localCodecCapabilities2.profileLevels.length; n++) {
//                    switch (localCodecCapabilities2.profileLevels[n].profile) {
//                        case 1:
//                            localMediaFormat.setInteger("profile", 1);
//                            if (m < localCodecCapabilities2.profileLevels[n].level) {
//                                m = localCodecCapabilities2.profileLevels[n].level;
//                            }
//                            localMediaFormat.setInteger("level", m);
//                    }
//
//                }
//
//                this.mFormat = localMediaFormat;
//                if (this.mFrameRate > 0)
//                    this.mFrameInverval = (1000000 / this.mFrameRate);
//                else {
//                    this.mFrameInverval = 40000;
//                }
//
//                this.mCodec.init(this.mFormat, ((MediaCodecInfo) localList.get(i)).getName(), this);
//            } catch (Exception localException) {
//                this.mCodec = null;
//            }
//    }
}