package jcc.example.com.launchmode;

import android.content.Intent;

/**
 * Created by jincancan on 2018/5/9.
 * Description:
 */
public class JTestActivity6 extends MainActivity {

    @Override
    public void onclick() {
        Intent intent = new Intent(this, JTestActivity2.class);
        startActivity(intent);
    }
}
