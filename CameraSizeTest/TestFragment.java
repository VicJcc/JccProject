package com.taole.myapplication.CameraSizeTest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taole.myapplication.R;

/**
 * Created by jincancan on 16/5/26.
 */
public class TestFragment extends Fragment {

    private View mRootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.test_fragment, container, false);
        return mRootView;
    }
}
