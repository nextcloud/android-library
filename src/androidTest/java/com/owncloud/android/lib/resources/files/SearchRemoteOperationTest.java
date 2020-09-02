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
package com.owncloud.android.lib.resources.files;

import android.net.Uri;
import android.os.Bundle;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.junit.Test;

import java.io.IOException;

import androidx.test.platform.app.InstrumentationRegistry;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SearchRemoteOperationTest extends AbstractIT {
    @Test
    public void testSearchByFileIdEmpty() {
        SearchRemoteOperation sut = new SearchRemoteOperation("123123",
                                                              SearchRemoteOperation.SearchType.FILE_ID_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().size());
    }

    @Test
    public void testSearchByFileIdSuccess() {
        assertTrue(new CreateFolderRemoteOperation("/test/", true).execute(client).isSuccess());

        RemoteOperationResult readFile = new ReadFileRemoteOperation("/test/").execute(client);
        assertTrue(readFile.isSuccess());

        RemoteFile remoteFile = ((RemoteFile) readFile.getSingleData());
        SearchRemoteOperation sut = new SearchRemoteOperation(remoteFile.getLocalId(),
                                                              SearchRemoteOperation.SearchType.FILE_ID_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("/test/", ((RemoteFile) result.getData().get(0)).getRemotePath());
    }

    @Test
    public void testFileSearchEmpty() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "image/jpg", "123")
                               .execute(client).isSuccess());
        }

        SearchRemoteOperation sut = new SearchRemoteOperation("123123",
                                                              SearchRemoteOperation.SearchType.FILE_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().size());
    }

    @Test
    public void testFileSearchEverything() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "image/jpg", "123")
                               .execute(client).isSuccess());
        }

        SearchRemoteOperation sut = new SearchRemoteOperation("", SearchRemoteOperation.SearchType.FILE_SEARCH, false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(10, result.getData().size());
    }

    @Test
    public void testFileSearchSuccess() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "image/jpg", "123")
                               .execute(client).isSuccess());
        }

        SearchRemoteOperation sut = new SearchRemoteOperation("image5",
                                                              SearchRemoteOperation.SearchType.FILE_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertEquals(1, result.getData().size());
        RemoteFile remoteFile = (RemoteFile) result.getData().get(0);
        assertEquals("/image5.jpg", remoteFile.getRemotePath());
    }

    @Test
    public void noFavorites() {
        SearchRemoteOperation sut = new SearchRemoteOperation("",
                                                              SearchRemoteOperation.SearchType.FAVORITE_SEARCH,
                                                              false);
        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    public void oneFavorite() {
        String path = "/testFolder/";

        // create folder, make it favorite
        new CreateFolderRemoteOperation(path, true).execute(client);
        assertTrue(new ToggleFavoriteRemoteOperation(true, path).execute(client).isSuccess());

        SearchRemoteOperation sut = new SearchRemoteOperation("",
                                                              SearchRemoteOperation.SearchType.FAVORITE_SEARCH,
                                                              false);
        RemoteOperationResult result = sut.execute(client);

        // test
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        RemoteFile remoteFile = (RemoteFile) result.getData().get(0);
        assertEquals(path, remoteFile.getRemotePath());
    }

    /**
     * shows just all files, but sorted by date
     */
    @Test
    public void testRecentlyModifiedSearch() throws IOException {
        long now = System.currentTimeMillis() / 1000;
        String filePath = createFile("image");
        assertTrue(new UploadFileRemoteOperation(filePath, "/image.jpg", "image/jpg", String.valueOf(now - 50))
                           .execute(client).isSuccess());

        String videoPath = createFile("video");
        assertTrue(new UploadFileRemoteOperation(videoPath, "/video.mp4", "video/mpeg", String.valueOf(now - 10))
                           .execute(client).isSuccess());

        String pdfPath = createFile("pdf");
        assertTrue(new UploadFileRemoteOperation(pdfPath, "/pdf.pdf", "application/pdf", String.valueOf(now - 30))
                           .execute(client).isSuccess());

        String oldPath = createFile("pdf");
        assertTrue(new UploadFileRemoteOperation(oldPath, "/old.pdf", "application/pdf", "1")
                           .execute(client).isSuccess());

        SearchRemoteOperation sut = new SearchRemoteOperation("",
                                                              SearchRemoteOperation.SearchType.RECENTLY_MODIFIED_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getData().size());

        assertEquals("/video.mp4", ((RemoteFile) result.getData().get(0)).getRemotePath());
        assertEquals("/pdf.pdf", ((RemoteFile) result.getData().get(1)).getRemotePath());
        assertEquals("/image.jpg", ((RemoteFile) result.getData().get(2)).getRemotePath());
    }

    @Test
    public void testPhotoSearchNoFiles() {
        SearchRemoteOperation sut = new SearchRemoteOperation("image/%", SearchRemoteOperation.SearchType.PHOTO_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    public void testPhotoSearch() throws IOException {
        String imagePath = createFile("image");
        assertTrue(new UploadFileRemoteOperation(imagePath, "/image.jpg", "image/jpg", "123")
                           .execute(client).isSuccess());

        String filePath = createFile("pdf");
        assertTrue(new UploadFileRemoteOperation(filePath, "/pdf.pdf", "application/pdf", "123")
                           .execute(client).isSuccess());

        SearchRemoteOperation sut = new SearchRemoteOperation("image/%", SearchRemoteOperation.SearchType.PHOTO_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    public void testPhotoSearchLimit() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath,
                                                     remotePath,
                                                     "image/jpg",
                                                     String.valueOf(100000 + i * 10000))
                               .execute(client).isSuccess());
        }

        // get all
        SearchRemoteOperation sut = new SearchRemoteOperation("image/%",
                                                              SearchRemoteOperation.SearchType.PHOTO_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(10, result.getData().size());

        // limit to 5
        sut.setLimit(5);

        result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(5, result.getData().size());
    }

    @Test
    public void testPhotoSearchTimestamps() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "image/jpg", String.valueOf(i))
                               .execute(client).isSuccess());
        }

        SearchRemoteOperation sut = new SearchRemoteOperation("image/%", SearchRemoteOperation.SearchType.PHOTO_SEARCH,
                                                              false);

        // get all
        RemoteOperationResult result = sut.execute(client);
        assertEquals(10, result.getData().size());

        // limit to timestamp 5
        sut.setTimestamp(5);

        result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(5, result.getData().size());
    }

    @Test
    public void testPhotoSearchLimitAndTimestamp() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath,
                                                     remotePath,
                                                     "image/jpg",
                                                     String.valueOf(100000 + i * 10000))
                               .execute(client).isSuccess());
        }

        // get all
        SearchRemoteOperation sut = new SearchRemoteOperation("image/%",
                                                              SearchRemoteOperation.SearchType.PHOTO_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(10, result.getData().size());

        // limit to 5
        sut.setLimit(5);
        sut.setTimestamp(120000);

        result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    @Test
    public void testGallerySearch() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "image/jpg", "123")
                               .execute(client).isSuccess());
        }
        String videoPath = createFile("video");
        assertTrue(new UploadFileRemoteOperation(videoPath, "/video.mp4", "video/mpeg", "123")
                           .execute(client).isSuccess());

        String filePath = createFile("pdf");
        assertTrue(new UploadFileRemoteOperation(filePath, "/pdf.pdf", "application/pdf", "123")
                           .execute(client).isSuccess());

        SearchRemoteOperation sut = new SearchRemoteOperation("image/%", SearchRemoteOperation.SearchType.GALLERY_SEARCH,
                                                              false);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(11, result.getData().size());
    }

    @Test
    public void showOnlyFolders() throws IOException {
        for (int i = 0; i < 10; i++) {
            String filePath = createFile("image" + i);
            String remotePath = "/image" + i + ".jpg";
            assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "image/jpg", "123")
                               .execute(client).isSuccess());
        }

        SearchRemoteOperation sut = new SearchRemoteOperation("", SearchRemoteOperation.SearchType.FILE_SEARCH,
                                                              true);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());

        assertTrue(new CreateFolderRemoteOperation("/folder/", false).execute(client).isSuccess());

        result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("/folder/", ((RemoteFile) result.getData().get(0)).getRemotePath());
    }

    @Test
    public void testSearchWithAtInUsername() {
        Bundle arguments = InstrumentationRegistry.getArguments();
        Uri url = Uri.parse(arguments.getString("TEST_SERVER_URL"));

        OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(url, context, true);
        client.setCredentials(new OwnCloudBasicCredentials("test@test", "test"));
        client.setUserId("test@test"); // for test same as userId

        SearchRemoteOperation sut = new SearchRemoteOperation("",
                                                              SearchRemoteOperation.SearchType.FILE_SEARCH,
                                                              true);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().size());
    }
}
