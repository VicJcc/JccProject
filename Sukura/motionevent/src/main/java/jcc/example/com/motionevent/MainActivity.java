package jcc.example.com.motionevent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JTestView jTestView = findViewById(R.id.v_jtest1);
        jTestView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean b;
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        b = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        b = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        b = false;
                        break;
                    default:
                        b = false;
                }
                Log.i(TAG, "onTouch: " + event.getAction() + " " + b);
                return b;
            }
        });

        String string = "AalsdhflkashdflkashdlfkhsadlkfhlkasdhfklashdflkahsdlkfhlasdkhflkashkldfhasdklfhlkashdflkhadsjkAAAAA";
        Bitmap bitmap = creatCodeBitmap(string, this);
        ImageView imageView = findViewById(R.id.iv_test);
        imageView.setImageBitmap(bitmap);
        TextView textView = findViewById(R.id.tv_test);
        textView.setText(string);
        textView.setSingleLine();
        textView.setDrawingCacheEnabled(true);
        textView.buildDrawingCache();
        textView.setEllipsize(TextUtils.TruncateAt.END);
        Bitmap bitmapCode = textView.getDrawingCache();
//        imageView.setImageBitmap(bitmapCode);
    }

    /**
     * 将文字 生成 文字图片 生成显示编码的Bitmap,目前这个方法是可用的
     *
     * @param contents
     * @param context
     * @return
     */
    public static Bitmap creatCodeBitmap(String contents , Context context) {
        float scale=context.getResources().getDisplayMetrics().scaledDensity;

        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
//        tv.setSingleLine(true);
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.measure(View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(50, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.setBackgroundColor(Color.TRANSPARENT);

        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        return bitmapCode;
    }
}
