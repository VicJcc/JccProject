package jcc.example.com.browseimg;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jincancan on 2018/2/5.
 * Description:
 */

public class JAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private AdapterCallback mCallback;
    private float mPressX;
    private float mPressY;

    public JAdapter(Context context, AdapterCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = new TextView(mContext);
        textView.setPadding(0,100,0,100);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        return new TestHolder(textView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ((TextView)(holder.itemView)).setText(position + "");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCallback != null){
                    mCallback.onItemClick(mPressX, mPressY);
                }
            }
        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    mPressX = event.getRawX();
                    mPressY = event.getRawY();
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return 20;
    }

    static class TestHolder extends RecyclerView.ViewHolder{

        public TestHolder(View itemView) {
            super(itemView);
        }
    }

    interface AdapterCallback{
        void onItemClick(float pressX, float pressY);
    }

}
