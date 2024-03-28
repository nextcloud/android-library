/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import android.net.Uri;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ShareToRemoteOperationResultParser {

    private static final String TAG = ShareToRemoteOperationResultParser.class.getSimpleName();

    private ShareXMLParser shareXmlParser;
    private boolean oneOrMoreSharesRequired = false;
    private Uri serverBaseUri = null;


    public ShareToRemoteOperationResultParser(ShareXMLParser shareXmlParser) {
        this.shareXmlParser = shareXmlParser;
    }

    public void setOneOrMoreSharesRequired(boolean oneOrMoreSharesRequired) {
        this.oneOrMoreSharesRequired = oneOrMoreSharesRequired;
    }

    public void setServerBaseUri(Uri serverBaseURi) {
        serverBaseUri = serverBaseURi;
    }

    public RemoteOperationResult<List<OCShare>> parse(String serverResponse) {
        if (serverResponse == null || serverResponse.length() == 0) {
            return new RemoteOperationResult<>(RemoteOperationResult.ResultCode.WRONG_SERVER_RESPONSE);
        }

        RemoteOperationResult<List<OCShare>> result;
        List<OCShare> resultData = new ArrayList<>();

        try {
            // Parse xml response and obtain the list of shares
            InputStream is = new ByteArrayInputStream(serverResponse.getBytes());
            if (shareXmlParser == null) {
                Log_OC.w(TAG, "No ShareXmlParser provided, creating new instance ");
                shareXmlParser = new ShareXMLParser();
            }
            List<OCShare> shares = shareXmlParser.parseXMLResponse(is);

            if (shareXmlParser.isSuccess()) {
                if ((shares != null && !shares.isEmpty()) || !oneOrMoreSharesRequired) {
                    result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.OK);
                    if (shares != null) {
                        for (OCShare share : shares) {
                            resultData.add(share);
                            // build the share link if not in the response (only received when the share is created)
                            if (share.getShareType() == ShareType.PUBLIC_LINK &&
                                    (share.getShareLink() == null ||
                                            share.getShareLink().length() <= 0) &&
                                    share.getToken().length() > 0
                                    ) {
                                if (serverBaseUri != null) {
                                    share.setShareLink(serverBaseUri +
                                            ShareUtils.SHARING_LINK_PATH_AFTER_VERSION_8 +
                                            share.getToken());
                                } else {
                                    Log_OC.e(TAG, "Couldn't build link for public share");
                                }
                            }
                        }
                    }
                    result.setResultData(resultData);

                } else {
                    result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.WRONG_SERVER_RESPONSE);
                    Log_OC.e(TAG, "Successful status with no share in the response");
                }

            } else if (shareXmlParser.isWrongParameter()) {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.SHARE_WRONG_PARAMETER);
                result.message = shareXmlParser.getMessage();
            } else if (shareXmlParser.isNotFound()) {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.SHARE_NOT_FOUND);
                result.message = shareXmlParser.getMessage();
            } else if (shareXmlParser.isForbidden()) {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.SHARE_FORBIDDEN);
                result.message = shareXmlParser.getMessage();
            } else {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.WRONG_SERVER_RESPONSE);
                result.message = shareXmlParser.getMessage();
            }

        } catch (XmlPullParserException e) {
            Log_OC.e(TAG, "Error parsing response from server ", e);
            result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.WRONG_SERVER_RESPONSE);

        } catch (IOException e) {
            Log_OC.e(TAG, "Error reading response from server ", e);
            result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.WRONG_SERVER_RESPONSE);
        }

        return result;
    }

}
