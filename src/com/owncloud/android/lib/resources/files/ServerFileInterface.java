package com.owncloud.android.lib.resources.files;

public interface ServerFileInterface {
    
    String getFileName();
    
    String getMimeType();
    
    String getRemotePath();
    
    String getRemoteId();
    
    boolean getIsFavorite();
    
    boolean isFolder();
    
    long getFileLength();
}
