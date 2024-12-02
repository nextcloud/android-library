/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import android.util.Xml;

import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.resources.files.FileUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parser for Share API Response
 *
 * @author masensio
 *
 */
public class ShareXMLParser {

	// No namespaces
	private static final String ns = null;

	// NODES for XML Parser
	private static final String NODE_OCS = "ocs";

	private static final String NODE_META = "meta";
	private static final String NODE_STATUS = "status";
	private static final String NODE_STATUS_CODE = "statuscode";
	private static final String NODE_MESSAGE = "message";

	private static final String NODE_DATA = "data";
	private static final String NODE_ELEMENT = "element";
	private static final String NODE_ID = "id";
	private static final String NODE_ITEM_TYPE = "item_type";
	private static final String NODE_ITEM_SOURCE = "item_source";
	private static final String NODE_PARENT = "parent";
	private static final String NODE_SHARE_TYPE = "share_type";
	private static final String NODE_SHARE_WITH = "share_with";
	private static final String NODE_FILE_SOURCE = "file_source";
	private static final String NODE_PATH = "path";
	private static final String NODE_PERMISSIONS = "permissions";
	private static final String NODE_STIME = "stime";
	private static final String NODE_EXPIRATION = "expiration";
	private static final String NODE_TOKEN = "token";
	private static final String NODE_STORAGE = "storage";
	private static final String NODE_MAIL_SEND = "mail_send";
	private static final String NODE_PASSWORD = "password";
	private static final String NODE_SHARE_WITH_DISPLAY_NAME = "share_with_displayname";
	private static final String NODE_NOTE = "note";
	private static final String NODE_HIDE_DOWNLOAD = "hide_download";
	private static final String NODE_UID_OWNER = "uid_owner";
	private static final String NODE_LABEL = "label";
	private static final String NODE_HAS_PREVIEW = "has_preview";
	private static final String NODE_MIMETYPE = "mimetype";
	private static final String NODE_DISPLAYNAME_FILE_OWNER = "displayname_file_owner";
	private static final String NODE_TAGS = "tags";
	private static final String NODE_URL = "url";

	private static final String TAG_FAVORITE = "_$!<Favorite>";

	private static final String TYPE_FOLDER = "folder";

	private static final String EMPTY_LINE = "\n";

	private static final String TRUE = "1";

	private static final int SUCCESS = 100;
	private static final int OK = 200;
	private static final int ERROR_WRONG_PARAMETER = 400;
	private static final int ERROR_FORBIDDEN = 403;
	private static final int ERROR_NOT_FOUND = 404;

	private String mStatus;
	private int mStatusCode;
	private String mMessage = "";

	// Getters and Setters
	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String status) {
		this.mStatus = status;
	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public void setStatusCode(int statusCode) {
		this.mStatusCode = statusCode;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	// Constructor
	public ShareXMLParser() {
		mStatusCode = -1;
	}

	public boolean isSuccess() {
		return mStatusCode == SUCCESS || mStatusCode == OK;
	}

	public boolean isForbidden() {
		return mStatusCode == ERROR_FORBIDDEN;
	}

	public boolean isNotFound() {
		return mStatusCode == ERROR_NOT_FOUND;
	}

	public boolean isWrongParameter() {
		return mStatusCode == ERROR_WRONG_PARAMETER;
	}

	/**
	 * Parse is as response of Share API
     * @param is InputStream to parse
	 * @return List of ShareRemoteFiles
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
    public ArrayList<OCShare> parseXMLResponse(InputStream is) throws XmlPullParserException, IOException {
		try {
			// XMLPullParser
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);

			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(is, null);
			parser.nextTag();
			return readOCS(parser);
		} finally {
			is.close();
		}
	}

	/**
	 * Parse OCS node
	 * @param parser
	 * @return List of ShareRemoteFiles
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ArrayList<OCShare> readOCS (XmlPullParser parser) throws XmlPullParserException,
			IOException {
		ArrayList<OCShare> shares = new ArrayList<OCShare>();
		parser.require(XmlPullParser.START_TAG,  ns , NODE_OCS);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// read NODE_META and NODE_DATA
			if (NODE_META.equalsIgnoreCase(name)) {
				readMeta(parser);
			} else if (NODE_DATA.equalsIgnoreCase(name)) {
				shares = readData(parser);
			} else {
				skip(parser);
			}

		}
		return shares;


	}

	/**
	 * Parse Meta node
	 * @param parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void readMeta(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, NODE_META);
		//Log_OC.d(TAG, "---- NODE META ---");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();

            if (NODE_STATUS.equalsIgnoreCase(name)) {
				setStatus(readNode(parser, NODE_STATUS));

            } else if (NODE_STATUS_CODE.equalsIgnoreCase(name)) {
				setStatusCode(Integer.parseInt(readNode(parser, NODE_STATUS_CODE)));

            } else if (NODE_MESSAGE.equalsIgnoreCase(name)) {
				setMessage(readNode(parser, NODE_MESSAGE));

			} else {
				skip(parser);
			}

		}
	}

	/**
	 * Parse Data node
	 * @param parser
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ArrayList<OCShare> readData(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		ArrayList<OCShare> shares = new ArrayList<OCShare>();
		OCShare share = null;

		parser.require(XmlPullParser.START_TAG, ns, NODE_DATA);		
		//Log_OC.d(TAG, "---- NODE DATA ---");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (NODE_ELEMENT.equalsIgnoreCase(name)) {
				readElement(parser, shares);

			} else if (NODE_ID.equalsIgnoreCase(name)) {// Parse Create XML Response
				share = new OCShare();
				String value = readNode(parser, NODE_ID);
				share.setRemoteId(Integer.parseInt(value));

			} else if (NODE_URL.equalsIgnoreCase(name)) {
				share.setShareType(ShareType.PUBLIC_LINK);
				String value = readNode(parser, NODE_URL);
				share.setShareLink(value);

			} else if (NODE_TOKEN.equalsIgnoreCase(name)) {
				share.setToken(readNode(parser, NODE_TOKEN));

			} else {
				skip(parser);
				
			} 
		}
		
		if (share != null) {
			// this is the response of a request for creation; don't pass to isValidShare()
			shares.add(share);
		}

		return shares;

	}


	/** 
	 * Parse Element node
	 * @param parser
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void readElement(XmlPullParser parser, ArrayList<OCShare> shares)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, NODE_ELEMENT);
		
		OCShare share = new OCShare();

		//Log_OC.d(TAG, "---- NODE ELEMENT ---");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			switch (name) {
				case NODE_ELEMENT:
					// patch to work around servers responding with extra <element> surrounding all
					// the shares on the same file before
					// https://github.com/owncloud/core/issues/6992 was fixed
					readElement(parser, shares);
					break;

				case NODE_ID:
					share.setRemoteId(Integer.parseInt(readNode(parser, NODE_ID)));
					break;

				case NODE_ITEM_TYPE:
					share.setFolder(TYPE_FOLDER.equalsIgnoreCase(readNode(parser, NODE_ITEM_TYPE)));
					fixPathForFolder(share);
					break;

				case NODE_ITEM_SOURCE:
					share.setItemSource(Long.parseLong(readNode(parser, NODE_ITEM_SOURCE)));
					break;

				case NODE_PARENT:
					readNode(parser, NODE_PARENT);
					break;

				case NODE_SHARE_TYPE:
					int value = Integer.parseInt(readNode(parser, NODE_SHARE_TYPE));
					share.setShareType(ShareType.fromValue(value));
					break;

				case NODE_SHARE_WITH:
					share.setShareWith(readNode(parser, NODE_SHARE_WITH));
					break;

				case NODE_FILE_SOURCE:
					share.setFileSource(Long.parseLong(readNode(parser, NODE_FILE_SOURCE)));
					break;

				case NODE_PATH:
					share.setPath(readNode(parser, NODE_PATH));
					fixPathForFolder(share);
					break;

				case NODE_PERMISSIONS:
					share.setPermissions(Integer.parseInt(readNode(parser, NODE_PERMISSIONS)));
					break;

				case NODE_STIME:
					share.setSharedDate(Long.parseLong(readNode(parser, NODE_STIME)));
					break;

				case NODE_EXPIRATION:
					String expirationValue = readNode(parser, NODE_EXPIRATION);
                    if (expirationValue.length() > 0) {
						Date date = WebdavUtils.INSTANCE.parseResponseDate(expirationValue);
						if (date != null) {
							share.setExpirationDate(date.getTime());
						}
					}
					break;

				case NODE_PASSWORD:
					share.setPasswordProtected(readNode(parser, NODE_PASSWORD).length() > 0);
					break;

				case NODE_TOKEN:
					share.setToken(readNode(parser, NODE_TOKEN));
					break;

				case NODE_STORAGE:
					readNode(parser, NODE_STORAGE);
					break;

				case NODE_MAIL_SEND:
					readNode(parser, NODE_MAIL_SEND);
					break;

				case NODE_SHARE_WITH_DISPLAY_NAME:
					share.setSharedWithDisplayName(readNode(parser, NODE_SHARE_WITH_DISPLAY_NAME));
					break;

				case NODE_URL:
					share.setShareType(ShareType.PUBLIC_LINK);
					share.setShareLink(readNode(parser, NODE_URL));
					break;

				case NODE_NOTE:
					share.setNote(readNode(parser, NODE_NOTE));
					break;

				case NODE_HIDE_DOWNLOAD:
					boolean b = TRUE.equalsIgnoreCase(readNode(parser, NODE_HIDE_DOWNLOAD));
					share.setHideFileDownload(b);
					break;

				case NODE_UID_OWNER:
					share.setUserId(readNode(parser, NODE_UID_OWNER));
					break;

				case NODE_LABEL:
					share.setLabel(readNode(parser, NODE_LABEL));
					break;

				case NODE_HAS_PREVIEW:
					share.setHasPreview(TRUE.equalsIgnoreCase(readNode(parser, NODE_HAS_PREVIEW)));
					break;

				case NODE_MIMETYPE:
					share.setMimetype(readNode(parser, NODE_MIMETYPE));
					break;

				case NODE_DISPLAYNAME_FILE_OWNER:
					share.setOwnerDisplayName(readNode(parser, NODE_DISPLAYNAME_FILE_OWNER));
					break;

				case NODE_TAGS:
					List<String> tags = readArrayNode(parser);
					for (String tag : tags) {
						if (tag.startsWith(TAG_FAVORITE)) {
							share.setFavorite(true);
							break;
						}

					}
					break;

				default:
					skip(parser);
					break;
			}
		}		

		if (isValidShare(share)) {
			shares.add(share);
		}
	}

	private boolean isValidShare(OCShare share) {
		return (share.getRemoteId() > -1);
	}

	private void fixPathForFolder(OCShare share) {
		if (share.isFolder() && share.getPath() != null && share.getPath().length() > 0 &&
				!share.getPath().endsWith(FileUtils.PATH_SEPARATOR)) {
			share.setPath(share.getPath() + FileUtils.PATH_SEPARATOR);
		}
	}

	/**
	 * Parse a node, to obtain its text. Needs readText method
	 * @param parser
	 * @param node
	 * @return Text of the node
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private String readNode(XmlPullParser parser, String node) throws XmlPullParserException,
			IOException {
		parser.require(XmlPullParser.START_TAG, ns, node);
		String value = readText(parser);
		//Log_OC.d(TAG, "node= " + node + ", value= " + value);
		parser.require(XmlPullParser.END_TAG, ns, node);
		return value;
	}

	/**
	 * Read the text from a node
	 *
	 * @param parser
	 * @return Text of the node
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private List<String> readArrayNode(XmlPullParser parser) throws IOException, XmlPullParserException {
		ArrayList<String> list = new ArrayList<>();

		parser.require(XmlPullParser.START_TAG, ns, ShareXMLParser.NODE_TAGS);

		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			String tag = parser.getName();

			if (event == XmlPullParser.TEXT) {
				String text = parser.getText();

				if (!EMPTY_LINE.equals(text)) {
					list.add(text);
				}
			}

			if (event == XmlPullParser.END_TAG && NODE_TAGS.equals(tag)) {
				break;
			}

			event = parser.next();
		}

		return list;
	}

	/**
	 * Skip tags in parser procedure
	 *
	 * @param parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}
