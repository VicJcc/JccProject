package jcc.example.com.launchmode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvInfo = findViewById(R.id.tv_info);
        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclick();
            }
        });
        setInfo();
    }

    public void onclick(){
        Intent intent = new Intent(this, JTestActivity1.class);
        int[] ints = {19,28,48,10,98,68,16,20,30,1,45,657,234,9};
        TLSort.fastSort(ints, 0, ints.length - 1);
        startActivity(intent);
    }

    public void setInfo(){
        int id = getTaskId();
        String name = getLocalClassName();

        mTvInfo.setText(name + "  " + id);
    }
}
