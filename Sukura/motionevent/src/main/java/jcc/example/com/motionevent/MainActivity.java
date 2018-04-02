package jcc.example.com.motionevent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JTestView jTestView = findViewById(R.id.v_jtest1);
//        jTestView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                boolean b;
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        b = true;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        b = false;
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        b = false;
//                        break;
//                    default:
//                        b = false;
//                }
//                Log.i(TAG, "onTouch: " + event.getAction() + " " + b);
//                return b;
//            }
//        });


    }
}
