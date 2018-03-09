package jcc.example.com.browseimg;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.jcc.common.JConfig;
import com.jcc.common.util.JMatrixUtil;

import java.util.ArrayList;

import jcc.example.com.browseimg.beans.JPhotosInfos;

public class MainActivity extends AppCompatActivity implements JAdapter.AdapterCallback{

    private ImageView mIvTest;
    private ImageView mIvTest1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JConfig.getInstence().setContext(this);
        Log.i("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
        }
        RecyclerView recyclerView = findViewById(R.id.rv_test);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new JAdapter(this, this));


        mIvTest = findViewById(R.id.iv_beauty);
        mIvTest1 = findViewById(R.id.iv_beauty_1);
        mIvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBrowse(0);
            }
        });

        mIvTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBrowse(1);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("MainActivity", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "onResume");


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("MainActivity", "onSaveInstanceState");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MainActivity", "onPause");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i("MainActivity", "onRestoreInstanceState");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("MainActivity", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("MainActivity", "onConfigurationChanged");
    }

    @Override
    public void onItemClick(float pressX, float pressY) {
//        JMovePicActivity.start(this, pressX, pressY);
    }


    private ArrayList<Rect> getPicRects(){
        ArrayList<Rect> rects = new ArrayList<>();
        Rect rect0 = JMatrixUtil.getDrawableBoundsInView(mIvTest);
        Rect rect1 = JMatrixUtil.getDrawableBoundsInView(mIvTest1);
        rects.add(rect0);
        rects.add(rect1);
        return rects;
    }

    private void startBrowse(int position){
        ArrayList<JPhotosInfos> infos = new ArrayList<>();
        ArrayList<Rect> rects = getPicRects();
        for (int i = 0; i < rects.size(); i++) {
            JPhotosInfos photosInfos = new JPhotosInfos();
            infos.add(photosInfos.build(rects.get(i)));
        }

        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(R.drawable.test);
        arrayList.add(R.drawable.long_test);
        JMovePicActivity.start(MainActivity.this, arrayList, position, infos);
    }

}
