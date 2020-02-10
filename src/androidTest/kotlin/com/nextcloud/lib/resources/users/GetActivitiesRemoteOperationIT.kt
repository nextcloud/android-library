/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2019 Tobias Kaminsky
 * Copyright (C) 2019 Nextcloud GmbH
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

package com.nextcloud.lib.resources.users

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.activities.GetActivitiesRemoteOperation
import com.owncloud.android.lib.resources.activities.model.Activity
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class GetActivitiesRemoteOperationIT : AbstractIT() {
    @Test
    fun getActivities() {
        // set-up, create a folder so there is an activity
        assertTrue(CreateFolderRemoteOperation("/test/123/1", true).execute(client).isSuccess)

        val result = nextcloudClient.execute(GetActivitiesRemoteOperation())
        assertTrue(result.isSuccess)

        val activities = result.data[0] as ArrayList<Activity>
        val lastGiven = result.data[1] as Integer;

        assertTrue(activities.isNotEmpty())
        assertTrue(lastGiven > 0)
    }
}
