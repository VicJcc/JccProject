package jcc.example.com.keepalive;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
//    public static final String ACCOUNT_TYPE = JApp.getIns().getString(R.string.account_auth_type);    // TYPE必须与account_preferences.xml中的TYPE保持一致
//    private AccountManager mAccountManager;
//    private Context mContext;
    private static final int REQUEST_CODE = 1;
    private boolean bContinu = true;

    public static final String ACCOUNT_TYPE = JApp.getIns().getString(R.string.account_auth_type);    // TYPE必须与account_preferences.xml中的TYPE保持一致
    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccountManager = (AccountManager)getSystemService(ACCOUNT_SERVICE);

        boolean isAndroid6 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

        if (isAndroid6) {

            List<String> permissions = new ArrayList<>();
            //是否有读取手机状态的权限
            int resultPhoneState = ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
            int resultSDState = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SYNC_SETTINGS);
//            int resultReadState = ActivityCompat.checkSelfPermission(this, Manifest.permission.auth);
            if (resultPhoneState != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (resultSDState != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

//            if (resultReadState != PackageManager.PERMISSION_GRANTED) {
//                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//            }
            if(permissions.size() > 0) {
                //如果没有则请求权限
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), REQUEST_CODE);
                return;
            }
        }
        //非Android6.0的，直接运行
        setAccount();

//        mContext = this;

//        mAccountManager = (AccountManager)getSystemService(ACCOUNT_SERVICE);
//        Account[] accounts =mAccountManager.getAccountsByType(ACCOUNT_TYPE);   // 获取系统帐户列表中已添加的帐户是否存在我们的帐户，用TYPE做为标识
//        if(accounts.length > 0) {
//            Toast.makeText(this, "已添加当前登录的帐户",Toast.LENGTH_SHORT).show();
//            finish();
//        }
    }

    /**
     * 请求权限后的回调方法
     * @param requestCode 标识
     * @param permissions 申请的权限
     * @param grantResults 权限请求成功时返回的值
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                //判断权限是否获取到，如果没有，则停止安装6.0
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    bContinu = false;
                    finish();
                    break;
                }
            }
        }

        if(bContinu) {
            setAccount();
        }
    }

    private void setAccount(){

        Account account = new Account(getString(R.string.app_name), ACCOUNT_TYPE);
        mAccountManager.addAccountExplicitly(account, null, null);                          // 帐户密码和信息这里用null演示
        // 自动同步
        Bundle bundle= new Bundle();
        ContentResolver.setIsSyncable(account, JAccountProvider.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, JAccountProvider.AUTHORITY,true);
        ContentResolver.addPeriodicSync(account, JAccountProvider.AUTHORITY, bundle, 60);// 间隔时间为60秒

        Bundle bundle1= new Bundle();
        bundle1.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle1.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                account,
                JAccountProvider.AUTHORITY,
                bundle1);
    }
}
