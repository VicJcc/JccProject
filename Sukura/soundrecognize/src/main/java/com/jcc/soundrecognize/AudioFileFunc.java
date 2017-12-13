package com.jcc.soundrecognize;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import com.jcc.common.util.JFileUtils;

import java.io.File;
import java.io.IOException;

public class AudioFileFunc {
    //音频输入-麦克风
    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;

    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //notice 16000 讯飞语音特别要求
    public final static int AUDIO_SAMPLE_RATE = 16000;  //44.1KHz,普遍使用的频率
    //录音输出文件
    private final static String AUDIO_RAW_FILENAME = "RawAudio.raw";
    private final static String AUDIO_WAV_FILENAME = "FinalAudio.wav";
    public final static String AUDIO_AMR_FILENAME = "FinalAudio.amr";

    public final static String AUDIO_DIR = "Audio";

    /**
     * 判断是否有外部存储设备sdcard
     * @return true | false
     */
    public static boolean isSdcardExit(){
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * 获取麦克风输入的原始音频流文件路径
     * @return
     */
    public static String getRawFilePath(Context context){
        String mAudioRawPath = "";
        if(isSdcardExit()){
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioRawPath = fileBasePath+"/"+AUDIO_RAW_FILENAME;
            File file = JFileUtils.getFile(context, AUDIO_DIR, AUDIO_RAW_FILENAME, true);
            if(file != null) {
                mAudioRawPath = file.getPath();
            }
        }

        return mAudioRawPath;
    }

    /**
     * 获取编码后的WAV格式音频文件路径
     * @return
     */
    public static String getWavFilePath(Context context){
        String mAudioWavPath = "";
        if(isSdcardExit()){
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioWavPath = fileBasePath+"/"+AUDIO_WAV_FILENAME;
            File file = JFileUtils.getFile(context, AUDIO_DIR, AUDIO_WAV_FILENAME, true);
            if(file != null) {
                mAudioWavPath = file.getPath();
            }
        }
        return mAudioWavPath;
    }


    /**
     * 获取编码后的AMR格式音频文件路径
     * @return
     */
    public static String getAMRFilePath(){
        String mAudioAMRPath = "";
        if(isSdcardExit()){
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioAMRPath = fileBasePath+"/"+AUDIO_AMR_FILENAME;

        }
        return mAudioAMRPath;
    }


    /**
     * 获取文件大小
     * @param path,文件的绝对路径
     * @return
     */
    public static long getFileSize(String path){
        File mFile = new File(path);
        if(!mFile.exists())
            return -1;
        return mFile.length();
    }


    public static int getLength(String url){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int length = mediaPlayer.getDuration();
        mediaPlayer.release();
        return length;
    }
}
