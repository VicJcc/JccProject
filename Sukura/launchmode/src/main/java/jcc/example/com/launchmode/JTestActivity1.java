package jcc.example.com.launchmode;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

/**
 * Created by jincancan on 2018/5/9.
 * Description:
 */
public class JTestActivity1 extends MainActivity {

    private Context mContext = this;
    private Resources mResources = mContext.getResources();


    @Override
    public void onclick() {
        Intent intent = new Intent(this, JTestActivity2.class);
        startActivity(intent);
    }
}
