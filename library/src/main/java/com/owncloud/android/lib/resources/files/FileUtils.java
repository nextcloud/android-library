/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
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

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    public static final String PATH_SEPARATOR = "/";


    public static String getParentPath(String remotePath) {
        String parentPath = new File(remotePath).getParent();
        parentPath = parentPath.endsWith(PATH_SEPARATOR) ? parentPath : parentPath + PATH_SEPARATOR;
        return parentPath;
    }

    /**
     * Validate the fileName to detect if path separator "/" is used
     *
     * @param fileName name to check
     * @return true if if no path separator is used
     */
    public static boolean isValidName(String fileName) {

        return !fileName.contains(PATH_SEPARATOR);
    }

    public static String md5Sum(File file) throws NoSuchAlgorithmException {
        String temp = file.getName() + file.lastModified() + file.length();

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(temp.getBytes());
        byte[] digest = messageDigest.digest();
        StringBuilder md5String = new StringBuilder(new BigInteger(1, digest).toString(16));

        while (md5String.length() < 32) {
            md5String.insert(0, "0");
        }

        return md5String.toString();
    }
}
