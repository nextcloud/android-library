/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import com.owncloud.android.lib.common.OwnCloudClientManager;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;

import junit.framework.TestCase;

/**
 * Unit test for OwnCloudClientManagerFactory
 *
 * @author David A. Velasco
 */
public class OwnCloudClientManagerFactoryTest extends TestCase {

    public void testGetDefaultSingleton() {
        OwnCloudClientManager mgr = OwnCloudClientManagerFactory.getDefaultSingleton();
        assertNotNull("Returned NULL default singleton", mgr);

        OwnCloudClientManager mgr2 = OwnCloudClientManagerFactory.getDefaultSingleton();
        assertSame("Not singleton", mgr, mgr2);

        mgr = OwnCloudClientManagerFactory.getDefaultSingleton();
        assertNotNull("Returned NULL default singleton", mgr);

        mgr2 = OwnCloudClientManagerFactory.getDefaultSingleton();
        assertSame("Not singleton", mgr, mgr2);
    }

}
