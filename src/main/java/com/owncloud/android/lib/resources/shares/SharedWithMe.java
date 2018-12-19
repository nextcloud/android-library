package com.owncloud.android.lib.resources.shares;

public class SharedWithMe {

    private String ownerUID;
    private String ownerDisplayName;
    private String note;

    public SharedWithMe(String ownerUID, String ownerDisplayName, String note) {
        this.ownerUID = ownerUID;
        this.ownerDisplayName = ownerDisplayName;
        this.note = note;
    }


    public String getOwnerUID() {
        return ownerUID;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public String getNote() {
        return note;
    }
}
