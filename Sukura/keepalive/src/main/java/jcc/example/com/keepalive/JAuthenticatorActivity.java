package jcc.example.com.keepalive;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by jincancan on 2018/3/9.
 * Description:
 */

public class JAuthenticatorActivity extends Activity {
    public static final String ACCOUNT_TYPE = JApp.getIns().getString(R.string.account_auth_type);    // TYPE必须与account_preferences.xml中的TYPE保持一致
    private AccountManager mAccountManager;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mContext = this;

        mAccountManager = (AccountManager)getSystemService(ACCOUNT_SERVICE);
        Account[] accounts =mAccountManager.getAccountsByType(ACCOUNT_TYPE);   // 获取系统帐户列表中已添加的帐户是否存在我们的帐户，用TYPE做为标识
        if(accounts.length > 0) {
            Toast.makeText(this, "已添加当前登录的帐户",Toast.LENGTH_SHORT).show();
            finish();
        }

        Button btnAddAccount = (Button)findViewById(R.id.btn_add_account);
        btnAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = new Account(getString(R.string.app_name), ACCOUNT_TYPE);
                mAccountManager.addAccountExplicitly(account, null, null);                          // 帐户密码和信息这里用null演示
                // 自动同步
                Bundle bundle= new Bundle();
                ContentResolver.setIsSyncable(account, JAccountProvider.AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(account, JAccountProvider.AUTHORITY,true);
                ContentResolver.addPeriodicSync(account, JAccountProvider.AUTHORITY, bundle, 60);    // 间隔时间为60秒

//                // 手动同步
//                ContentResolver.requestSync(account, JAccountProvider.AUTHORITY, bundle);
                finish();
            }
        });
    }
}
