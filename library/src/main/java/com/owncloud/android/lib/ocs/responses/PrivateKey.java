/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.ocs.responses;

import com.google.gson.annotations.SerializedName;

/**
 * @author Bartosz Przybylski
 */
public class PrivateKey {

    @SerializedName("private-key")
    public String key;

    public String getKey() {
        return key;
    }
}
