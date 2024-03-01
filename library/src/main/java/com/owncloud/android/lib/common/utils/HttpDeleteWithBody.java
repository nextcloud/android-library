/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.utils;

import org.apache.commons.httpclient.methods.Utf8PostMethod;


public class HttpDeleteWithBody extends Utf8PostMethod {

    public HttpDeleteWithBody(String url) {
        super(url);
    }

    @Override
    public String getName() {
        return "DELETE";
    }

}
