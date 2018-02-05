package example.com.img;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import example.com.learning.MyApp;

/**
 * Created by jincancan on 2018/1/5.
 * Description:
 */

public class TLImageUtil {

    public static final Comparator<TLPickPic> mSort = new TLPicSortByTime(TLPicSortByTime.SORT_TYPE_DESC);

    /**
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
     */
    public static void getImages(final IMGCallback callback) {
        //显示进度条
//        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

        new Thread(new Runnable() {

            @Override
            public void run() {

                ArrayList<TLPickPic> imgs = new ArrayList<>();

                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = MyApp.getIns().getContentResolver();

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

                    TLPickPic pic = new TLPickPic();
                    pic.setPath(path);
                    pic.setTime(time);
                    imgs.add(pic);
                }

                Collections.sort(imgs, mSort);

                //通知Handler扫描图片完成
//                mHandler.sendEmptyMessage(SCAN_OK);
                mCursor.close();

                if(callback != null){
                    callback.onGetImgs(imgs);
                }

            }
        }).start();

    }
}
