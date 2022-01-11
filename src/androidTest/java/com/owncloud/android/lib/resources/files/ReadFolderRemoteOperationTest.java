package com.owncloud.android.lib.resources.files;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ReadFolderRemoteOperationTest extends AbstractIT {
    @Test
    public void noFiles() {
        ReadFolderRemoteOperation sut = new ReadFolderRemoteOperation("/");
        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size()); // only root folder
    }

    @Test
    public void oneFolder() {
        String rootFolderPath = "/";
        String testFolderPath = "/testFolder/";

        // create folder
        assertTrue(new CreateFolderRemoteOperation(testFolderPath, true).execute(client).isSuccess());

        ReadFolderRemoteOperation sut = new ReadFolderRemoteOperation("/");
        RemoteOperationResult result = sut.execute(client);

        // test
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        RemoteFile rootFolder = (RemoteFile) result.getData().get(0);
        assertEquals(rootFolderPath, rootFolder.getRemotePath());
        
        RemoteFile testFolder = (RemoteFile) result.getData().get(1);
        assertEquals(testFolderPath, testFolder.getRemotePath());

        // clean up
        assertTrue(new RemoveFileRemoteOperation(testFolderPath).execute(client).isSuccess());
    }
}
