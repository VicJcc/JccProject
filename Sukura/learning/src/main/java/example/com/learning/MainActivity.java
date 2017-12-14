package example.com.learning;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private HandlerThread mThread;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mThread = new HandlerThread("Test HandlerThread");
        mThread.start();

        mHandler = new Handler(mThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG,"Msg " + Thread.currentThread().getName() + "  " + Thread.currentThread().getId());
            }
        };
        findViewById(R.id.tv_test).setOnClickListener(this);

        Log.i(TAG, "Main " + Thread.currentThread().getName()+ "  " + Thread.currentThread().getId());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_test:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Runnable " + Thread.currentThread().getName() + "  " + Thread.currentThread().getId());
                    }
                });
                mHandler.sendEmptyMessage(0);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThread.quit();
    }
}
