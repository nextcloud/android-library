/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

/**
 * Provides method to define a set of share permissions and calculate the appropiate
 * int value representing it.
 */
public class SharePermissionsBuilder {

    /** Set of permissions */
    private int mPermissions = OCShare.READ_PERMISSION_FLAG;    // READ is minimum permission

    /**
     * Sets or clears permission to reshare a file or folder.
     *
     * @param enabled       'True' to set, 'false' to clear.
     * @return              Instance to builder itself, to allow consecutive calls to setters
     */
    public SharePermissionsBuilder setSharePermission(boolean enabled) {
        updatePermission(OCShare.SHARE_PERMISSION_FLAG, enabled);
        return this;
    }

    /**
     * Sets or clears permission to update a folder or folder.
     *
     * @param enabled       'True' to set, 'false' to clear.
     * @return              Instance to builder itself, to allow consecutive calls to setters
     */
    public SharePermissionsBuilder setUpdatePermission(boolean enabled) {
        updatePermission(OCShare.UPDATE_PERMISSION_FLAG, enabled);
        return this;
    }

    /**
     * Sets or clears permission to create files in share folder.
     *
     * @param enabled       'True' to set, 'false' to clear.
     * @return              Instance to builder itself, to allow consecutive calls to setters
     */
    public SharePermissionsBuilder setCreatePermission(boolean enabled) {
        updatePermission(OCShare.CREATE_PERMISSION_FLAG, enabled);
        return this;
    }

    /**
     * Sets or clears permission to delete files in a shared folder.
     *
     * @param enabled       'True' to set, 'false' to clear.
     * @return              Instance to builder itself, to allow consecutive calls to setters
     */
    public SharePermissionsBuilder setDeletePermission(boolean enabled) {
        updatePermission(OCShare.DELETE_PERMISSION_FLAG, enabled);
        return this;
    }

    /**
     * Common code to update the value of the set of permissions.
     *
     * @param permissionsFlag       Flag for the permission to update.
     * @param enable                'True' to set, 'false' to clear.
     */
    private void updatePermission(int permissionsFlag, boolean enable) {
        if (enable) {
            // add permission
            mPermissions |= permissionsFlag;
        } else {
            // delete permission
            mPermissions &= ~permissionsFlag;
        }
    }

    /**
     * 'Builds' the int value for the accumulated set of permissions.
     *
     * @return  An int value representing the accumulated set of permissions.
     */
    public int build() {
        return mPermissions;
    }
}
