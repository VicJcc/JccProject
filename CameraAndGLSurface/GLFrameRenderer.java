package com.taole.myapplication.CameraAndGLSurface;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.view.Surface;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLFrameRenderer implements Renderer {

    private GLSurfaceView mTargetSurface;
    private GLProgram prog = new GLProgram(0);
    private int mScreenWidth, mScreenHeight;
    private int mVideoWidth, mVideoHeight;
    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;

    public GLFrameRenderer(GLSurfaceView surface, DisplayMetrics dm, RenderCallbackTest mTest) {
//        mParentAct = callback;
        mTargetSurface = surface;
        this.mTest = mTest;
//        mScreenWidth = dm.widthPixels;
//        mScreenHeight = dm.heightPixels;
        mScreenWidth = 800;
        mScreenHeight = 600;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (!prog.isProgramBuilt()) {
            prog.buildProgram();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        update(640, 480);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (y != null) {
                // reset position, have to be done
                y.position(0);
                u.position(0);
                v.position(0);
                prog.buildTextures(y, u, v, mVideoWidth, mVideoHeight);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                prog.drawFrame();
                if(mTest != null){
                    mTest.onFrameAvailableTest();
                }
            }
        }
    }

    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     */
    public void update(int w, int h) {
        if (w > 0 && h > 0) {
            // 调整比例
//            if (mScreenWidth > 0 && mScreenHeight > 0) {
//                float f1 = 1f * mScreenHeight / mScreenWidth;
//                float f2 = 1f * h / w;
//                if (f1 == f2) {
                    prog.createBuffers(GLProgram.squareVertices);
//                } else if (f1 < f2) {
//                    float widScale = f1 / f2;
//                    prog.createBuffers(new float[] { -widScale, -1.0f, widScale, -1.0f, -widScale, 1.0f, widScale,
//                            1.0f, });
//                } else {
//                    float heightScale = f2 / f1;
//                    prog.createBuffers(new float[] { -1.0f, -heightScale, 1.0f, -heightScale, -1.0f, heightScale, 1.0f,
//                            heightScale, });
//                }
//            }
            // 初始化容器
            if (w != mVideoWidth && h != mVideoHeight) {
                this.mVideoWidth = w;
                this.mVideoHeight = h;
                int yarraySize = w * h;
                int uvarraySize = yarraySize / 4;
                synchronized (this) {
                    y = ByteBuffer.allocate(yarraySize);
                    u = ByteBuffer.allocate(uvarraySize);
                    v = ByteBuffer.allocate(uvarraySize);
                }
            }
        }

//        mParentAct.onPlayStart();
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */
    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
        synchronized (this) {
            if(y != null && ydata != null) {
                y.clear();
                u.clear();
                v.clear();
                y.put(ydata, 0, ydata.length);
                u.put(udata, 0, udata.length);
                v.put(vdata, 0, vdata.length);
            }
        }

        // request to render
        mTargetSurface.requestRender();
    }

    /**
     * this method will be called from native code, it's used for passing play state to activity.
     */
    public void updateState(int state) {
//        if (mParentAct != null) {
//            mParentAct.onReceiveState(state);
//        }
    }

    private RenderCallbackTest mTest;
    public interface RenderCallbackTest{
        void onSurfaceCreatedTest(GL10 gl, EGLConfig config, Surface surface);
        void onSurfaceChangedTest(GL10 gl, int width, int height);
        void onFrameAvailableTest();
    }
}