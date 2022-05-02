package com.nextcloud.common;

import android.accounts.Account;

public interface User {
    @Deprecated
    Account toPlatformAccount();
    String getAccountName();
}
