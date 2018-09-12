package com.taole.myapplication.CameraAndGLSurface;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.taole.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  Created by jincancan on 16/5/4.
 */
public class CameraActivity extends Activity implements GLRender.RenderCallback, GLFrameRenderer.RenderCallbackTest {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder = null;
    private Camera mCamera;


    public static int height = 480;
    public static int width = 640;

    private GLSurfaceView mGLSurfaceView;
    private GLRender mGLRender;
    private GLFrameRenderer mFrameRenderer;


    private ArrayList<byte[]> datas = new ArrayList<>();


    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_layout);

        mSurfaceView = (SurfaceView) findViewById(R.id.sv_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);


        mTextView = (TextView) findViewById(R.id.tv_view);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLSurfaceView.setVisibility(View.VISIBLE);
            }
        });

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.gsv_data);
        mGLRender = new GLRender(this);
        mFrameRenderer = new GLFrameRenderer(mGLSurfaceView,null, this);
        mGLSurfaceView.setEGLContextClientVersion(2);// select GLES 2.0
        mGLSurfaceView.setRenderer(mFrameRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLSurfaceView.setPreserveEGLContextOnPause(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.release();
        mCamera = null;
    }

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mCamera = Camera.open();// 开启摄像头（2.3版本后支持多摄像头,需传入参数）
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);//set the surface to be used for live preview
            } catch (Exception ex) {
                if(null != mCamera) {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            initCamera();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private void initCamera()//surfaceChanged中调用
    {
        if(null != mCamera) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
                parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPreviewSize(width,height);
//                mCamera.setDisplayOrientation(90);
                mCamera.setParameters(parameters); // 将Camera.Parameters设定予Camera
                mCamera.startPreview(); // 打开预览画面

                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
//                        TLLogger.trace("JCCTLTESTfuck", "data length: " + data.length );
                        datas.add(data);
//                        if(mGLRender != null){
//                            mGLRender.setdata(datas.get(0));
//                        }

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config, Surface surface) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        datas.remove(0);
        mGLSurfaceView.requestRender();
    }

    @Override
    public void onSurfaceCreatedTest(GL10 gl, EGLConfig config, Surface surface) {

//        System.arraycopy(yv12bytes, 0, i420bytes, 0, width*height);
//        System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height, width*height/4);
//        System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4, width*height/4);

    }

    @Override
    public void onSurfaceChangedTest(GL10 gl, int width, int height) {

    }
    byte[] y;
    byte[] u;
    byte[] v;
    @Override
    public void onFrameAvailableTest() {
        if(datas.size() > 0) {
            y = new byte[width * height];
            u = new byte[width * height / 4];
            v = new byte[width * height / 4];
            System.arraycopy(datas.get(0), 0, y, 0, width * height);
            System.arraycopy(datas.get(0), width * height + width * height / 4, u, 0, width * height / 4);
            System.arraycopy(datas.get(0), width * height, v, 0, width * height / 4);
            datas.remove(0);
        }

        mFrameRenderer.update(y,u,v);
    }
}
