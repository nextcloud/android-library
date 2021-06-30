package com.owncloud.android.lib.resources.files.model;

public interface ServerFileInterface {
    
    String getFileName();
    
    String getMimeType();

    String getRemotePath();
    
    String getImageKey();
    
    boolean isFavorite();
    
    boolean isFolder();
    
    long getFileLength();
}
