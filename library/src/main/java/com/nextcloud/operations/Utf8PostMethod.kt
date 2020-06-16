/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.nextcloud.operations

import okhttp3.Request
import okhttp3.RequestBody

/**
 * HTTP POST method that uses OkHttp with new NextcloudClient
 */
class Utf8PostMethod(
    uri: String,
    useOcsApiRequestHeader: Boolean,
    body: RequestBody?
) : PostMethod(uri, useOcsApiRequestHeader, body) {
    override fun applyType(temp: Request.Builder) {
        temp.addHeader("Content-Type", "charset=utf-8")
    }
}
