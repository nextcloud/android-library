/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2018 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.ocs;

import com.google.gson.annotations.SerializedName;

/**
 * Wrapper for server OCS response
 *
 * @author Bartosz Przybylski
 */
public class OCSResponse<T extends Object> {

    @SerializedName("data")
    public T data;

    @SerializedName("meta")
    public OCSMeta meta;

    public T getData() {
        return this.data;
    }

    public OCSMeta getMeta() {
        return this.meta;
    }
}
