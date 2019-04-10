package com.owncloud.android.lib.resources.files;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GetFavoritesRemoteOperationTest extends AbstractIT {
    @Test
    public void noFavorites() {
        GetFavoritesRemoteOperation sut = new GetFavoritesRemoteOperation();
        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    public void oneFavorite() {
        String path = "/testFolder";

        // create folder, make it favorite
        new CreateFolderRemoteOperation(path, true).execute(client);
        assertTrue(new ToggleFavoriteRemoteOperation(true, path).execute(client).isSuccess());

        GetFavoritesRemoteOperation sut = new GetFavoritesRemoteOperation();
        RemoteOperationResult result = sut.execute(client);
        
        // test
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        RemoteFile remoteFile = (RemoteFile) result.getData().get(0); 
        assertEquals(path, remoteFile.getRemotePath());

        // clean up
        assertTrue(new ToggleFavoriteRemoteOperation(false, path).execute(client).isSuccess());
        assertTrue(new RemoveFileRemoteOperation(path).execute(client).isSuccess());
    }
}
