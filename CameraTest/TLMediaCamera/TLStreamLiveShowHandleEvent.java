package com.taole.myapplication.CameraTest.TLMediaCamera;

/**
 * 主播直播控制事件
 * Created by taole on 15/5/14.
 */
public class TLStreamLiveShowHandleEvent{

    /**
     * 录制控制，录制就代表开始直播了
     * handleData --> boolean record on / off
     */
    public static final int TLStreamLiveShowHandleType_Record = 0;
    public static final int TLStreamLiveShowHandleType_RecordResume = 1;
    public static final int TLStreamLiveShowHandleType_FocusStop = 2;

    public int handleType = TLStreamLiveShowHandleType_Record;
    public Object handleData;

    public TLStreamLiveShowHandleEvent(int type, Object data) {
        handleType = type;
        handleData = data;
    }
}
