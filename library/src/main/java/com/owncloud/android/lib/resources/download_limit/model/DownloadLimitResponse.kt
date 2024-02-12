/*
 *
 * Nextcloud Android client application
 *
 * @author TSI-mc
 * Copyright (C) 2023 TSI-mc
 * Copyright (C) 2023 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.owncloud.android.lib.resources.download_limit.model

/**
 * response from the Get download limit api
 *
 * <ocs>
 * <meta></meta>
 * <status>ok</status>
 * <statuscode>200</statuscode>
 * <message>OK</message>
 *
 * <data>
 * <limit>5</limit>
 * <count>0</count>
</data> *
</ocs> *
 */
data class DownloadLimitResponse(var limit: Long = 0, var count: Long = 0)