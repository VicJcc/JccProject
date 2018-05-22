package jcc.example.com.launchmode.httputil;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by jincancan on 2018/5/19.
 * Description:
 */
public class JHttpUtil {

    private OkHttpClient clientWith30sTimeout = new OkHttpClient().newBuilder()
            .readTimeout(30, TimeUnit.SECONDS)
            .build();


}
