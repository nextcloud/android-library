/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2014 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import android.util.Log;

import com.nextcloud.common.SessionTimeOut;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.client.methods.CopyMethod;

import java.io.IOException;


/**
 * Remote operation moving a remote file or folder in the ownCloud server to a different folder
 * in the same account.
 *
 * Allows renaming the moving file/folder at the same time.
 *
 * @author David A. Velasco
 */
public class CopyFileRemoteOperation extends RemoteOperation {

    private static final String TAG = CopyFileRemoteOperation.class.getSimpleName();


    private final String mSrcRemotePath;
    private final String mTargetRemotePath;

    private final boolean mOverwrite;

    private final SessionTimeOut sessionTimeOut;


    /**
     * Constructor.
     *
     * TODO Paths should finish in "/" in the case of folders. ?
     *
     * @param srcRemotePath    Remote path of the file/folder to move.
     * @param targetRemotePath Remove path desired for the file/folder after moving it.
     */
    public CopyFileRemoteOperation(String srcRemotePath, String targetRemotePath, boolean overwrite) {
        this(srcRemotePath, targetRemotePath, overwrite, new SessionTimeOut(600000, 5000));
    }


    public CopyFileRemoteOperation(String srcRemotePath, String targetRemotePath, boolean overwrite, SessionTimeOut sessionTimeOut) {
        mSrcRemotePath = srcRemotePath;
        mTargetRemotePath = targetRemotePath;
        mOverwrite = overwrite;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the rename operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {

        /// check parameters
        if (mTargetRemotePath.equals(mSrcRemotePath)) {
            // nothing to do!
            return new RemoteOperationResult<>(ResultCode.OK);
        }

        if (mTargetRemotePath.startsWith(mSrcRemotePath)) {
            return new RemoteOperationResult<>(ResultCode.INVALID_COPY_INTO_DESCENDANT);
        }

        /// perform remote operation
        CopyMethod copyMethod = null;
        RemoteOperationResult result = null;
        try {
            copyMethod = new CopyMethod(
                    client.getFilesDavUri(mSrcRemotePath),
                    client.getFilesDavUri(mTargetRemotePath),
                    mOverwrite
            );
            int status = client.executeMethod(copyMethod, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            /// process response
            if (status == HttpStatus.SC_MULTI_STATUS) {
                result = processPartialError(copyMethod);

            } else if (status == HttpStatus.SC_PRECONDITION_FAILED && !mOverwrite) {

                result = new RemoteOperationResult<>(ResultCode.INVALID_OVERWRITE);
                client.exhaustResponse(copyMethod.getResponseBodyAsStream());


                /// for other errors that could be explicitly handled, check first:
                /// http://www.webdav.org/specs/rfc4918.html#rfc.section.9.9.4

            } else {
                result = new RemoteOperationResult<>(isSuccess(status), copyMethod);
                client.exhaustResponse(copyMethod.getResponseBodyAsStream());
            }

            Log.i(TAG, "Copy " + mSrcRemotePath + " to " + mTargetRemotePath + ": " + result.getLogMessage());

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log.e(TAG, "Copy " + mSrcRemotePath + " to " + mTargetRemotePath + ": " + result.getLogMessage(), e);

        } finally {
            if (copyMethod != null) {
                copyMethod.releaseConnection();
            }
        }

        return result;
    }


    /**
     * Analyzes a multistatus response from the OC server to generate an appropriate result.
     *
     * In WebDAV, a COPY request on collections (folders) can be PARTIALLY successful: some
     * children are copied, some other aren't.
     *
     * According to the WebDAV specification, a multistatus response SHOULD NOT include partial
     * successes (201, 204) nor for descendants of already failed children (424) in the response
     * entity. But SHOULD NOT != MUST NOT, so take carefully.
     *
     * @param copyMethod Copy operation just finished with a multistatus response
     * @return A result for the {@link CopyFileRemoteOperation} caller
     * @throws java.io.IOException                       If the response body could not be parsed
     * @throws org.apache.jackrabbit.webdav.DavException If the status code is other than MultiStatus or if obtaining
     *                                                   the response XML document fails
     */
    private RemoteOperationResult processPartialError(CopyMethod copyMethod)
            throws IOException, DavException {
        // Adding a list of failed descendants to the result could be interesting; or maybe not.
        // For the moment, let's take the easy way.

        /// check that some error really occurred
        MultiStatusResponse[] responses = copyMethod.getResponseBodyAsMultiStatus().getResponses();
        Status[] status;
        boolean failFound = false;
        for (int i = 0; i < responses.length && !failFound; i++) {
            status = responses[i].getStatus();
            failFound = (
                    status != null &&
                            status.length > 0 &&
                            status[0].getStatusCode() > 299
            );
        }

        RemoteOperationResult result;
        if (failFound) {
            result = new RemoteOperationResult<>(ResultCode.PARTIAL_COPY_DONE);
        } else {
            result = new RemoteOperationResult<>(true, copyMethod);
        }

        return result;
    }

    protected boolean isSuccess(int status) {
        return status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT;
    }
}
