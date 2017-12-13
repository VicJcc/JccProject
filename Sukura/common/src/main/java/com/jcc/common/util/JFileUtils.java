package com.jcc.common.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;


public class JFileUtils {
    /***
     * SDCARD 是否装载sdcard
     * @return
     */
    public static boolean isSDCardMounted(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getFile(Context context, String dirs, String name, boolean out){
        File rootDir;
        if(isSDCardMounted()){
            if(out) {
                rootDir = Environment.getExternalStorageDirectory();
            }else{
                rootDir = context.getExternalFilesDir(null);
            }
        }else{
            rootDir = context.getFilesDir();
        }

        File fileDir = new File(rootDir + File.separator + dirs + File.separator);
        if(!fileDir.exists() && !fileDir.isDirectory()){
            fileDir.mkdirs();
        }

        File file = new File(fileDir, name);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}
