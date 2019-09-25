/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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
