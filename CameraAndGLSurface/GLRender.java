package com.taole.myapplication.CameraAndGLSurface;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.Surface;

import com.taole.myapplication.Utils.TLLogger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  Created by jincancan on 16/5/4.
 */
public class GLRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    public static final String TAG = "GLRender";
//    private static final String VERTEX_SHADER =
//            "uniform mat4 uMVPMatrix;\n" +
//                    "uniform mat4 uSTMatrix;\n" +
//                    "attribute vec4 aPosition;\n" +
//                    "attribute vec4 aTextureCoord;\n" +
//                    "varying vec2 vTextureCoord;\n" +
//                    "void main() {\n" +
//                    "    gl_Position = uMVPMatrix * aPosition;\n" +
//                    "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
//                    "}\n";
    private static final String VERTEX_SHADER =
                "attribute vec4 aPosition;\n"
                + "attribute vec2 aTextureCoord;\n"
                + "varying vec2 tc;\n"
                + "void main() {\n"
                + "gl_Position = aPosition;\n"
                + "tc = a_texCoord;\n"
                + "}\n";

//    private static final String FRAGMENT_SHADER =
//            "#extension GL_OES_EGL_image_external : require\n" +
//                    "precision mediump float;\n" +      // highp here doesn't seem to matter
//                    "varying vec2 vTextureCoord;\n" +
//                    "uniform samplerExternalOES sTexture;\n" +
//                    "void main() {\n" +
//                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
//                    "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
            + "uniform sampler2D tex_y;\n"
            + "uniform sampler2D tex_u;\n"
            + "uniform sampler2D tex_v;\n"
            + "varying vec2 tc;\n"
            + "void main() {\n"
            + "vec4 c = vec4((texture2D(tex_y, tc).r - 16./255.) * 1.164);\n"
            + "vec4 U = vec4(texture2D(tex_u, tc).r - 128./255.);\n"
            + "vec4 V = vec4(texture2D(tex_v, tc).r - 128./255.);\n"
            + "c += V * vec4(1.596, -0.813, 0, 0);\n"
            + "c += U * vec4(0, -0.392, 2.017, 0);\n"
            + "c.a = 1.0;\n"
            + "gl_FragColor = c;\n"
            + "}\n";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };
    /**
     * 三角形顶点缓冲?
     */
    private FloatBuffer mTriangleVertices;
    // 4x4 的矩阵
    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mTextureID = -12345;
    private int mProgram;

    private int maPositionHandle;
    private int maTextureHandle;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;

    private ByteBuffer mPixelBuf;                       // used by saveFrame()


    /***把图片的帧转成OpenGLes的纹理*/
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;


    private RenderCallback mRenderCallback;

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;


    private int mWidth = 640;
    private int mHeight = 360;

    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;


    public GLRender(RenderCallback renderCallback) {
        mRenderCallback = renderCallback;

        // 配置egl
        eglSetup();
        makeCurrent();
        setup();

        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);
    }

    /**
     * Prepares EGL.  We want a GLES 2.0 context and a surface that supports pbuffer.
     */
    private void eglSetup() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }

        // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
        };

        android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                attrib_list, 0);
        checkEglError("eglCreateContext");
        if (mEGLContext == null) {
            throw new RuntimeException("null context");
        }

        // Create a pbuffer surface.
        int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, mWidth,
                EGL14.EGL_HEIGHT, mHeight,
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);
        checkEglError("eglCreatePbufferSurface");
        if (mEGLSurface == null) {
            throw new RuntimeException("surface was null");
        }
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Creates interconnected instances of TextureRender, SurfaceTexture, and Surface.
     */
    private void setup() {
        mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        surfaceCreated();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);

        if (null != mRenderCallback) {
            mRenderCallback.onSurfaceCreated(gl, config, mSurface);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        int yarraySize = width * height;
        int uvarraySize = yarraySize / 4;
        y = ByteBuffer.allocate(yarraySize);
        u = ByteBuffer.allocate(uvarraySize);
        v = ByteBuffer.allocate(uvarraySize);

        if (null != mRenderCallback) {
            mRenderCallback.onSurfaceChanged(gl, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (null != mSurfaceTexture) {

            mSurfaceTexture.updateTexImage();
            drawFrame(mSurfaceTexture, false);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if(mRenderCallback != null){
            mRenderCallback.onFrameAvailable(surfaceTexture);
        }
    }

    public interface RenderCallback{
        void onSurfaceCreated(GL10 gl, EGLConfig config, Surface surface);
        void onSurfaceChanged(GL10 gl, int width, int height);
        void onFrameAvailable(SurfaceTexture surfaceTexture);
    }




    /**
     * Initializes GL state.  Call this after the EGL surface has been created and made current.
     */
    public void surfaceCreated() {
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkLocation(maPositionHandle, "aPosition");
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkLocation(maTextureHandle, "aTextureCoord");

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkLocation(muMVPMatrixHandle, "uMVPMatrix");
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkLocation(muSTMatrixHandle, "uSTMatrix");

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mTextureID = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexImage2D (GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mWidth, mHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mBuffer);
        checkGlError("glTexImage2D");



        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");
    }

    /**
     * 一些gl的配置
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            TLLogger.trace(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            TLLogger.trace(TAG, "Could not link program: ");
            TLLogger.trace(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * 加载着色器
     */
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            TLLogger.trace(TAG, "Could not compile shader " + shaderType + ":");
            TLLogger.trace(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * Draws the external texture in SurfaceTexture onto the current EGL surface.
     */
    public void drawFrame(SurfaceTexture st, boolean invert) {
        checkGlError("onDrawFrame start");
        st.getTransformMatrix(mSTMatrix);
        if (invert) {
            mSTMatrix[5] = -mSTMatrix[5];
            mSTMatrix[13] = 1.0f - mSTMatrix[13];
        }

        // (optional) clear to green so we can see if we're failing to set pixels
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            TLLogger.trace(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

    /**
     * Checks for EGL errors.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    ByteBuffer mBuffer = ByteBuffer.allocate(640 * 360 * 3/2);
    public void setdata(byte[] data){
        mBuffer.put(data);
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */
    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
        y.clear();
        u.clear();
        v.clear();
        y.put(ydata, 0, ydata.length);
        u.put(udata, 0, udata.length);
        v.put(vdata, 0, vdata.length);
    }
}
