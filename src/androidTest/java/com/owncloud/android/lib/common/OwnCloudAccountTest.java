package com.owncloud.android.lib.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OwnCloudAccountTest {

    @Test
    public void parcelableIsImplemented() {
        Uri uri = Uri.parse("https://nextcloud.localhost.localdomain");
        OwnCloudCredentials credentials = new OwnCloudBasicCredentials(
                "user",
                "pass",
                true
        );
        OwnCloudAccount original = new OwnCloudAccount(uri, credentials);
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeParcelable(original, 0);
        parcel.setDataPosition(0);
        OwnCloudAccount retrieved = parcel.readParcelable(OwnCloudAccount.class.getClassLoader());
        Assert.assertNotSame(original, retrieved);
        Assert.assertEquals(original, retrieved);
    }
}
