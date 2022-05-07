package com.nextcloud.common

import android.accounts.Account

interface User {
    val accountName: String

    @Deprecated("Temporary workaround")
    fun toPlatformAccount(): Account
}
