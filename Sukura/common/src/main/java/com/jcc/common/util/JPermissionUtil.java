package com.jcc.common.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Created by alanchen on 15/5/8.
 */
public class JPermissionUtil {
    /**
     *
     * @param context
     * @param permission e.g:  Manifest.permission.READ_PHONE_STATE
     * @return
     */
    public static boolean checkPermission(@NonNull Context context, @NonNull String permission){
        int result = ActivityCompat.checkSelfPermission(context.getApplicationContext(), permission);
        return  result == PackageManager.PERMISSION_GRANTED;
    }

}
