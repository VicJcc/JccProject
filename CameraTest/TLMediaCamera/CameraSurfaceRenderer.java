package com.taole.myapplication.CameraTest.TLMediaCamera;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import com.taole.myapplication.CameraTest.RoomActivity;
import com.taole.myapplication.Utils.TLLogger;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



/**
 * Renderer object for our GLSurfaceView.
 * <p>
 * Do not call any methods here directly from another thread -- use the
 * GLSurfaceView#queueEvent() call.
 */
public class CameraSurfaceRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "CameraSurfaceRenderer";

    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;

    private RoomActivity.CameraHandler mCameraHandler;
    private File mOutputFile;

    private FullFrameRect mFullScreen;

    private final float[] mSTMatrix = new float[16];
    private int mTextureId;

    private SurfaceTexture mSurfaceTexture;
    private boolean mRecordingEnabled;
    private int mRecordingStatus;

    // width/height of the incoming camera preview frames
    private boolean mIncomingSizeUpdated;
    private int mIncomingWidth;
    private int mIncomingHeight;

    private int mCurrentFilter;
    private int mNewFilter;

    //预留以后如果需要
    // Camera filters; must match up with cameraFilterNames in strings.xml
    static final int FILTER_NONE = 0;
    static final int FILTER_BLACK_WHITE = 1;
    static final int FILTER_BLUR = 2;
    static final int FILTER_SHARPEN = 3;
    static final int FILTER_EDGE_DETECT = 4;
    static final int FILTER_EMBOSS = 5;
    static final int FILTER_BRIGHT = 6;


    private boolean isInited;
    /**
     * Constructs CameraSurfaceRenderer.
     * <p>
     * @param cameraHandler Handler for communicating with UI thread
//     * @param movieEncoder video encoder object
     * @param outputFile output file for encoded video; forwarded to movieEncoder
     */
    public CameraSurfaceRenderer(RoomActivity.CameraHandler cameraHandler,
                                  File outputFile) {
        mCameraHandler = cameraHandler;
        mOutputFile = outputFile;

        mTextureId = -1;

        mRecordingStatus = -1;
        mRecordingEnabled = false;

        mIncomingSizeUpdated = false;
        mIncomingWidth = mIncomingHeight = -1;

        // We could preserve the old filter mode, but currently not bothering.
        mCurrentFilter = -1;
        mNewFilter = FILTER_NONE;
    }

    /**
     * Notifies the renderer thread that the activity is pausing.
     * <p>
     * For best results, call this *after* disabling Camera preview.
     */
    public void notifyPausing() {
        if (mSurfaceTexture != null) {
            TLLogger.trace(TAG, "renderer pausing -- releasing SurfaceTexture");
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             //  to be destroyed
        }
        mIncomingWidth = mIncomingHeight = -1;
    }

    /**
     * Notifies the renderer that we want to stop or start recording.
     */
    public void changeRecordingState(boolean isRecording, boolean changeState) {
        TLLogger.trace(TAG, "changeRecordingState: was " + mRecordingEnabled + " now " + isRecording);
        mRecordingEnabled = isRecording;

        if (changeState) {
            TLLogger.trace(TAG, "jcclive STOP "+ (null == mSurfaceTexture) + " " + (null == mFullScreen));

            mRecordingStatus = mRecordingEnabled ? RECORDING_OFF : RECORDING_RESUMED;

            if (null == mSurfaceTexture && null == mFullScreen) {

                if (isRecording) {
                    start();
                }
                else {
                    stop();
                }
            }
        }
    }

    public void justStop(){
        stop();
    }

    private void init() {

        // Set up the texture blitter that will be used for on-screen display.  This
        // is *not* applied to the recording, because that uses a separate shader.
        mFullScreen = new FullFrameRect(
                new Texture2dProgram(getType()));

        mTextureId = mFullScreen.createTextureObject();

        // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
        // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
        // available messages will arrive on the main thread.
        mSurfaceTexture = new SurfaceTexture(mTextureId);
    }
    public static final float BITRATE_RATIO = 2;            // width * height * ratio ,  2 同iOS同步

    private void start() {
        TLLogger.trace(TAG, "START recording");
        // start recording

        int bitRatePerSecons = (int)(TLCameraManager.ENCODE_SIZE.x * TLCameraManager.ENCODE_SIZE.y * BITRATE_RATIO);

        mRecordingStatus = RECORDING_ON;
    }

    private void stop() {

        TLLogger.trace(TAG, "STOP recording");
//        if (null != mVideoEncoder)
//            mVideoEncoder.stopRecording();
        mRecordingStatus = RECORDING_OFF;
    }


    /**
     * Changes the filter that we're applying to the camera preview.
     */
    public void changeFilterMode(int filter) {
        mNewFilter = filter;
//        mVideoEncoder.updateMode(getType());
    }

    private Texture2dProgram.ProgramType getType(){
        Texture2dProgram.ProgramType type = Texture2dProgram.ProgramType.TEXTURE_EXT;
        switch (mNewFilter) {
            case FILTER_NONE:
                type = Texture2dProgram.ProgramType.TEXTURE_EXT;
                break;
            case FILTER_BLACK_WHITE:
                type = Texture2dProgram.ProgramType.TEXTURE_EXT_BW;
                break;
            case FILTER_BLUR:
                type = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                break;
            case FILTER_SHARPEN:
                type = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                break;
            case FILTER_EDGE_DETECT:
                type = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                break;
            case FILTER_EMBOSS:
                type = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                break;
            case FILTER_BRIGHT:
                type = Texture2dProgram.ProgramType.TEXTURE_EXT_BRIGHT;
                break;
            default:
                throw new RuntimeException("Unknown filter mode " + mNewFilter);
        }
        return type;
    }

    /**
     * Updates the filter program.
     */
    public void updateFilter() {
        Texture2dProgram.ProgramType programType;
        float[] kernel = null;
        float colorAdj = 0.0f;

        TLLogger.trace(TAG, "Updating filter to " + mNewFilter);
        switch (mNewFilter) {
            case FILTER_NONE:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT;
                break;
            case FILTER_BLACK_WHITE:
                // (In a previous version the TEXTURE_EXT_BW variant was enabled by a flag called
                // ROSE_COLORED_GLASSES, because the shader set the red channel to the B&W color
                // and green/blue to zero.)
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_BW;
                break;
            case FILTER_BRIGHT:
                // (In a previous version the TEXTURE_EXT_BW variant was enabled by a flag called
                // ROSE_COLORED_GLASSES, because the shader set the red channel to the B&W color
                // and green/blue to zero.)
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_BRIGHT;
                break;
            case FILTER_BLUR:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[] {
                        1f/16f, 2f/16f, 1f/16f,
                        2f/16f, 4f/16f, 2f/16f,
                        1f/16f, 2f/16f, 1f/16f };
                break;
            case FILTER_SHARPEN:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[] {
                        0f, -1f, 0f,
                        -1f, 5f, -1f,
                        0f, -1f, 0f };
                break;
            case FILTER_EDGE_DETECT:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[] {
                        -1f, -1f, -1f,
                        -1f, 8f, -1f,
                        -1f, -1f, -1f };
                break;
            case FILTER_EMBOSS:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[] {
                        2f, 0f, 0f,
                        0f, -1f, 0f,
                        0f, 0f, -1f };
                colorAdj = 0.5f;
                break;
            default:
                throw new RuntimeException("Unknown filter mode " + mNewFilter);
        }

        // Do we need a whole new program?  (We want to avoid doing this if we don't have
        // too -- compiling a program could be expensive.)
        if (programType != mFullScreen.getProgram().getProgramType()) {
            mFullScreen.changeProgram(new Texture2dProgram(programType));
            // If we created a new program, we need to initialize the texture width/height.
            mIncomingSizeUpdated = true;
        }

        // Update the filter kernel (if any).
        if (kernel != null) {
            mFullScreen.getProgram().setKernel(kernel, colorAdj);
        }

        mCurrentFilter = mNewFilter;
    }

    /**
     * Records the size of the incoming camera preview frames.
     * <p>
     * It's not clear whether this is guaranteed to execute before or after onSurfaceCreated(),
     * so we assume it could go either way.  (Fortunately they both run on the same thread,
     * so we at least know that they won't execute concurrently.)
     */
    public void setCameraPreviewSize(int width, int height) {
        TLLogger.trace(TAG, "setCameraPreviewSize");
        mIncomingWidth = width;
        mIncomingHeight = height;
        mIncomingSizeUpdated = true;
    }

    //    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        TLLogger.trace(TAG, "onSurfaceCreated");

        // We're starting up or coming back.  Either way we've got a new EGLContext that will
        // need to be shared with the video encoder, so figure out if a recording is already
        // in progress.
//        mRecordingEnabled = mVideoEncoder.isRecording();
        if (mRecordingEnabled) {
            mRecordingStatus = RECORDING_RESUMED;
        } else {
            mRecordingStatus = RECORDING_OFF;
        }

        init();

        // Tell the UI thread to enable the camera preview.
        if (isInited) {
            mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
                    RoomActivity.CameraHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
        }
    }

    //    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        TLLogger.trace(TAG, "onSurfaceChanged " + width + "x" + height);
//        Camera camera = TLCameraManager.sharedCameraManager().getCameraInstance();
//        Camera.Parameters pp = camera.getParameters();
//        pp.setPreviewSize(1024, 1024);
//        camera.setParameters(pp);
    }


    //    @Override
    public void onDrawFrame(GL10 unused) {
//        TLLogger.trace(TAG, "onDrawFrame tex=" + mTextureId);

        // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
        // was there before.
        mSurfaceTexture.updateTexImage();

//        TLLogger.trace(TAG, "recording enable : " + mRecordingEnabled + " recording status: " + mRecordingStatus);
//        // If the recording state is changing, take care of it here.  Ideally we wouldn't
//        // be doing all this in onDrawFrame(), but the EGLContext sharing with GLSurfaceView
//        // makes it hard to do elsewhere.
//        if (mRecordingEnabled) {
//            switch (mRecordingStatus) {
//                case RECORDING_OFF:
//                    start();
//                    break;
//                case RECORDING_RESUMED:
//                    TLLogger.trace(TAG, "RESUME recording");
////                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
//                    mRecordingStatus = RECORDING_ON;
//                    break;
//                case RECORDING_ON:
//                    // yay
//                    break;
//                default:
//                    throw new RuntimeException("unknown status " + mRecordingStatus);
//            }
//        } else {
//            switch (mRecordingStatus) {
//                case RECORDING_ON:
//                case RECORDING_RESUMED:
//                    // stop recording
//                    stop();
//                    break;
//                case RECORDING_OFF:
//                    // yay
//                    break;
//                default:
//                    throw new RuntimeException("unknown status " + mRecordingStatus);
//            }
//        }
//
//        // Set the video encoder's texture name.  We only need to do this once, but in the
//        // current implementation it has to happen after the video encoder is started, so
//        // we just do it here.
//        //
//        // TODO: be less lame.
////        mVideoEncoder.setTextureId(mTextureId);
////
////        // Tell the video encoder thread that a new frame is available.
////        // This will be ignored if we're not actually recording.
////        mVideoEncoder.frameAvailable(mSurfaceTexture);
//
//
//        if (mIncomingWidth <= 0 || mIncomingHeight <= 0) {
//            // Texture size isn't set yet.  This is only used for the filters, but to be
//            // safe we can just skip drawing while we wait for the various races to resolve.
//            // (This seems to happen if you toggle the screen off/on with power button.)
//            TLLogger.trace(TAG, "Drawing before incoming texture size set; skipping");
//            return;
//        }
//        // Update the filter, if necessary.
//        if (mCurrentFilter != mNewFilter) {
//            updateFilter();
//        }
//        if (mIncomingSizeUpdated) {
//            mFullScreen.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
//            mIncomingSizeUpdated = false;
//        }

        // Draw the video frame.
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);
    }

    public void setIsInited(boolean isInited) {
        this.isInited = isInited;
        mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
                RoomActivity.CameraHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
    }
}
