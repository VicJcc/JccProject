package jcc.example.com.launchmode;

import android.content.Intent;

/**
 * Created by jincancan on 2018/5/9.
 * Description:
 */
public class JTestActivity5 extends MainActivity {

    @Override
    public void onclick() {
        Intent intent = new Intent(this, JTestActivity6.class);
        startActivity(intent);
    }
}
