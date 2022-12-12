package com.owncloud.android.lib.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OwnCloudBasicCredentialsTest {

    @Test
    public void notEquals() {

        OwnCloudBasicCredentials original = new OwnCloudBasicCredentials(
                "user",
                "pass",
                true
        );

        OwnCloudBasicCredentials[] differentCredentials = new OwnCloudBasicCredentials[] {
                new OwnCloudBasicCredentials(
                    "different user",
                    "pass",
                    true
                ),
                new OwnCloudBasicCredentials(
                        "user",
                        "different pass",
                        true
                ),
                new OwnCloudBasicCredentials(
                        "user",
                        "pass",
                        false
                ),
        };

        for (OwnCloudBasicCredentials modified : differentCredentials) {
            assertNotEquals(original, modified);
        }
    }

    @Test
    public void equals() {
        OwnCloudBasicCredentials original = new OwnCloudBasicCredentials(
                "user",
                "pass",
                true
        );

        OwnCloudBasicCredentials identical = new OwnCloudBasicCredentials(
                "user",
                "pass",
                true
        );

        assertEquals(original, identical);
    }

    @Test
    public void parcelableIsImplemented() {

        OwnCloudBasicCredentials original = new OwnCloudBasicCredentials(
                "username",
                "password",
                false
        );

        Intent intent = new Intent();
        intent.putExtra("credentials", original);

        OwnCloudBasicCredentials copy = intent.getParcelableExtra("credentials");

        assertEquals(original, copy);
    }
}
