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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okio.BufferedSink;
import okio.Okio;

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
            case R.id.button_speed_test:
                performSpeedTest();
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

    private void performSpeedTest() {

        // Size in MB of file to create, upload, then download
        int sizeInMB = 100;

        // Delay to wait after upload test to allow server to process
        // 200ms per MB seems to work well
        int delayInMs = (200 * sizeInMB) + 1000;

        // Limits in bytes per second (0=off)
        long uploadLimit = 5 * 1000 * 1000;
        long downloadLimit = 3 * 1000 * 1000;


        // Results
        // 100MB randomly generated file
        // Using ethernet connection on S10+

        // Original before modifications

        // Upload : Download (kBps)
        // 36273    29416
        // 45511    30649
        // 46929    31673

        // Avg
        // 42904    30579


        // Post modifications

        // Upload : Download (kBps)
        // 41915    27743
        // 45430    29066
        // 46800    33160

        // Avg
        // 44715    29989


        // Create local file with random bytes to upload to the server
        String date = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.ENGLISH).format(new Date());
        final File file = new File(getCacheDir(), "speed_test_" + date + ".txt");

        // Fill file with random bytes to limit
        BufferedSink sink = null;
        Random random = new Random();
        byte[] b = new byte[1000];
        try {
            // Open sink
            sink = Okio.buffer(Okio.sink(file));
            int targetSizeInKB = sizeInMB * 1000;

            // Write bytes to file
            for(int i=0; i < targetSizeInKB; i++) {
                random.nextBytes(b);
                sink.write(b);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (sink != null) {
                try {
                    sink.flush();
                    sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Prepare to upload file to server
        String remotePath = FileUtils.PATH_SEPARATOR + file.getName();
        String mimeType = "application/octet-stream";
        // Get the last modification date of the file from the file system
        long timeStamp = file.lastModified() / 1000;
        UploadFileRemoteOperation uploadOperation = new UploadFileRemoteOperation(
            file.getAbsolutePath(),
            remotePath,
            mimeType,
            timeStamp
        );

        // Set the limit
        uploadOperation.setBandwidthLimit(uploadLimit);

        // Start the clock
        long start = System.currentTimeMillis();

        // Use custom listener to update the values in the UI and run the download test after
        uploadOperation.addDataTransferProgressListener(new OnDatatransferProgressListener() {
            @Override
            public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName) {

                final long percentage = (totalToTransfer > 0 ? totalTransferredSoFar * 100 / totalToTransfer : 0);
                final long elapsedTime = System.currentTimeMillis() - start;
                final long estimatedSpeed = totalTransferredSoFar / Math.max(elapsedTime, 1);
                Log.d(TAG, "Upload percentage: " + percentage);

                mHandler.post(() -> {
                    TextView uploadPercent = findViewById(R.id.text_upload_completion);
                    TextView uploadSpeed = findViewById(R.id.text_upload_speed);
                    TextView uploadElapsed = findViewById(R.id.text_upload_elapsed);

                    uploadPercent.setText(percentage + " %");
                    uploadSpeed.setText(estimatedSpeed + " kBps");
                    uploadElapsed.setText(elapsedTime + " ms");

                    if (percentage == 100) {

                        Log.i(TAG, "Will run download after delay to allow server to process.");

                        // Then continue to the download test
                        Handler handler = new Handler();
                        final Runnable r = () -> runDownloadTest(file, downloadLimit);
                        handler.postDelayed(r, delayInMs);

                        Log.i(TAG, "Upload done!");
                    }
                });
            }
        });

        // Execute the upload!
        uploadOperation.execute(mClient, this, mHandler);

    }

    private void runDownloadTest(File file, long downloadLimit)  {

        try {

            // Download remote file
            Log.i(TAG, "Starting download!");

            // Setup for download
            File downFolder = new File(getCacheDir(), getString(R.string.download_folder_path));
            downFolder.mkdir();
            String remotePath = FileUtils.PATH_SEPARATOR  + file.getName();
            DownloadFileRemoteOperation downloadOperation = new DownloadFileRemoteOperation(remotePath,
                downFolder.getAbsolutePath());
            downloadOperation.setBandwidthLimit(downloadLimit);

            // Start the clock
            final long start = System.currentTimeMillis();

            // Add the listening code to update the UI
            downloadOperation.addDatatransferProgressListener(new OnDatatransferProgressListener() {
                @Override
                public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName) {
                    final long percentage = (totalToTransfer > 0 ? totalTransferredSoFar * 100 / totalToTransfer : 0);
                    final long elapsedTime = System.currentTimeMillis() - start;
                    final long estimatedSpeed = totalTransferredSoFar / Math.max(elapsedTime, 1);
                    Log.d(TAG, "Download percentage:  " + percentage);

                    mHandler.post(() -> {
                        TextView downloadPercent = findViewById(R.id.text_download_completion);
                        TextView downloadSpeed = findViewById(R.id.text_download_speed);
                        TextView downloadElapsed = findViewById(R.id.text_download_elapsed);

                        downloadPercent.setText(percentage + " %");
                        downloadSpeed.setText(estimatedSpeed + " kBps");
                        downloadElapsed.setText(elapsedTime + " ms");
                    });
                }
            });

            // Run the download
            downloadOperation.execute(mClient, this, mHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            onSuccessfulRefresh((ReadFolderRemoteOperation) operation, result);

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

	private void onSuccessfulRefresh(ReadFolderRemoteOperation operation, RemoteOperationResult result) {
		mFilesAdapter.clear();
		List<RemoteFile> files = new ArrayList<RemoteFile>();
		for (Object obj : result.getData()) {
			files.add((RemoteFile) obj);
		}

		Iterator<RemoteFile> it = files.iterator();
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
