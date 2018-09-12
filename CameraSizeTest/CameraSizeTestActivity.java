package com.taole.myapplication.CameraSizeTest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.taole.myapplication.R;
import com.taole.myapplication.Utils.BitmapUtils;
import com.taole.myapplication.Utils.TLLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;

//import taole.com.medialib.Utils.TLLogger;

/**
 * Created by jincancan on 15/12/2.
 */
public class CameraSizeTestActivity extends Activity {

    public static final String TAG = "CameraSizeTestActivity";

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Camera mCamera;
    public static VideoDataEncoder fuckEncoder = null;
    private TestFragment mTestFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerasize_activity);
        mSurfaceView = (SurfaceView)findViewById(R.id.sv_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                mCamera = Camera.open();// 开启摄像头（2.3版本后支持多摄像头,需传入参数）
                try
                {
                    mCamera.setPreviewDisplay(mSurfaceHolder);//set the surface to be used for live preview

                } catch (Exception ex)
                {
                    if(null != mCamera)
                    {
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
        });
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        TextView textView = (TextView)findViewById(R.id.tv_takephoto);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        File file = new File(Environment.getExternalStorageDirectory()+"/aaa.jpg");
                        if(!file.exists()){
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        BitmapUtils.saveBitmapToFile(file, mBitmap, Bitmap.CompressFormat.JPEG);

                    }
                });
            }
        });

        mTestFragment = new TestFragment();
        getFragmentManager().beginTransaction().replace(R.id.fl_test, mTestFragment).commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    /*【2】【相机预览】*/
    private void initCamera()//surfaceChanged中调用
    {
        if(null != mCamera)
        {
            try
            {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
                Camera.Size psize = null;
                for (int i = 0; i < previewSizes.size(); i++)
                {
                    psize = previewSizes.get(i);
                }
                Integer pf = null;
                for (int i = 0; i < previewFormats.size(); i++)
                {
                    pf = previewFormats.get(i);
                }
                Camera.Size previewSize = choosePreviewSize(parameters, 480, 640);
//                fuckEncoder = new VideoDataEncoder(previewSize.width, previewSize.height);
//                fuckEncoder = new VideoDataEncoder(previewSize.width, previewSize.height);
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                mCamera.setDisplayOrientation(90);
                parameters.setPreviewFormat(ImageFormat.NV21);
                mCamera.setParameters(parameters); // 将Camera.Parameters设定予Camera
                mCamera.startPreview(); // 打开预览画面
//                ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
//                params.width = WindowUtil.getWindowWidth(this);
//                params.height = WindowUtil.getWindowWidth(this) * previewSize.width / previewSize.height;
//                params.width = previewSize.height * 2;
//                params.height = previewSize.width * 2;
//                mSurfaceView.setLayoutParams(params);

//                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] data, Camera camera) {
//                        TLLogger.trace("JCCTLTESTfuck", "data length: " + data.length );
//
//                        fuckEncoder.encode(data);
//
//                    }
//                });

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private Camera.Size choosePreviewSize(Camera.Parameters parms, int width, int height) {

        Camera.Size targetPreviewSize = null;

        //以ratio 为主, on s'en fou de size
        List<Camera.Size> previewSizes = parms.getSupportedPreviewSizes();
        int count = previewSizes.size();

        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        if (null != ppsfv) {
            targetPreviewSize = ppsfv;
        } else {
            targetPreviewSize = previewSizes.get(0);
        }

        float targetRatio = getAspectRatio(height, width);
        for (int i = 0; i < count; i++) {

            Camera.Size size = previewSizes.get(i);

            float ratio = getAspectRatio(size.width, size.height);
            TLLogger.trace("JCCCameraSize", "  Width:" + size.width +
                    "  Height:" + size.height +
                    "  ratio:" + ratio +
                    "  targetRatio:" + targetRatio);
        }
        //低分辨率兼容性比较差,即便是tm supported的－ －， Camera.CAMERA_ERROR_SERVER_DIED
//        for (int i = count - 1; i >= 0; i-- ) {
        for (int i = 0; i < count; i++) {

            Camera.Size size = previewSizes.get(i);

            float ratio = getAspectRatio(size.width, size.height);
            TLLogger.trace("CameraSize", "  Width:" + size.width + "  Height:" + size.height + "   ratio:"+ratio);

            if (targetRatio == ratio) {

//                if (size.width < width && size.height < height) {
                    targetPreviewSize = size;
                    break;
//                }
            }
        }
//        targetPreviewSize = previewSizes.get(8);
        parms.setPreviewSize(targetPreviewSize.width, targetPreviewSize.height);
        TLLogger.trace(TAG+"JCC", "camera preview size: width=" + targetPreviewSize.width + " <<>> height=" + targetPreviewSize.height);
        return targetPreviewSize;
    }

    private float getAspectRatio(int width, int height) {

        float ratio = width * 1.f / height;
        int round = (int) (ratio * 100);
        return round / 100.f;
    }
}
