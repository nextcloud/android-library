/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.activities;

import com.owncloud.android.lib.resources.activities.model.Activity;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetActivitiesRemoteOperationTest {

    @Test
    public void testParseActivities() {
        String activities = "{\"ocs\":{\"meta\":{\"status\":\"ok\",\"statuscode\":200," +
                "\"message\":\"OK\"},\"data\":[{\"activity_id\":1,\"app\":\"core\",\"type\":" +
                "\"security\",\"user\":\"test\",\"subject\":" +
                "\"You logged in success with your two factor device (U2F device)\"," +
                "\"subject_rich\":[\"\",[]],\"message\":\"\",\"message_rich\":[\"\",[]]," +
                "\"object_type\":\"\",\"object_id\":0,\"object_name\":\"\",\"objects\":[\"\"]," +
                "\"link\":\"\",\"icon\":\"https:\\/\\/localhost\\/nc\\/core\\/img" +
                "\\/actions\\/password.svg\",\"datetime\":\"2019-01-28T21:41:03+00:00\"," +
                "\"previews\":[]}]}}";

        GetActivitiesRemoteOperation sut = new GetActivitiesRemoteOperation();

        ArrayList<Activity> activityList = sut.parseResult(activities);

        assertTrue(activityList.size() > 0);
    }

    @Test
    public void testEmptyString() {
        String activities = "";

        GetActivitiesRemoteOperation sut = new GetActivitiesRemoteOperation();

        ArrayList<Activity> activityList = sut.parseResult(activities);

        assertEquals(0, activityList.size());
    }

    @Test
    public void testNotValidString() {
        String activities = "Wrong json syntax";

        GetActivitiesRemoteOperation sut = new GetActivitiesRemoteOperation();

        ArrayList<Activity> activityList = sut.parseResult(activities);

        assertEquals(0, activityList.size());
    }

    @Test
    public void testNullString() {
        String activities = null;

        GetActivitiesRemoteOperation sut = new GetActivitiesRemoteOperation();

        ArrayList<Activity> activityList = sut.parseResult(activities);

        assertEquals(0, activityList.size());
    }
}
