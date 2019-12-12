package com.owncloud.android.lib.resources.status;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OwnCloudVersionTest {

    @Test
    public void parcelableIsImplemented() {
        OwnCloudVersion original = new OwnCloudVersion(42);

        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeParcelable(original, 0);

        parcel.setDataPosition(0);
        OwnCloudVersion retrieved = parcel.readParcelable(OwnCloudVersion.class.getClassLoader());

        Assert.assertNotSame(original, retrieved);
        Assert.assertEquals(original, retrieved);
    }
}
