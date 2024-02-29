/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.lib.resources.activities.model.Activity;

import org.junit.Test;

import java.util.ArrayList;

public class GetActivitiesRemoteOperationTest {

    @Test
    public void testParseActivities() {
        String activities = "{\"ocs\":{\"meta\":{\"status\":\"ok\",\"statuscode\":200," +
                "\"message\":\"OK\"},\"data\":[{\"activity_id\":1,\"app\":\"core\",\"type\":" +
                "\"security\",\"user\":\"test\",\"affecteduser\":\"Test User\",\"subject\":" +
                "\"You logged in success with your two factor device (U2F device)\"," +
                "\"subject_rich\":[\"\",[]],\"message\":\"\",\"message_rich\":[\"\",[]]," +
                "\"object_type\":\"\",\"object_id\":0,\"object_name\":\"\",\"objects\":[\"\"]," +
                "\"link\":\"\",\"icon\":\"https:\\/\\/localhost\\/nc\\/core\\/img" +
                "\\/actions\\/password.svg\",\"datetime\":\"2019-01-28T21:41:03+00:00\"," +
                "\"previews\":[]}]}}";

        GetActivitiesRemoteOperation sut = new GetActivitiesRemoteOperation();

        ArrayList<Activity> activityList = sut.parseResult(activities);

        assertTrue(activityList.size() > 0);

        Activity firstItem = activityList.get(0);
        assertEquals("test", firstItem.getUser());
        assertEquals("Test User", firstItem.getAffectedUser());
        assertEquals("You logged in success with your two factor device (U2F device)", firstItem.getSubject());
        assertEquals("0", firstItem.getObjectId());
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
