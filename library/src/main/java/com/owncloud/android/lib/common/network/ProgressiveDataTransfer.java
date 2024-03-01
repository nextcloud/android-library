/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014-2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import java.util.Collection;



public interface ProgressiveDataTransfer {
    void addDataTransferProgressListener(OnDatatransferProgressListener listener);

    void addDataTransferProgressListeners(Collection<OnDatatransferProgressListener> listeners);

    void removeDataTransferProgressListener(OnDatatransferProgressListener listener);
}
