/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2018 Tobias Kaminsky
 *   Copyright (C) 2018 Nextcloud GmbH
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

package com.owncloud.android.utils;

import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OwncloudVersionTest {
    @Test
    public void testOwnCloudVersion() {
        OwnCloudVersion version = new OwnCloudVersion("12.0.0");

        assertTrue(version.isNewerOrEqual(OwnCloudVersion.nextcloud_12));
    }

    @Test
    public void testOwnCloudVersionFailure() {
        OwnCloudVersion version = new OwnCloudVersion("");

        assertFalse(version.isVersionValid());
    }

    @Test
    public void testSupportNC10() {
        assertFalse(OwnCloudVersion.nextcloud_10.isSelfSupported());
        assertFalse(OwnCloudVersion.nextcloud_10.isSearchSupported());
        assertFalse(OwnCloudVersion.nextcloud_10.isWebLoginSupported());
        assertFalse(OwnCloudVersion.nextcloud_10.isMediaStreamingSupported());
        assertFalse(OwnCloudVersion.nextcloud_10.isNoteOnShareSupported());
    }

    @Test
    public void testSupportNC11() {
        assertFalse(OwnCloudVersion.nextcloud_11.isSelfSupported());
        assertFalse(OwnCloudVersion.nextcloud_11.isSearchSupported());
        assertFalse(OwnCloudVersion.nextcloud_11.isWebLoginSupported());
        assertFalse(OwnCloudVersion.nextcloud_11.isMediaStreamingSupported());
        assertFalse(OwnCloudVersion.nextcloud_11.isNoteOnShareSupported());
    }

    @Test
    public void testSupportNC12() {
        assertTrue(OwnCloudVersion.nextcloud_12.isSelfSupported());
        assertTrue(OwnCloudVersion.nextcloud_12.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_12.isWebLoginSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isMediaStreamingSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isNoteOnShareSupported());
    }

    @Test
    public void testSupportNC13() {
        assertTrue(OwnCloudVersion.nextcloud_13.isSelfSupported());
        assertTrue(OwnCloudVersion.nextcloud_13.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_13.isWebLoginSupported());
        assertFalse(OwnCloudVersion.nextcloud_13.isMediaStreamingSupported());
        assertFalse(OwnCloudVersion.nextcloud_13.isNoteOnShareSupported());
    }

    @Test
    public void testSupportNC14() {
        assertTrue(OwnCloudVersion.nextcloud_14.isSelfSupported());
        assertTrue(OwnCloudVersion.nextcloud_14.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_14.isWebLoginSupported());
        assertTrue(OwnCloudVersion.nextcloud_14.isMediaStreamingSupported());
        assertTrue(OwnCloudVersion.nextcloud_14.isNoteOnShareSupported());
    }

    @Test
    public void testSupportNC15() {
        assertTrue(OwnCloudVersion.nextcloud_15.isSelfSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isWebLoginSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isMediaStreamingSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isNoteOnShareSupported());
    }
}
