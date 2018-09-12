package com.taole.myapplication.MediaTest;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.taole.myapplication.R;

import java.lang.ref.WeakReference;

/**
 *  Created by jincancan on 15/10/19.
 */
public class MediaTestActivity extends Activity implements View.OnClickListener, SurfaceTexture.OnFrameAvailableListener {

    private Activity mActivity = null;
    private GLSurfaceView mGLSurfaceView = null;
    private SurfaceView mSurfaceView = null;
    private MediaRender mRender = null;
    private CameraHandler mCameraHandler;
    Camera mCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_test_activity);
        mActivity = this;
//        mCameraHandler = new CameraHandler(this);
//        try {
//            TLCameraManager.init(mActivity);
//            TLCameraManager.sharedCameraManager().setSwapAble(true);
//            TLCameraManager.sharedCameraManager().config(null);
//            TLCameraManager.sharedCameraManager().setCameraHandler(mCameraHandler);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.gsv_test);
//        mGLSurfaceView.setEGLContextClientVersion(2);
//        mRender = new MediaRender(mCameraHandler);
//        mGLSurfaceView.setRenderer(mRender);
//        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mSurfaceView = (SurfaceView) findViewById(R.id.sv_test);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                try {
                    TLCameraManager.init(mActivity);
//                    TLCameraManager.sharedCameraManager().setSwapAble(true);
                    TLCameraManager.sharedCameraManager().config(null);
//                    TLCameraManager.sharedCameraManager().setCameraHandler(mCameraHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mCamera = TLCameraManager.sharedCameraManager().getCameraInstance();// 开启摄像头（2.3版本后支持多摄像头,需传入参数）
                try
                {
                    mCamera.setPreviewDisplay(holder);//set the surface to be used for live preview

                } catch (Exception ex)
                {
                    if(null != mCamera)
                    {
                        mCamera.release();
                        mCamera = null;
                    }
                }
                mCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mGLSurfaceView.onPause();
    }

    @Override

    protected void onResume() {
        super.onResume();
//        mGLSurfaceView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        invalidateHandler();
        TLCameraManager.sharedCameraManager().closeCamera();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }

    static class CameraHandler extends Handler {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;

        // Weak reference to the Activity; only access this from the UI thread.
        private WeakReference<MediaTestActivity> mWeakActivity;
        private WeakReference<SurfaceTexture> nWeakSurfaceTexture;

        public CameraHandler(MediaTestActivity activity) {
            mWeakActivity = new WeakReference<MediaTestActivity>(activity);
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

            if(true){
                return;
            }

            final MediaTestActivity activity = mWeakActivity.get();
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
                    Camera camera = TLCameraManager.sharedCameraManager().getCameraInstance();
                    if (null == camera) {
                        return;
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
                    final Camera.Size previewSize = TLCameraManager.sharedCameraManager().getCameraPreviewSize();
                    activity.mGLSurfaceView.queueEvent(new Runnable() {
                        public void run() {
                            //nullpoint 但是不确定什么为null --czn
                            if (null != activity.mRender && null != previewSize) {
                                activity.mRender.setCameraPreviewSize(previewSize.width, previewSize.height);
                            }
                        }
                    });
//                    if(bNeedFace) {
//                        bNeedFace = false;
//                        TLCameraManager.sharedCameraManager().swapCameraFacingTo(Camera.CameraInfo.CAMERA_FACING_FRONT);
//                    }

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
        mCameraHandler.removeCallbacksAndMessages(null);
        mCameraHandler.invalidateHandler();
    }
}

