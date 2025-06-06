/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2020 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status;

import android.net.Uri;
import android.text.TextUtils;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Get the Capabilities from the server
 *
 * Save in Result.getData in a OCCapability object
 */
public class GetCapabilitiesRemoteOperation extends RemoteOperation {

    private static final String TAG = GetCapabilitiesRemoteOperation.class.getSimpleName();

    // OCS Routes
    private static final String OCS_ROUTE = "ocs/v2.php/cloud/capabilities";

    // Arguments - names
    private static final String PARAM_FORMAT = "format";

    // Arguments - constant values
    private static final String VALUE_FORMAT = "json";

    // JSON Node names
    private static final String NODE_OCS = "ocs";

    private static final String NODE_META = "meta";

    private static final String NODE_DATA = "data";
    private static final String NODE_VERSION = "version";

    private static final String NODE_CAPABILITIES = "capabilities";
    private static final String NODE_CORE = "core";

    private static final String NODE_FILES_SHARING = "files_sharing";
    private static final String NODE_PUBLIC = "public";
    private static final String NODE_PASSWORD = "password";
    private static final String NODE_ASK_FOR_OPTIONAL_PASSWORD = "askForOptionalPassword";
    private static final String NODE_EXPIRE_DATE = "expire_date";
    private static final String NODE_USER = "user";
    private static final String NODE_FEDERATION = "federation";
    private static final String NODE_FILES = "files";
    private static final String NODE_THEMING = "theming";

    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_STATUSCODE = "statuscode";
    private static final String PROPERTY_MESSAGE = "message";

    private static final String PROPERTY_POLLINTERVAL = "pollinterval";

    private static final String PROPERTY_MAJOR = "major";
    private static final String PROPERTY_MINOR = "minor";
    private static final String PROPERTY_MICRO = "micro";
    private static final String PROPERTY_STRING = "string";
    private static final String PROPERTY_EDITION = "edition";
    private static final String NODE_HAS_EXTENDED_SUPPORT = "extendedSupport";

    private static final String PROPERTY_API_ENABLED = "api_enabled";
    private static final String PROPERTY_ENABLED = "enabled";
    private static final String PROPERTY_ENFORCED = "enforced";
    private static final String PROPERTY_DAYS = "days";
    private static final String PROPERTY_ASSISTANT = "assistant";
    private static final String PROPERTY_SEND_MAIL = "send_mail";
    private static final String PROPERTY_UPLOAD = "upload";
    private static final String PROPERTY_RESHARING = "resharing";
    private static final String PROPERTY_OUTGOING = "outgoing";
    private static final String PROPERTY_INCOMING = "incoming";

    private static final String PROPERTY_BIGFILECHUNKING = "bigfilechunking";
    private static final String PROPERTY_UNDELETE = "undelete";
    private static final String PROPERTY_VERSIONING = "versioning";

    private static final String PROPERTY_LOCKING = "locking";

    private static final String PROPERTY_SERVERNAME = "name";
    private static final String PROPERTY_SERVERSLOGAN = "slogan";
    private static final String PROPERTY_SERVERCOLOR = "color";
    private static final String PROPERTY_SERVERTEXTCOLOR = "color-text";
    private static final String PROPERTY_SERVERELEMENTCOLOR = "color-element";
    private static final String PROPERTY_SERVERELEMENTCOLOR_BRIGHT = "color-element-bright";
    private static final String PROPERTY_SERVERELEMENTCOLOR_DARK = "color-element-dark";
    private static final String PROPERTY_SERVERLOGO = "logo";
    private static final String PROPERTY_SERVERBACKGROUND = "background";
    private static final String PROPERTY_SERVERBACKGROUND_DEFAULT = "background-default";
    private static final String PROPERTY_SERVERBACKGROUND_PLAIN = "background-plain";

    // v1 notifications
    private static final String NODE_NOTIFICATIONS = "notifications";
    private static final String PROPERTY_OCSENDPOINT = "ocs-endpoints";

    // v2 notifications
    private static final String PROPERTY_ICONS = "icons";
    private static final String PROPERTY_RICH_STRINGS = "rich-strings";

    // v1 external
    private static final String NODE_EXTERNAL_LINKS = "external";
    private static final String NODE_EXTERNAL_LINKS_V1 = "v1";
    private static final String NODE_EXTERNAL_LINKS_SITES = "sites";

    // v1 client side encryption
    private static final String NODE_END_TO_END_ENCRYPTION = "end-to-end-encryption";
    
    // Richdocuments
    private static final String NODE_RICHDOCUMENTS = "richdocuments";
    private static final String NODE_MIMETYPES = "mimetypes";
    private static final String NODE_OPTIONAL_MIMETYPES = "mimetypesNoDefaultOpen";
    private static final String NODE_RICHDOCUMENTS_DIRECT_EDITING = "direct_editing";
    private static final String NODE_RICHDOCUMENTS_TEMPLATES = "templates";
    private static final String NODE_RICHDOCUMENTS_PRODUCT_NAME = "productName";

    // DirectEditing
    private static final String NODE_DIRECT_EDITING = "directEditing";

    // activity
    private static final String NODE_ACTIVITY = "activity";

    // user status
    private static final String NODE_USER_STATUS = "user_status";
    private static final String NODE_USER_STATUS_ENABLED = "enabled";
    private static final String NODE_USER_STATUS_SUPPORTS_EMOJI = "supports_emoji";

    // groupfolders
    private static final String NODE_GROUPFOLDERS = "groupfolders";
    private static final String NODE_HAS_GROUPFOLDERS = "hasGroupFolders";

    // end to end encryption
    private static final String PROPERTY_KEYS_EXIST = "keys-exist";
    private static final String PROPERTY_API_VERSION = "api-version";

    // drop-account
    private static final String NODE_DROP_ACCOUNT = "drop-account";

    // security guard
    private static final String NODE_SECURITY_GUARD = "security_guard";
    private static final String NODE_DIAGNOSTICS = "diagnostics";

    //recommendations
    private static final String NODE_RECOMMENDATIONS = "recommendations";
    
    // needed for checking compatible filenames
    private static final String FORBIDDEN_FILENAME_CHARACTERS = "forbidden_filename_characters";
    private static final String FORBIDDEN_FILENAMES = "forbidden_filenames";
    private static final String FORBIDDEN_FILENAME_EXTENSIONS = "forbidden_filename_extensions";
    private static final String FORBIDDEN_FILENAME_BASE_NAMES = "forbidden_filename_basenames";

    // files download limits
    private static final String NODE_FILES_DOWNLOAD_LIMIT = "downloadlimit";
    private static final String FILES_DOWNLOAD_LIMIT_DEFAULT = "default-limit";

    // notes folder location
    private static final String NODE_NOTES = "notes";
    private static final String NOTES_PATH = "notes_path";

    private static final String PROPERTY_DEFAULT_PERMISSIONS = "default_permissions";

    private OCCapability currentCapability = null;

    public GetCapabilitiesRemoteOperation() {
    }

    public GetCapabilitiesRemoteOperation(OCCapability currentCapability) {
        this.currentCapability = currentCapability;
    }

    @Override
    public RemoteOperationResult run(NextcloudClient client) {
        RemoteOperationResult result;
        int status;
        GetMethod get = null;

        try {
            Uri requestUri = client.getBaseUri();
            Uri.Builder uriBuilder = requestUri.buildUpon();
            uriBuilder.appendEncodedPath(OCS_ROUTE);    // avoid starting "/" in this method
            uriBuilder.appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT);

            // Get Method
            get = new GetMethod(uriBuilder.build().toString(), true);

            if (null != currentCapability && !"".equals(currentCapability.getEtag())) {
                get.addRequestHeader(OCS_ETAG_HEADER, currentCapability.getEtag());
            }

            status = client.execute(get);

            if (isNotModified(status)) {
                Log_OC.d(TAG, "Capabilities not modified");

                result = new RemoteOperationResult(true, get);
                result.setSingleData(currentCapability);

                Log_OC.d(TAG, "*** Get Capabilities completed ");
            } else if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();
                Log_OC.d(TAG, "Successful response: " + response);

                OCCapability capability = parseResponse(response);

                String etag = get.getResponseHeader("ETag");
                if (!TextUtils.isEmpty(etag)) {
                    capability.setEtag(etag);
                }

                // Result
                result = new RemoteOperationResult(true, get);
                result.setSingleData(capability);
            } else {
                result = new RemoteOperationResult(false, get);
                String response = get.getResponseBodyAsString();
                Log_OC.e(TAG, "Failed response while getting capabilities from the server ");
                if (response != null) {
                    Log_OC.e(TAG, "*** status code: " + status + "; response message: " + response);
                } else {
                    Log_OC.e(TAG, "*** status code: " + status);
                }
            }
        } catch (JSONException | IOException e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Exception while getting capabilities", e);

        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return result;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        int status;
        org.apache.commons.httpclient.methods.GetMethod get = null;

        try {
            Uri requestUri = client.getBaseUri();
            Uri.Builder uriBuilder = requestUri.buildUpon();
            uriBuilder.appendEncodedPath(OCS_ROUTE);    // avoid starting "/" in this method
            uriBuilder.appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT);

            // Get Method
            get = new org.apache.commons.httpclient.methods.GetMethod(uriBuilder.build().toString());
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            if (null != currentCapability && !"".equals(currentCapability.getEtag())) {
                get.addRequestHeader(OCS_ETAG_HEADER, currentCapability.getEtag());
            }

            status = client.executeMethod(get);

            if (isNotModified(status)) {
                Log_OC.d(TAG, "Capabilities not modified");

                result = new RemoteOperationResult(true, get);
                result.setSingleData(currentCapability);

                Log_OC.d(TAG, "*** Get Capabilities completed ");
            } else if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();
                Log_OC.d(TAG, "Successful response: " + response);

                OCCapability capability = parseResponse(response);

                Header etag = get.getResponseHeader("ETag");
                if (etag != null) {
                    capability.setEtag(etag.getValue());
                }

                // Result
                result = new RemoteOperationResult(true, get);
                result.setSingleData(capability);
            } else {
                result = new RemoteOperationResult(false, get);
                String response = get.getResponseBodyAsString();
                Log_OC.e(TAG, "Failed response while getting capabilities from the server ");
                if (response != null) {
                    Log_OC.e(TAG, "*** status code: " + status + "; response message: " + response);
                } else {
                    Log_OC.e(TAG, "*** status code: " + status);
                }
            }
        } catch (JSONException | IOException e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Exception while getting capabilities", e);

        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return result;
    }

    private OCCapability parseResponse(String response) throws JSONException {
        OCCapability capability = new OCCapability();

        // Parse the response
        JSONObject respJSON = new JSONObject(response);
        JSONObject respOCS = respJSON.getJSONObject(NODE_OCS);
        JSONObject respMeta = respOCS.getJSONObject(NODE_META);
        JSONObject respData = respOCS.getJSONObject(NODE_DATA);

        // Read meta
        boolean statusProp = "ok".equalsIgnoreCase(respMeta.getString(PROPERTY_STATUS));
        String message = respMeta.getString(PROPERTY_MESSAGE);

        if (statusProp) {
            // Add Version
            if (respData.has(NODE_VERSION)) {
                JSONObject respVersion = respData.getJSONObject(NODE_VERSION);
                capability.setVersionMayor(respVersion.getInt(PROPERTY_MAJOR));
                capability.setVersionMinor(respVersion.getInt(PROPERTY_MINOR));
                capability.setVersionMicro(respVersion.getInt(PROPERTY_MICRO));
                capability.setVersionString(respVersion.getString(PROPERTY_STRING));
                capability.setVersionEdition(respVersion.getString(PROPERTY_EDITION));

                if (respVersion.has(NODE_HAS_EXTENDED_SUPPORT)) {
                    if (respVersion.getBoolean(NODE_HAS_EXTENDED_SUPPORT)) {
                        capability.setExtendedSupport(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setExtendedSupport(CapabilityBooleanType.FALSE);
                    }
                }

                Log_OC.d(TAG, "*** Added " + NODE_VERSION);
            }

            // Capabilities Object
            if (respData.has(NODE_CAPABILITIES)) {
                JSONObject respCapabilities = respData.getJSONObject(NODE_CAPABILITIES);

                // Add Core: pollInterval
                if (respCapabilities.has(NODE_CORE)) {
                    JSONObject respCore = respCapabilities.getJSONObject(NODE_CORE);
                    capability.setCorePollInterval(respCore.getInt(PROPERTY_POLLINTERVAL));
                    Log_OC.d(TAG, "*** Added " + NODE_CORE);
                }

                // Add files_sharing: public, user, resharing
                if (respCapabilities.has(NODE_FILES_SHARING)) {
                    JSONObject respFilesSharing = respCapabilities.getJSONObject(NODE_FILES_SHARING);
                    if (respFilesSharing.has(PROPERTY_API_ENABLED)) {
                        capability.setFilesSharingApiEnabled(CapabilityBooleanType.fromBooleanValue(
                                respFilesSharing.getBoolean(PROPERTY_API_ENABLED)));
                    }

                    if (respFilesSharing.has(PROPERTY_DEFAULT_PERMISSIONS)) {
                        capability.setDefaultPermissions(respFilesSharing.getInt(PROPERTY_DEFAULT_PERMISSIONS));
                    }

                    if (respFilesSharing.has(NODE_PUBLIC)) {
                        JSONObject respPublic = respFilesSharing.getJSONObject(NODE_PUBLIC);
                        capability.setFilesSharingPublicEnabled(CapabilityBooleanType.fromBooleanValue(
                                respPublic.getBoolean(PROPERTY_ENABLED)));
                        if (respPublic.has(NODE_PASSWORD)) {
                            JSONObject passwordJson = respPublic.getJSONObject(NODE_PASSWORD);

                            capability.setFilesSharingPublicPasswordEnforced(
                                    CapabilityBooleanType.fromBooleanValue(
                                            passwordJson.getBoolean(PROPERTY_ENFORCED)));

                            if (passwordJson.has(NODE_ASK_FOR_OPTIONAL_PASSWORD)) {
                                capability.setFilesSharingPublicAskForOptionalPassword(
                                        CapabilityBooleanType.fromBooleanValue(
                                                passwordJson.getBoolean(NODE_ASK_FOR_OPTIONAL_PASSWORD))
                                                                                      );
                            } else {
                                capability.setFilesSharingPublicAskForOptionalPassword(
                                        CapabilityBooleanType.FALSE);
                            }
                        }
                        if (respPublic.has(NODE_EXPIRE_DATE)) {
                            JSONObject respExpireDate = respPublic.getJSONObject(NODE_EXPIRE_DATE);
                            capability.setFilesSharingPublicExpireDateEnabled(
                                    CapabilityBooleanType.fromBooleanValue(
                                            respExpireDate.getBoolean(PROPERTY_ENABLED)));
                            if (respExpireDate.has(PROPERTY_DAYS)) {
                                capability.setFilesSharingPublicExpireDateDays(
                                        respExpireDate.getInt(PROPERTY_DAYS));
                            }
                            if (respExpireDate.has(PROPERTY_ENFORCED)) {
                                capability.setFilesSharingPublicExpireDateEnforced(
                                        CapabilityBooleanType.fromBooleanValue(
                                                respExpireDate.getBoolean(PROPERTY_ENFORCED)));
                            }
                        }
                        if (respPublic.has(PROPERTY_UPLOAD)) {
                            capability.setFilesSharingPublicUpload(CapabilityBooleanType.fromBooleanValue(
                                    respPublic.getBoolean(PROPERTY_UPLOAD)));
                        }
                    }

                    if (respFilesSharing.has(NODE_USER)) {
                        JSONObject respUser = respFilesSharing.getJSONObject(NODE_USER);
                        capability.setFilesSharingUserSendMail(CapabilityBooleanType.fromBooleanValue(
                                respUser.getBoolean(PROPERTY_SEND_MAIL)));
                    }

                    capability.setFilesSharingResharing(CapabilityBooleanType.fromBooleanValue(
                            respFilesSharing.getBoolean(PROPERTY_RESHARING)));
                    if (respFilesSharing.has(NODE_FEDERATION)) {
                        JSONObject respFederation = respFilesSharing.getJSONObject(NODE_FEDERATION);
                        capability.setFilesSharingFederationOutgoing(
                                CapabilityBooleanType.fromBooleanValue(respFederation.getBoolean(PROPERTY_OUTGOING)));
                        capability.setFilesSharingFederationIncoming(CapabilityBooleanType.fromBooleanValue(
                                respFederation.getBoolean(PROPERTY_INCOMING)));
                    }
                    Log_OC.d(TAG, "*** Added " + NODE_FILES_SHARING);
                }


                if (respCapabilities.has(NODE_FILES)) {
                    JSONObject respFiles = respCapabilities.getJSONObject(NODE_FILES);
                    // Add files
                    capability.setFilesBigFileChunking(CapabilityBooleanType.fromBooleanValue(
                            respFiles.getBoolean(PROPERTY_BIGFILECHUNKING)));
                    if (respFiles.has(PROPERTY_UNDELETE)) {
                        capability.setFilesUndelete(CapabilityBooleanType.fromBooleanValue(
                                respFiles.getBoolean(PROPERTY_UNDELETE)));
                    }

                    if (respFiles.has(PROPERTY_VERSIONING)) {
                        capability.setFilesVersioning(CapabilityBooleanType.fromBooleanValue(
                                respFiles.getBoolean(PROPERTY_VERSIONING)));
                    }

                    if (respFiles.has(PROPERTY_LOCKING)) {
                        capability.setFilesLockingVersion(respFiles.getString(PROPERTY_LOCKING));
                    }

                    // direct editing
                    if (respFiles.has(NODE_DIRECT_EDITING)) {
                        JSONObject respDirectEditing = respFiles.getJSONObject(NODE_DIRECT_EDITING);

                        capability.setDirectEditingEtag(respDirectEditing.getString("etag"));
                    }

                    // region compatible file names
                    if (respFiles.has(FORBIDDEN_FILENAME_CHARACTERS)) {
                        JSONArray result = respFiles.getJSONArray(FORBIDDEN_FILENAME_CHARACTERS);
                        capability.setForbiddenFilenameCharactersJson(result.toString());
                    }

                    if (respFiles.has(FORBIDDEN_FILENAMES)) {
                        JSONArray result = respFiles.getJSONArray(FORBIDDEN_FILENAMES);
                        capability.setForbiddenFilenamesJson(result.toString());
                    }

                    if (respFiles.has(FORBIDDEN_FILENAME_EXTENSIONS)) {
                        JSONArray result = respFiles.getJSONArray(FORBIDDEN_FILENAME_EXTENSIONS);
                        capability.setForbiddenFilenameExtensionJson(result.toString());
                    }

                    if (respFiles.has(FORBIDDEN_FILENAME_BASE_NAMES)) {
                        JSONArray result = respFiles.getJSONArray(FORBIDDEN_FILENAME_BASE_NAMES);
                        capability.setForbiddenFilenameBaseNamesJson(result.toString());
                    }
                    // endregion

                    Log_OC.d(TAG, "*** Added " + NODE_FILES);
                }

                if (respCapabilities.has(NODE_THEMING)) {
                    JSONObject respTheming = respCapabilities.getJSONObject(NODE_THEMING);
                    // Add theming
                    capability.setServerName(respTheming.getString(PROPERTY_SERVERNAME));
                    capability.setServerSlogan(respTheming.getString(PROPERTY_SERVERSLOGAN));
                    capability.setServerColor(respTheming.getString(PROPERTY_SERVERCOLOR));
                    if (respTheming.has(PROPERTY_SERVERLOGO) &&
                            respTheming.getString(PROPERTY_SERVERLOGO) != null) {
                        capability.setServerLogo(respTheming.getString(PROPERTY_SERVERLOGO));
                    }
                    if (respTheming.has(PROPERTY_SERVERBACKGROUND) &&
                            respTheming.getString(PROPERTY_SERVERBACKGROUND) != null) {
                        capability.setServerBackground(respTheming.getString(PROPERTY_SERVERBACKGROUND));
                    }
                    if (respTheming.has(PROPERTY_SERVERTEXTCOLOR) &&
                            respTheming.getString(PROPERTY_SERVERTEXTCOLOR) != null) {
                        capability.setServerTextColor(respTheming.getString(PROPERTY_SERVERTEXTCOLOR));
                    }
                    if (respTheming.has(PROPERTY_SERVERELEMENTCOLOR) &&
                            respTheming.getString(PROPERTY_SERVERTEXTCOLOR) != null) {
                        capability.setServerElementColor(respTheming.getString(PROPERTY_SERVERTEXTCOLOR));
                    }
                    if (respTheming.has(PROPERTY_SERVERELEMENTCOLOR_BRIGHT) &&
                            respTheming.getString(PROPERTY_SERVERELEMENTCOLOR_BRIGHT) != null) {
                        capability.setServerElementColorBright(respTheming.getString(PROPERTY_SERVERELEMENTCOLOR_BRIGHT));
                    }
                    if (respTheming.has(PROPERTY_SERVERELEMENTCOLOR_DARK) &&
                            respTheming.getString(PROPERTY_SERVERELEMENTCOLOR_DARK) != null) {
                        capability.setServerElementColorDark(respTheming.getString(PROPERTY_SERVERELEMENTCOLOR_DARK));
                    }
                    if (respTheming.has(PROPERTY_SERVERBACKGROUND_DEFAULT)) {
                        if (respTheming.getBoolean(PROPERTY_SERVERBACKGROUND_DEFAULT)) {
                            capability.setServerBackgroundDefault(CapabilityBooleanType.TRUE);
                        } else {
                            capability.setServerBackgroundDefault(CapabilityBooleanType.FALSE);
                        }
                    }
                    if (respTheming.has(PROPERTY_SERVERBACKGROUND_PLAIN)) {
                        if (respTheming.getBoolean(PROPERTY_SERVERBACKGROUND_PLAIN)) {
                            capability.setServerBackgroundPlain(CapabilityBooleanType.TRUE);
                        } else {
                            capability.setServerBackgroundPlain(CapabilityBooleanType.FALSE);
                        }
                    }
                    Log_OC.d(TAG, "*** Added " + NODE_THEMING);
                }

                if (respCapabilities.has(NODE_NOTIFICATIONS)) {
                    JSONObject respNotifications = respCapabilities.getJSONObject(NODE_NOTIFICATIONS);
                    JSONArray respNotificationSupportArray = respNotifications.getJSONArray(
                            PROPERTY_OCSENDPOINT);
                    for (int i = 0; i < respNotificationSupportArray.length(); i++) {
                        String propertyString = respNotificationSupportArray.getString(i);
                        if (PROPERTY_RICH_STRINGS.equals(propertyString)
                                || PROPERTY_ICONS.equals((propertyString))) {
                            capability.setSupportsNotificationsV2(CapabilityBooleanType.TRUE);
                            break;
                        }
                    }
                    if (capability.getSupportsNotificationsV2() != CapabilityBooleanType.TRUE) {
                        capability.setSupportsNotificationsV1(CapabilityBooleanType.TRUE);
                    }
                    Log_OC.d(TAG, "*** Added " + NODE_NOTIFICATIONS);
                }

                if (respCapabilities.has(NODE_EXTERNAL_LINKS)) {
                    JSONObject respExternalLinks = respCapabilities.getJSONObject(NODE_EXTERNAL_LINKS);

                    if (respExternalLinks.has(NODE_EXTERNAL_LINKS_V1)) {
                        JSONArray respExternalLinksV1 = respExternalLinks.getJSONArray(NODE_EXTERNAL_LINKS_V1);

                        String element = (String) respExternalLinksV1.get(0);

                        if (NODE_EXTERNAL_LINKS_SITES.equalsIgnoreCase(element)) {
                            capability.setExternalLinks(CapabilityBooleanType.TRUE);
                        } else {
                            capability.setExternalLinks(CapabilityBooleanType.FALSE);
                        }
                    }
                    Log_OC.d(TAG, "*** Added " + NODE_EXTERNAL_LINKS);
                }

                if (respCapabilities.has("fullnextsearch")) {
                    JSONObject respFullNextSearch = respCapabilities.getJSONObject("fullnextsearch");

                    if (respFullNextSearch.getBoolean("remote")) {
                        capability.setFullNextSearchEnabled(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setFullNextSearchEnabled(CapabilityBooleanType.FALSE);
                    }

                    JSONArray providers = respFullNextSearch.getJSONArray("providers");

                    for (int i = 0; i < providers.length(); i++) {
                        JSONObject provider = (JSONObject) providers.get(i);

                        String id = provider.getString("id");

                        // do nothing
                        if ("files".equals(id)) {
                            capability.setFullNextSearchFiles(CapabilityBooleanType.TRUE);
                            Log_OC.d(TAG, "full next search: file provider enabled");
                        }
                    }
                }

                if (respCapabilities.has(NODE_END_TO_END_ENCRYPTION)) {
                    JSONObject respEndToEndEncryption = respCapabilities
                            .getJSONObject(NODE_END_TO_END_ENCRYPTION);

                    if (respEndToEndEncryption.getBoolean(PROPERTY_ENABLED)) {
                        capability.setEndToEndEncryption(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setEndToEndEncryption(CapabilityBooleanType.FALSE);
                    }

                    if (respEndToEndEncryption.has(PROPERTY_KEYS_EXIST)) {
                        final boolean keysExist = respEndToEndEncryption.getBoolean(PROPERTY_KEYS_EXIST);
                        if (keysExist) {
                            capability.setEndToEndEncryptionKeysExist(CapabilityBooleanType.TRUE);
                        } else {
                            capability.setEndToEndEncryptionKeysExist(CapabilityBooleanType.FALSE);
                        }
                    } else {
                        capability.setEndToEndEncryptionKeysExist(CapabilityBooleanType.UNKNOWN);
                    }

                    String version = respEndToEndEncryption.getString(PROPERTY_API_VERSION);
                    E2EVersion e2EVersion = E2EVersion.fromValue(version);
                    capability.setEndToEndEncryptionApiVersion(e2EVersion);

                    Log_OC.d(TAG, "*** Added " + NODE_END_TO_END_ENCRYPTION);
                }

                // activity
                if (respCapabilities.has(NODE_ACTIVITY)) {
                    capability.setActivity(CapabilityBooleanType.TRUE);
                } else {
                    capability.setActivity(CapabilityBooleanType.FALSE);
                }

                if (respCapabilities.has(NODE_RICHDOCUMENTS)) {
                    JSONObject richDocumentsCapability = respCapabilities.getJSONObject(NODE_RICHDOCUMENTS);
                    capability.setRichDocuments(CapabilityBooleanType.TRUE);

                    JSONArray mimeTypesArray = richDocumentsCapability.getJSONArray(NODE_MIMETYPES);

                    ArrayList<String> mimeTypes = new ArrayList<>();

                    for (int i = 0; i < mimeTypesArray.length(); i++) {
                        mimeTypes.add(mimeTypesArray.getString(i));
                    }

                    capability.setRichDocumentsMimeTypeList(mimeTypes);

                    if (richDocumentsCapability.has(NODE_OPTIONAL_MIMETYPES)) {
                        JSONArray optionalMimeTypesArray = richDocumentsCapability
                                .getJSONArray(NODE_OPTIONAL_MIMETYPES);

                        ArrayList<String> optionalMimeTypes = new ArrayList<>();

                        for (int i = 0; i < optionalMimeTypesArray.length(); i++) {
                            optionalMimeTypes.add(optionalMimeTypesArray.getString(i));
                        }

                        capability.setRichDocumentsOptionalMimeTypeList(optionalMimeTypes);
                    }

                    if (richDocumentsCapability.has(NODE_RICHDOCUMENTS_DIRECT_EDITING)) {
                        if (richDocumentsCapability.getBoolean(NODE_RICHDOCUMENTS_DIRECT_EDITING)) {
                            capability.setRichDocumentsDirectEditing(CapabilityBooleanType.TRUE);
                        } else {
                            capability.setRichDocumentsDirectEditing(CapabilityBooleanType.FALSE);
                        }
                    } else {
                        capability.setRichDocumentsDirectEditing(CapabilityBooleanType.FALSE);
                    }

                    if (richDocumentsCapability.has(NODE_RICHDOCUMENTS_TEMPLATES)) {
                        if (richDocumentsCapability.getBoolean(NODE_RICHDOCUMENTS_TEMPLATES)) {
                            capability.setRichDocumentsTemplatesAvailable(CapabilityBooleanType.TRUE);
                        } else {
                            capability.setRichDocumentsTemplatesAvailable(CapabilityBooleanType.FALSE);
                        }
                    } else {
                        capability.setRichDocumentsTemplatesAvailable(CapabilityBooleanType.FALSE);
                    }

                    if (richDocumentsCapability.has(NODE_RICHDOCUMENTS_PRODUCT_NAME)) {
                        capability.setRichDocumentsProductName(
                                richDocumentsCapability.getString(NODE_RICHDOCUMENTS_PRODUCT_NAME));
                    }
                } else {
                    capability.setRichDocuments(CapabilityBooleanType.FALSE);
                }

                // user status
                if (respCapabilities.has(NODE_USER_STATUS)) {
                    JSONObject userStatusCapability = respCapabilities.getJSONObject(NODE_USER_STATUS);

                    if (userStatusCapability.getBoolean(NODE_USER_STATUS_ENABLED)) {
                        capability.setUserStatus(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setUserStatus(CapabilityBooleanType.FALSE);
                    }

                    if (userStatusCapability.getBoolean(NODE_USER_STATUS_SUPPORTS_EMOJI)) {
                        capability.setUserStatusSupportsEmoji(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setUserStatusSupportsEmoji(CapabilityBooleanType.FALSE);
                    }
                } else {
                    capability.setUserStatus(CapabilityBooleanType.FALSE);
                    capability.setUserStatusSupportsEmoji(CapabilityBooleanType.FALSE);
                }

                // groupfolders
                if (respCapabilities.has(NODE_GROUPFOLDERS)) {
                    JSONObject groupfoldersCapability = respCapabilities.getJSONObject(NODE_GROUPFOLDERS);

                    if (groupfoldersCapability.getBoolean(NODE_HAS_GROUPFOLDERS)) {
                        capability.setGroupfolders(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setGroupfolders(CapabilityBooleanType.FALSE);
                    }
                } else {
                    capability.setGroupfolders(CapabilityBooleanType.FALSE);
                }

                // assistant
                if (respCapabilities.has(PROPERTY_ASSISTANT)) {
                    JSONObject assistantCapability = respCapabilities.getJSONObject(PROPERTY_ASSISTANT);

                    if (assistantCapability.getBoolean(PROPERTY_ENABLED)) {
                        capability.setAssistant(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setAssistant(CapabilityBooleanType.FALSE);
                    }
                } else {
                    capability.setAssistant(CapabilityBooleanType.FALSE);
                }

                // drop-account
                if (respCapabilities.has(NODE_DROP_ACCOUNT)) {
                    JSONObject dropAccountCapability = respCapabilities.getJSONObject(NODE_DROP_ACCOUNT);

                    if (dropAccountCapability.getBoolean(PROPERTY_ENABLED)) {
                        capability.setDropAccount(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setDropAccount(CapabilityBooleanType.FALSE);
                    }
                }

                // security guard
                if (respCapabilities.has(NODE_SECURITY_GUARD)) {
                    JSONObject securityGuardCapability = respCapabilities.getJSONObject(NODE_SECURITY_GUARD);

                    if (securityGuardCapability.getBoolean(NODE_DIAGNOSTICS)) {
                        capability.setSecurityGuard(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setSecurityGuard(CapabilityBooleanType.FALSE);
                    }
                } else {
                    capability.setSecurityGuard(CapabilityBooleanType.FALSE);
                }

                // files download limits
                if (respCapabilities.has(NODE_FILES_DOWNLOAD_LIMIT)) {
                    JSONObject filesDownloadLimitCapability = respCapabilities.getJSONObject(NODE_FILES_DOWNLOAD_LIMIT);

                    if (filesDownloadLimitCapability.getBoolean(PROPERTY_ENABLED)) {
                        capability.setFilesDownloadLimit(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setFilesDownloadLimit(CapabilityBooleanType.FALSE);
                    }

                    if (filesDownloadLimitCapability.has(FILES_DOWNLOAD_LIMIT_DEFAULT)) {
                        int defaultDownloadLimit = filesDownloadLimitCapability.getInt(FILES_DOWNLOAD_LIMIT_DEFAULT);
                        capability.setFilesDownloadLimitDefault(defaultDownloadLimit);
                    }
                }

                // recommendations
                if (respCapabilities.has(NODE_RECOMMENDATIONS)) {
                    JSONObject recommendationsCapability = respCapabilities.getJSONObject(NODE_RECOMMENDATIONS);

                    if (recommendationsCapability.getBoolean(PROPERTY_ENABLED)) {
                        capability.setRecommendations(CapabilityBooleanType.TRUE);
                    } else {
                        capability.setRecommendations(CapabilityBooleanType.FALSE);
                    }
                } else {
                    capability.setRecommendations(CapabilityBooleanType.FALSE);
                }

                // notes folder
                if (respCapabilities.has(NODE_NOTES)) {
                    JSONObject notesCapability = respCapabilities.getJSONObject(NODE_NOTES);

                    if (notesCapability.has(NOTES_PATH)) {
                        String notesFolderPath = notesCapability.getString(NOTES_PATH);

                        if (!notesFolderPath.isEmpty() && !notesFolderPath.endsWith("/")) {
                            notesFolderPath += "/";
                        }

                        capability.setNotesFolderPath(notesFolderPath);
                    }
                }

            }

            Log_OC.d(TAG, "*** Get Capabilities completed ");
        } else {
            Log_OC.e(TAG, "Failed response while getting capabilities from the server");
            Log_OC.e(TAG, "*** status: " + statusProp + "; message: " + message);
        }

        return capability;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }

    private boolean isNotModified(int status) {
        return (status == HttpStatus.SC_NOT_MODIFIED);
    }
}
