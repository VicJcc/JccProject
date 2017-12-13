package module;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.jcc.common.JBaseActivity;
import com.jcc.common.util.JDeviceUtils;
import com.jcc.common.util.JPermissionUtil;
import com.jcc.soundrecognize.AudioFileFunc;
import com.jcc.soundrecognize.AudioRecordFunc;
import com.jcc.soundrecognize.JsonParser;
import com.jcc.soundrecognize.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by jincancan on 2017/12/13.
 * Description:
 */

public class JSoundActivity extends JBaseActivity implements InitListener {

    private static final int REQUEST_CODE = 10000;
    private SpeechRecognizer mIat;// 语音识别
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>(); // 语音识别结果

    private TextView mTvTest;// 录音按钮
    private TextView mTvPath;// 录音文件路径
    private TextView mTvTrans;// 翻译
    private boolean bRecoding;
    private Toast mToast;
    private boolean hasPermission;

    @Override
    public int layoutID() {
        return R.layout.activity_sound;
    }

    @Override
    public void initData() {
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5a30c82f");// 语音识别
        mIat = SpeechRecognizer.createRecognizer(mContext, this);
        mToast = new Toast(mContext);
        getPermission();
    }

    @Override
    public void initView() {
        mTvTest = findViewById(R.id.tv_test_sound);
        mTvPath = findViewById(R.id.tv_path);
        mTvTrans = findViewById(R.id.tv_translate);
        mTvTest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if(hasPermission) {
                        startRecode();
                        mTvTest.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }else{
                        mToast.makeText(mContext, "录音权限", Toast.LENGTH_LONG).show();
                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP ||
                        motionEvent.getAction() == MotionEvent.ACTION_CANCEL ||
                        motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE){
                    stopRecode();
                    mTvTest.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                return true;
            }
        });
    }

    @Override
    public void onInit(int code) {
        if (code != ErrorCode.SUCCESS) {
            mToast.cancel();
            mToast.makeText(mContext, "初始化失败，错误码：" + code, Toast.LENGTH_LONG).show();
        }
    }

    private void startRecode(){
        if(bRecoding){
            mToast.cancel();
            mToast.makeText(mContext, "Recoding!!", Toast.LENGTH_LONG).show();
            return;
        }
        bRecoding = true;
        AudioRecordFunc.getInstance().startRecordAndFile(mContext);
    }

    private void stopRecode(){
        AudioRecordFunc.getInstance().stopRecordAndFile();
        translateAudio();
    }

    private void translateAudio(){
        mIatResults.clear();
        // 设置参数
        setParam();
        // 设置音频来源为外部文件
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
        mIat.setParameter(SpeechConstant.SAMPLE_RATE, "16000");
        mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, AudioFileFunc.getWavFilePath(mContext));
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            mToast.cancel();
            mToast.makeText(mContext, "识别失败,错误码：" + ret, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "60000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "3000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT,  "1");
    }

    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
//            if(mCallback != null){
//                mCallback.onShowSelfVoice(mAudioFile.getPath());
//            }
//            uploadFile();
            bRecoding = false;
            mIatResults.clear();
            mToast.cancel();
            mToast.makeText(mContext, "识别失败：" + error.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
            if(isLast) {

                StringBuffer resultBuffer = new StringBuffer();
                for (String key : mIatResults.keySet()) {
                    resultBuffer.append(mIatResults.get(key));
                }

                mTvTrans.setText(resultBuffer.toString());
                mTvPath.setText(AudioFileFunc.getWavFilePath(mContext));
                bRecoding = false;
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

    }

    private void getPermission(){
        if (JDeviceUtils.isAndroid6()) {
            List<String> permissions = new ArrayList<>();
            //是否有读取手机状态的权限
            boolean resultPhoneState = JPermissionUtil.checkPermission(this, Manifest.permission.READ_PHONE_STATE);
            boolean resultSDState = JPermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            boolean resultReadState = JPermissionUtil.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            boolean recode = JPermissionUtil.checkPermission(this, Manifest.permission.RECORD_AUDIO);

            if (!resultPhoneState){
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (!resultSDState) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!resultReadState) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if(!recode){
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if(permissions.size() > 0) {
                //如果没有则请求权限
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), REQUEST_CODE);
            }else{
                hasPermission = true;
            }
        }else{
            hasPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            boolean agree = true;
            for (int i = 0; i < grantResults.length; i++) {
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    agree = false;
                    break;
                }
            }
            hasPermission = agree;
        }
    }
}
