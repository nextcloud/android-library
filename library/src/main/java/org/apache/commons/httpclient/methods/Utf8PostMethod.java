/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package org.apache.commons.httpclient.methods;

public class Utf8PostMethod extends PostMethod {
    public Utf8PostMethod(String uri) {
        super(uri);
        getParams().setContentCharset("utf-8");
    }
}
