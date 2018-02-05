package example.com.learning;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jcc.common.util.JDeviceUtils;
import com.jcc.common.util.JPermissionUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import example.com.emotionkeyboard.activity.ListViewBarEditActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TestMainActivity";

    private HandlerThread mThread;
    private Handler mHandler;
    private boolean hasPermission;
    private static final int REQUEST_CODE = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "TestActivity onCreate");
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_test).setOnClickListener(this);
        getPermission();

        Log.i(TAG, "Main " + Thread.currentThread().getName()+ "  " + Thread.currentThread().getId());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "TestActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "TestActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "TestActivity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "TestActivity onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "TestActivity onRestart");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "TestActivity onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThread.quit();
        Log.i(TAG, "TestActivity onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_test:
//                getImageFromCamera();
//                TLImageUtil.getImages();
                Intent intent = new Intent(this, ListViewBarEditActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void getPermission(){
        if (JDeviceUtils.isAndroid6()) {
            List<String> permissions = new ArrayList<>();
            //是否有读取手机状态的权限
            boolean resultPhoneState = JPermissionUtil.checkPermission(this, Manifest.permission.READ_PHONE_STATE);
            boolean resultSDState = JPermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            boolean resultReadState = JPermissionUtil.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            boolean recode = JPermissionUtil.checkPermission(this, Manifest.permission.RECORD_AUDIO);
            boolean camera = JPermissionUtil.checkPermission(this, Manifest.permission.CAMERA);

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

            if(!camera){
                permissions.add(Manifest.permission.CAMERA);
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


    private static final int REQUEST_CODE_PICK_IMAGE = 10000;
    private static final int REQUEST_CODE_CAPTURE_CAMERA = 10001;
    private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();

    /**
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
     */
    private void getImages() {
        //显示进度条
//        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

        new Thread(new Runnable() {

            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = MainActivity.this.getContentResolver();

                //只查询jpeg和png的图片
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);

                if(mCursor == null){
                    return;
                }

                while (mCursor.moveToNext()) {
                    //获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));

                    //获取该图片的父路径名
                    String parentName = new File(path).getParentFile().getName();
                    long time = new File(path).lastModified();

                    //根据父路径名将图片放入到mGruopMap中
                    if (!mGruopMap.containsKey(parentName)) {
                        List<String> chileList = new ArrayList<String>();
                        chileList.add(path);
                        mGruopMap.put(parentName, chileList);
                    } else {
                        mGruopMap.get(parentName).add(path);
                    }
                }

                //通知Handler扫描图片完成
//                mHandler.sendEmptyMessage(SCAN_OK);
                mCursor.close();
            }
        }).start();

    }
    protected void getImageFromCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMERA);
        }
        else {
            Toast.makeText(getApplicationContext(), "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_PICK_IMAGE){

        }else if(requestCode == REQUEST_CODE_CAPTURE_CAMERA){
//            Uri uri = data.getData();
//            if(uri == null){
//                //use bundle to get data
//                Bundle bundle = data.getExtras();
//                if (bundle != null) {
//                    Bitmap photo = (Bitmap) bundle.get("data"); //get bitmap
//                    //spath :生成图片取个名字和路径包含类型
//                    saveImage(photo, spath);
//                } else {
//                    Toast.makeText(getApplicationContext(), "err****", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }else{
//                //to do find the path of pic by uri
//            }
        }
    }

    public static boolean saveImage(Bitmap photo, String spath) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(spath, false));
            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
