package jcc.example.com.keepalive;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by jincancan on 2018/3/9.
 * Description:
 */

public class JAuthenticationService extends Service {
    private JAuthenticationService.AccountAuthenticator mAuthenticator;

    private JAuthenticationService.AccountAuthenticator getAuthenticator() {
        if(mAuthenticator == null)
            mAuthenticator = new JAuthenticationService.AccountAuthenticator(this);
        return mAuthenticator;
    }

    @Override
    public void onCreate() {
        mAuthenticator = new JAuthenticationService.AccountAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return getAuthenticator().getIBinder();
    }

    class AccountAuthenticator extends AbstractAccountAuthenticator {
        private Context mContext;
        private AccountManager accountManager;

        public AccountAuthenticator(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
            final Bundle bundle = new Bundle();
            final Intent intent = new Intent(mContext, JAuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,response);
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws
        NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,String authTokenType, Bundle options) throws NetworkErrorException {
            // 认证 示例代码
            String authToken = accountManager.peekAuthToken(account, getString(R.string.account_auth_type));
            //if not, might be expired, register again
            if (TextUtils.isEmpty(authToken)) {
                final String password = accountManager.getPassword(account);
                if (password != null) {
                    //get new token
                    authToken = account.name + password;
                }
            }
            //without password, need to sign again
            final Bundle bundle = new Bundle();
            if (!TextUtils.isEmpty(authToken)) {
                bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return bundle;
            }

            //no account data at all, need to do a sign
            final Intent intent = new Intent(mContext, JAuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,String[] features) throws NetworkErrorException {
            return null;
        }
    }
}
