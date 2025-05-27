/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.utils.Log_OC;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    public static final String PATH_SEPARATOR = "/";

    public static String getHashFromFile(Object thi, File f, String digestAlgorithm) {
        if (OwnCloudClientManagerFactory.getHASHcheck()) {
            try {
                MessageDigest md = MessageDigest.getInstance(digestAlgorithm);

                try (FileInputStream fis = new FileInputStream(f);
                     DigestInputStream dis = new DigestInputStream(fis, md)) {
                    byte[] buffer = new byte[8192];
                    while (dis.read(buffer) != -1) {
                        // digest is updated by reading
                    }
                }
                return String.format("%064x", new BigInteger(1, md.digest()));
            } catch (Exception e) {
                Log_OC.w(thi, "Could not compute chunk hash");
            }
        }
        return null;
    }
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

    @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
    /*
     * ATTENTION: Do not use this for security critical purpose!
     */
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
