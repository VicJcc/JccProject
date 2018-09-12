package com.taole.myapplication.MediaTest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;

/**
 * 摄像头控制
 * Created by taole on 15/5/9.
 */
public class TLCameraManager {
    public static final int FRAME_RATE_MIN = 15;               // 20fps
    public static final int FRAME_RATE_MAX = 20;               // 20fps
    public static final int VIDEO_WIDTH = 480;
    public static final int VIDEO_HEIGHT = 480;
    public static final Point ENCODE_SIZE = new Point(VIDEO_WIDTH, VIDEO_HEIGHT);
    private static final String TAG = "TLCameraManager";

    //目标采集比率以及编码大小

    private static TLCameraManager mManager;

    public static TLCameraManager sharedCameraManager() throws IllegalArgumentException {
        if (null == mManager) {
            throw new IllegalArgumentException("还没有通过init做初始化");
        }
        return mManager;
    }

    public static void init(Context context) {
        if (null == mManager) {
            mManager = new TLCameraManager(context);
        }
    }

    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        // no camera on this device
        return false;
    }


    private Context mContext;
    private Camera mCamera;
    private int mCameraId = -1;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    private Camera.Size mCameraPreviewSize;
    private final Object mLock = new Object();
    private boolean bSwapAble = true; // 摄像头是否可切换

    //Notice 临时添加，以后和video一同改掉
    private MediaTestActivity.CameraHandler mCameraHandler;

    public void setCameraHandler(MediaTestActivity.CameraHandler handler) {
        mCameraHandler = handler;
    }

    public TLCameraManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public Camera getCameraInstance() {

        synchronized (mLock) {
            if (null == mCamera) {
                initCamera(mCameraFacing);
            }

            return mCamera;
        }
    }

    private Camera initCamera(int facing) {

        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (facing == cameraInfo.facing) {
                    mCameraId = i;
                    if(bSwapAble) {
                        mCamera = Camera.open(i); // attempt to get a Camera instance
                    }
                }
            }
        } catch (Exception exp) {
            // Null if Camera is not available (in use or does not exist)
            mCameraId = -1;
            mCamera = null;
            exp.printStackTrace();
        }
        return mCamera;
    }

    public int getCameraFacing() {
        synchronized (mLock) {
            return mCameraFacing;
        }
    }

    public void setSwapAble(boolean swapAble){
        bSwapAble = swapAble;
    }

    public boolean swapCameraFacingTo(int facing) {
        synchronized (mLock) {
            if (facing == mCameraFacing) {
                return false;
            }
            if(!bSwapAble){
                return false;
            }

            //前移，如果下面操作失败，即停止录像，再按切换可切换
            mCameraFacing = facing;

            releaseCamera(false);
            Camera camera = initCamera(facing);
            if (null == camera) {
                return false;
            }

            try {
                config(camera);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (null == mCameraHandler) {
                return false;
            }

            mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
                    MediaTestActivity.CameraHandler.MSG_SET_SURFACE_TEXTURE, null));
            return true;
        }
    }


    public void closeCamera() {
        synchronized (mLock) {
            releaseCamera(true);
        }
    }

    private void releaseCamera(boolean reset) {
        mZoomIndex = 0;
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (reset) {
            mCameraId = -1;
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
    }


    public void config(Camera camera) throws Exception {

        if (null == camera) {
            camera = getCameraInstance();
        }

        if (null == camera) {
            throw new IllegalArgumentException("no available camera");
        }

        Camera.Parameters parameters = camera.getParameters();
        // Give the camera a hint that we're recording video.  This can have a big
        // impact on frame rate.
        parameters.setPictureSize(720, 720);//暂时测试一下 不行
        mCameraPreviewSize = choosePreviewSize(parameters, ENCODE_SIZE.x, ENCODE_SIZE.y);
        parameters.setRecordingHint(true);
//        parameters.setPreviewSize(mCameraPreviewSize.width,mCameraPreviewSize.height);
        //fps
        parameters.set("video-size", "720X720");
        List<int[]> fpsRange = parameters.getSupportedPreviewFpsRange();
        int count = fpsRange.size();
        int[] choosenRange = null;
        for (int i = 0; i < count; i++) {
            int[] range = fpsRange.get(i);
            if (range[0] >= FRAME_RATE_MIN * 1000) {

                choosenRange = range;
                if (range[1] <= FRAME_RATE_MAX * 1000) {
                    break;
                }
            }
        }
        if (null != choosenRange) {
            parameters.setPreviewFpsRange(choosenRange[0], choosenRange[1]);
        }

        //focus
        List<String> supportFocusMode = parameters.getSupportedFocusModes();
        if (supportFocusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(parameters);
    }

    public float getVideoRatio(int width, int height) {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (Surface.ROTATION_0 == rotation) {
            return height * 1.0f / width;
        }
        return width * 1.0f / height;
    }


    public boolean adjustCameraDisplayOrientation() {
        Camera camera = getCameraInstance();
        if (null == camera) {
            return false;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);

        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);

        return true;
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

        if (targetPreviewSize.width > targetPreviewSize.height) {
            width ^= height;
            height ^= width;
            width ^= height;
        }
        float targetRatio = getAspectRatio(width, height);

        //低分辨率兼容性比较差,即便是tm supported的－ －， Camera.CAMERA_ERROR_SERVER_DIED
//        for (int i = count - 1; i >= 0; i-- ) {
        for (int i = 0; i < count; i++) {

            Camera.Size size = previewSizes.get(i);

            float ratio = getAspectRatio(size.width, size.height);
            if (targetRatio == ratio) {

                if (size.width > width && size.height > height) {
                    targetPreviewSize = size;
                    break;
                }
            }
        }

        parms.setPreviewSize(targetPreviewSize.width, targetPreviewSize.height);
        return targetPreviewSize;
    }


    private float getAspectRatio(int width, int height) {

        float ratio = width * 1.f / height;
        int round = (int) (ratio * 100);
        return round / 100.f;
    }

    public Camera.Size getCameraPreviewSize() {
        return mCameraPreviewSize;
    }

    public boolean isTorchOn() {

        Camera camera = getCameraInstance();
        if (null == camera) {
            return false;
        }

        Camera.Parameters parameter = camera.getParameters();
        return Camera.Parameters.FLASH_MODE_TORCH.equals(parameter.getFlashMode());
    }

    public boolean setTorchEnable(boolean flag) {

        Camera camera = getCameraInstance();
        if (null == camera) {
            return false;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return false;
        }
        Camera.Parameters parameter = camera.getParameters();

        String flashMode = flag ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF;

        List<String> supportMode = parameter.getSupportedFlashModes();
        if (null == supportMode || !supportMode.contains(flashMode)) {
            return false;
        }
        parameter.setFlashMode(flashMode);
        camera.setParameters(parameter);
        return true;
    }

    private double mLastScale = 1;
    private int mZoomIndex = 0;
    private final int ZOOM_INDEX_INC = 1;

    public void cameraZoom(double scale) {

        Camera camera = getCameraInstance();
        if (null == camera) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        if (!parameters.isZoomSupported()) {
            return;
        }

        if (scale == mLastScale) {
            return;
        }
        //zoomIn
        if (scale > mLastScale) {
            mZoomIndex += ZOOM_INDEX_INC;
        } else {
            mZoomIndex -= ZOOM_INDEX_INC;
        }
        mLastScale = scale;
        int max = parameters.getMaxZoom();

        if (mZoomIndex < 0) {
            mZoomIndex = 0;
        }
        if (mZoomIndex > max) {
            mZoomIndex = max;
        }

        parameters.setZoom(mZoomIndex);
        camera.setParameters(parameters);
    }

    public void setFace(int facing) {
        synchronized (mLock) {
            mCameraFacing = facing;
        }
    }
}
