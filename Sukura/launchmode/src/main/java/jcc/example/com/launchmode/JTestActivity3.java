package jcc.example.com.launchmode;

import android.content.Intent;

/**
 * Created by jincancan on 2018/5/9.
 * Description:
 */
public class JTestActivity3 extends MainActivity {

    private static final int REQUEST_CODE = 1000;

    @Override
    public void onclick() {
        Intent intent = new Intent(this, JTestActivity4.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){

        }
    }
}
