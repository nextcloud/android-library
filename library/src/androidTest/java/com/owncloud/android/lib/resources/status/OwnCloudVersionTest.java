/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2019 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

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

        assertNotSame(original, retrieved);
        assertEquals(original, retrieved);
    }
}
