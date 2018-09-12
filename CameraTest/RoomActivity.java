package com.taole.myapplication.CameraTest;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.taole.myapplication.CameraTest.TLMediaCamera.CameraSurfaceRenderer;
import com.taole.myapplication.CameraTest.TLMediaCamera.TLCameraManager;
import com.taole.myapplication.CameraTest.TLMediaCamera.TLResizeSurfaceView;
import com.taole.myapplication.R;

import java.lang.ref.WeakReference;

/**
 * Created by jincancan on 15/11/30.
 */
public class RoomActivity extends Activity implements SurfaceTexture.OnFrameAvailableListener {


    private TLResizeSurfaceView mGLSurfaceView;
//    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_activity);
//        mTextView = (TextView) findViewById(R.id.tv_camera);
        mGLSurfaceView = (TLResizeSurfaceView)findViewById(R.id.room_video_me);
        mCameraHandler = new CameraHandler(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mRenderer = new CameraSurfaceRenderer(mCameraHandler, null);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        mTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
                initCamera();
                initCameraSize();
//            }
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override

    protected void onResume() {
        super.onResume();
//        mGLSurfaceView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        invalidateHandler();
        TLCameraManager.sharedCameraManager().closeCamera();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }

    private CameraHandler mCameraHandler;
    private boolean isCameraInited;
    private static boolean bNeedFace;
    private CameraSurfaceRenderer mRenderer;
    private boolean bFrameAvailable;

    private void initCamera() {
        try {
            TLCameraManager.init(this);
            TLCameraManager.sharedCameraManager().setSwapAble(true);
            TLCameraManager.sharedCameraManager().config(null);
            TLCameraManager.sharedCameraManager().setCameraHandler(mCameraHandler);
            isCameraInited = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRenderer.setIsInited(true);
    }

    private void initCameraSize() {
        final Camera.Size previewSize = TLCameraManager.sharedCameraManager().getCameraPreviewSize();
        //Notice 如果用户拒绝给权限 previewsize为null
        if (previewSize == null) {
            return;
        }
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        float x = TLCameraManager.sharedCameraManager().getVideoRatio(previewSize.width, previewSize.height) * size.y;
//        mGLSurfaceView.resize(1024, 1024);

        mGLSurfaceView.onResume();
        mGLSurfaceView.queueEvent(new Runnable() {
            public void run() {
                mRenderer.setCameraPreviewSize(previewSize.width, previewSize.height);
            }
        });
    }

    /**
     * Handles camera operation requests from other threads.  Necessary because the Camera
     * must only be accessed from one thread.
     * <p>
     * The object is created on the UI thread, and all handlers run there.  Messages are
     * sent from other threads, using sendMessage().
     */
    public static class CameraHandler extends Handler {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;

        // Weak reference to the Activity; only access this from the UI thread.
        private WeakReference<RoomActivity> mWeakActivity;
        private WeakReference<SurfaceTexture> nWeakSurfaceTexture;

        public CameraHandler(RoomActivity activity) {
            mWeakActivity = new WeakReference<RoomActivity>(activity);
        }

        /**
         * Drop the reference to the activity.  Useful as a paranoid measure to ensure that
         * attempts to access a stale Activity through a handler are caught.
         */
        public void invalidateHandler() {
            mWeakActivity.clear();
            if (null != nWeakSurfaceTexture)
                nWeakSurfaceTexture.clear();
        }

        @Override  // runs on UI thread
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;

            final RoomActivity activity = mWeakActivity.get();
            if (activity == null) {
                return;
            }

            Exception exception = null;

            switch (what) {

                case MSG_SET_SURFACE_TEXTURE:

                    /**
                     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
                     */
                    SurfaceTexture st = (SurfaceTexture) inputMessage.obj;
                    if (null != st) {
                        nWeakSurfaceTexture = new WeakReference<SurfaceTexture>(st);
                    }

                    if (null == nWeakSurfaceTexture) {
                        return;
                    }

                    st = nWeakSurfaceTexture.get();
                    if (null == st) {
                        return;
                    }
                    if(bNeedFace) {
                        TLCameraManager.sharedCameraManager().setFace(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    }
                    Camera camera = TLCameraManager.sharedCameraManager().getCameraInstance();
                    if (null == camera) {
                        return;
                    }
                    if(bNeedFace) {
                        bNeedFace = false;
                        try {
                            TLCameraManager.sharedCameraManager().config(camera);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    st.setOnFrameAvailableListener(activity);
                    try {
                        camera.setPreviewTexture(st);
                        TLCameraManager.sharedCameraManager().adjustCameraDisplayOrientation();

                    } catch (Exception exp) {
                        exception = exp;
                        break;
                    }
                    camera.startPreview();
//                    final Camera.Size previewSize = TLCameraManager.sharedCameraManager().getCameraPreviewSize();
//                    activity.mGLSurfaceView.queueEvent(new Runnable() {
//                        public void run() {
//                            //nullpoint 但是不确定什么为null --czn
//                            if (null != activity.mRenderer && null != previewSize) {
//                                activity.mRenderer.setCameraPreviewSize(previewSize.width, previewSize.height);
//                            }
//                        }
//                    });

                    break;
                default:
                    exception = new RuntimeException("unknown msg " + what);
            }

            if (null != exception) {
                //TODO 观察发生场景，增加错误处理
                exception.printStackTrace();
            }
        }
    }

    public void invalidateHandler(){
        if(mCameraHandler != null) {
            mCameraHandler.removeCallbacksAndMessages(null);
            mCameraHandler.invalidateHandler();
        }
    }
}
