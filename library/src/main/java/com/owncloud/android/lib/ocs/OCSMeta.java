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
 * A meta class which is a part of OCS response from server
 *
 * @author Bartosz Przybylski
 */
public class OCSMeta {
    @SerializedName("status")
    public String status;
    @SerializedName("statuscode")
    public int statusCode;
    @SerializedName("message")
    public String message;

    public String getStatus() {
        return this.status;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getMessage() {
        return this.message;
    }
    // TODO(bp): add paging information
}
