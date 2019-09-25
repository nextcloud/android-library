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
package com.owncloud.android;

import android.util.Log;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.RemoveRemoteShareOperation;
import com.owncloud.android.lib.resources.shares.ShareType;
import com.owncloud.android.lib.resources.shares.UpdateRemoteShareOperation;
import com.owncloud.android.lib.testclient.TestActivity;

import java.io.File;
import java.util.Calendar;

/**
 * Class to test UpdateRemoteShareOperation
 * with public shares
 */
public class UpdatePublicShareTest extends RemoteTest {
    private static final String LOG_TAG = UpdatePublicShareTest.class.getCanonicalName();

    /* File to share and update.*/
    private static final String FILE_TO_SHARE = "/fileToShare.txt";

    /* Folder to share and update */
    private static final String FOLDER_TO_SHARE = "/folderToShare";

    // Data for tests 
    private static final String PASSWORD = "password";
    private static final String PASS_SPECIAL_CHARS = "p@ssw�rd";

    private String mFullPath2FileToShare;
    private String mFullPath2FolderToShare;

    private OCShare mShare;
    private OCShare mFolderShare;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Log.v(LOG_TAG, "Setting up the remote fixture...");

        // Upload the file
        mFullPath2FileToShare = mBaseFolderPath + FILE_TO_SHARE;

        File textFile = getFile(TestActivity.ASSETS__TEXT_FILE_NAME);
        RemoteOperationResult result = getActivity().uploadFile(textFile.getAbsolutePath(), mFullPath2FileToShare,
                "txt/plain", null);

        if (!result.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, result);
        }

        // Share the file with a public link
        result = getActivity().createShare(mFullPath2FileToShare, ShareType.PUBLIC_LINK, "", false, "",
                OCShare.READ_PERMISSION_FLAG);

        if (result.isSuccess()) {
            mShare = (OCShare) result.getData().get(0);
        } else {
            Utils.logAndThrow(LOG_TAG, result);
        }

        // Create the folder
        mFullPath2FolderToShare = mBaseFolderPath + FOLDER_TO_SHARE;
        result = getActivity().createFolder(mFullPath2FolderToShare, true);

        if (!result.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, result);
        }

        // Share the folder publicly via link
        result = getActivity().createShare(mFullPath2FolderToShare, ShareType.PUBLIC_LINK, "", false, "",
                OCShare.READ_PERMISSION_FLAG);

        if (result.isSuccess()) {
            mFolderShare = (OCShare) result.getData().get(0);
        } else {
            Utils.logAndThrow(LOG_TAG, result);
        }

        Log.v(LOG_TAG, "Remote fixtures created.");
    }

    public void testUpdatePublicShare() {
        Log.v(LOG_TAG, "testUpdatePublicShare in");

        if (mShare != null) {
            // successful tests
            // Update Share with password
            UpdateRemoteShareOperation updateShare = new UpdateRemoteShareOperation(mShare.getRemoteId());
            updateShare.setPassword(PASSWORD);
            RemoteOperationResult result = updateShare.execute(mClient);
            assertTrue(result.isSuccess());

            // Update Share with password with special characters
            updateShare = new UpdateRemoteShareOperation(mShare.getRemoteId());
            updateShare.setPassword(PASS_SPECIAL_CHARS);
            result = updateShare.execute(mClient);
            assertTrue(result.isSuccess());

            // Update Share with expiration date
            updateShare = new UpdateRemoteShareOperation(mShare.getRemoteId());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            long expirationDateInMillis = calendar.getTimeInMillis();
            updateShare.setExpirationDate(expirationDateInMillis);
            result = updateShare.execute(mClient);
            assertTrue(result.isSuccess());

            // Update the Folder Share with edit permission
            updateShare = new UpdateRemoteShareOperation(mFolderShare.getRemoteId());
            updateShare.setPublicUpload(true);
            result = updateShare.execute(mClient);
            assertTrue(result.isSuccess());

            // unsuccessful test
            // Update Share with expiration date in the past
            updateShare = new UpdateRemoteShareOperation(mShare.getRemoteId());
            calendar.set(Calendar.YEAR, 2014);
            expirationDateInMillis = calendar.getTimeInMillis();
            updateShare.setExpirationDate(expirationDateInMillis);
            result = updateShare.execute(mClient);
            assertFalse(result.isSuccess());

            // Try to update the file Share with edit permission
            updateShare = new UpdateRemoteShareOperation(mShare.getRemoteId());
            updateShare.setPublicUpload(true);
            result = updateShare.execute(mClient);
            assertFalse(result.isSuccess());

            // Unshare the file before the unsuccessful tests
            result = new RemoveRemoteShareOperation((int) mShare.getRemoteId()).execute(mClient);

            if (result.isSuccess()) {
                // Update Share with password on unknown share
                UpdateRemoteShareOperation updateNoShare = new UpdateRemoteShareOperation(mShare.getRemoteId());
                updateNoShare.setPassword(PASSWORD);
                result = updateNoShare.execute(mClient);
                assertFalse(result.isSuccess());

                // Update Share with expiration date on unknown share
                updateNoShare = new UpdateRemoteShareOperation(mShare.getRemoteId());
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                expirationDateInMillis = cal.getTimeInMillis();
                updateNoShare.setExpirationDate(expirationDateInMillis);
                result = updateNoShare.execute(mClient);
                assertFalse(result.isSuccess());
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        Log.v(LOG_TAG, "Deleting remote fixture...");

        if (mFullPath2FileToShare != null) {
            RemoteOperationResult removeResult = getActivity().removeFile(mFullPath2FileToShare);

            if (!removeResult.isSuccess()) {
                Utils.logAndThrow(LOG_TAG, removeResult);
            }
        }

        if (mFullPath2FolderToShare != null) {
            RemoteOperationResult removeResult = getActivity().removeFile(mFullPath2FolderToShare);

            if (!removeResult.isSuccess()) {
                Utils.logAndThrow(LOG_TAG, removeResult);
            }
        }
        
        super.tearDown();
        Log.v(LOG_TAG, "Remote fixture delete.");
    }
}
