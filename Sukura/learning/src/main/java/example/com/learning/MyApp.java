package example.com.learning;

import android.app.Application;

/**
 * Created by jincancan on 2017/12/19.
 * Description:
 */

public class MyApp extends Application {
    private static MyApp instance;

    public static MyApp getIns(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
