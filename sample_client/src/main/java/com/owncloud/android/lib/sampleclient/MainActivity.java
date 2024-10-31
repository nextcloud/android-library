/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.sampleclient;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity implements OnRemoteOperationListener, OnDatatransferProgressListener {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private Handler mHandler;

    private OwnCloudClient mClient;

    private FilesArrayAdapter mFilesAdapter;

    private View mFrame;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHandler = new Handler();

        Uri serverUri = Uri.parse(getString(R.string.server_base_url));
        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, this, true);
        mClient.setCredentials(
                OwnCloudCredentialsFactory.newBasicCredentials(
                        getString(R.string.username),
                        getString(R.string.password)
                )
        );
        mClient.setUserId(getString(R.string.username));

        mFilesAdapter = new FilesArrayAdapter(this, R.layout.file_in_list);
        ((ListView) findViewById(R.id.list_view)).setAdapter(mFilesAdapter);

        // TODO move to background thread or task
        AssetManager assets = getAssets();
        try {
            String sampleFileName = getString(R.string.sample_file_name);
            File upFolder = new File(getCacheDir(), getString(R.string.upload_folder_path));
            upFolder.mkdir();
            File upFile = new File(upFolder, sampleFileName);
            FileOutputStream fos = new FileOutputStream(upFile);
            InputStream is = assets.open(sampleFileName);
            int count = 0;
            byte[] buffer = new byte[1024];
            while ((count = is.read(buffer, 0, buffer.length)) >= 0) {
                fos.write(buffer, 0, count);
            }
            is.close();
            fos.close();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_copying_sample_file, Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.error_copying_sample_file), e);
        }

        mFrame = findViewById(R.id.frame);
    }


    @Override
    public void onDestroy() {
        File upFolder = new File(getCacheDir(), getString(R.string.upload_folder_path));
        File upFile = upFolder.listFiles()[0];
        upFile.delete();
        upFolder.delete();
        super.onDestroy();
    }


    public void onClickHandler(View button) {
        switch (button.getId()) {
            case R.id.button_refresh:
                startRefresh();
                break;
            case R.id.button_upload:
                startUpload();
                break;
            case R.id.button_delete_remote:
                startRemoteDeletion();
                break;
            case R.id.button_download:
                startDownload();
                break;
            case R.id.button_delete_local:
                startLocalDeletion();
                break;
            default:
                Toast.makeText(this, R.string.youre_doing_it_wrong, Toast.LENGTH_SHORT).show();
        }
    }

    private void startRefresh() {
        ReadFolderRemoteOperation refreshOperation = new ReadFolderRemoteOperation(FileUtils.PATH_SEPARATOR);
        refreshOperation.execute(mClient, this, mHandler);
    }

    private void startUpload() {
        File upFolder = new File(getCacheDir(), getString(R.string.upload_folder_path));
        File fileToUpload = upFolder.listFiles()[0];
        String remotePath = FileUtils.PATH_SEPARATOR + fileToUpload.getName();
        String mimeType = getString(R.string.sample_file_mimetype);

        // Get the last modification date of the file from the file system
        long timeStamp = fileToUpload.lastModified() / 1000;

        UploadFileRemoteOperation uploadOperation =
                new UploadFileRemoteOperation(
                        fileToUpload.getAbsolutePath(),
                        remotePath,
                        mimeType,
                        timeStamp
                );
        uploadOperation.addDataTransferProgressListener(this);
        uploadOperation.execute(mClient, this, mHandler);
    }

    private void startRemoteDeletion() {
        File upFolder = new File(getCacheDir(), getString(R.string.upload_folder_path));
        File fileToUpload = upFolder.listFiles()[0];
        String remotePath = FileUtils.PATH_SEPARATOR + fileToUpload.getName();
        RemoveFileRemoteOperation removeOperation = new RemoveFileRemoteOperation(remotePath);
        removeOperation.execute(mClient, this, mHandler);
    }

    private void startDownload() {
        File downFolder = new File(getCacheDir(), getString(R.string.download_folder_path));
        downFolder.mkdir();
        File upFolder = new File(getCacheDir(), getString(R.string.upload_folder_path));
        File fileToUpload = upFolder.listFiles()[0];
        String remotePath = FileUtils.PATH_SEPARATOR + fileToUpload.getName();
        DownloadFileRemoteOperation downloadOperation = new DownloadFileRemoteOperation(remotePath, downFolder.getAbsolutePath());
        downloadOperation.addDatatransferProgressListener(this);
        downloadOperation.execute(mClient, this, mHandler);
    }

    @SuppressWarnings("deprecation")
    private void startLocalDeletion() {
        File downFolder = new File(getCacheDir(), getString(R.string.download_folder_path));
        File downloadedFile = downFolder.listFiles()[0];
        if (!downloadedFile.delete() && downloadedFile.exists()) {
            Toast.makeText(this, R.string.error_deleting_local_file, Toast.LENGTH_SHORT).show();
        } else {
            ((TextView) findViewById(R.id.download_progress)).setText("0%");
            findViewById(R.id.frame).setBackgroundDrawable(null);
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (!result.isSuccess()) {
            Toast.makeText(this, R.string.todo_operation_finished_in_fail, Toast.LENGTH_SHORT).show();
            Log.e(TAG, result.getLogMessage(), result.getException());

        } else if (operation instanceof ReadFolderRemoteOperation) {
            onSuccessfulRefresh(result);

        } else if (operation instanceof UploadFileRemoteOperation) {
            onSuccessfulUpload((UploadFileRemoteOperation) operation, result);

        } else if (operation instanceof RemoveFileRemoteOperation) {
            onSuccessfulRemoteDeletion((RemoveFileRemoteOperation) operation, result);

        } else if (operation instanceof DownloadFileRemoteOperation) {
            onSuccessfulDownload((DownloadFileRemoteOperation) operation, result);

        } else {
            Toast.makeText(this, R.string.todo_operation_finished_in_success, Toast.LENGTH_SHORT).show();
        }
    }

	private void onSuccessfulRefresh(RemoteOperationResult<List<RemoteFile>> result) {
		mFilesAdapter.clear();
		
		Iterator<RemoteFile> it = result.getResultData().iterator();
		while (it.hasNext()) {
			mFilesAdapter.add(it.next());
		}

		mFilesAdapter.remove(mFilesAdapter.getItem(0));
		mFilesAdapter.notifyDataSetChanged();
	}

    private void onSuccessfulUpload(UploadFileRemoteOperation operation, RemoteOperationResult result) {
        startRefresh();
    }

    private void onSuccessfulRemoteDeletion(RemoveFileRemoteOperation operation, RemoteOperationResult result) {
        startRefresh();
        TextView progressView = findViewById(R.id.upload_progress);
        if (progressView != null) {
            progressView.setText("0%");
        }
    }

    @SuppressWarnings("deprecation")
    private void onSuccessfulDownload(DownloadFileRemoteOperation operation, RemoteOperationResult result) {
        File downFolder = new File(getCacheDir(), getString(R.string.download_folder_path));
        File downloadedFile = downFolder.listFiles()[0];
        BitmapDrawable bDraw = new BitmapDrawable(getResources(), downloadedFile.getAbsolutePath());
        mFrame.setBackgroundDrawable(bDraw);
    }

    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileName) {
        final long percentage = (totalToTransfer > 0 ? totalTransferredSoFar * 100 / totalToTransfer : 0);
        final boolean upload = fileName.contains(getString(R.string.upload_folder_path));
        Log.d(TAG, "progressRate " + percentage);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView progressView = null;
                if (upload) {
                    progressView = findViewById(R.id.upload_progress);
                } else {
                    progressView = findViewById(R.id.download_progress);
                }
                if (progressView != null) {
                    progressView.setText(Long.toString(percentage) + "%");
                }
            }
        });
    }
}
