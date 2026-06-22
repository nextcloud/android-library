/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2018-2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014-2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2012 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import org.apache.commons.httpclient.methods.RequestEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * A RequestEntity that represents a PIECE of a file.
 * 
 * @author David A. Velasco
 */
public class ChunkFromFileChannelRequestEntity implements RequestEntity, ProgressiveDataTransfer {
    private final FileChannel mChannel;
    private final String mContentType;
    private final long length;
    private final File mFile;
    private long mOffset;
    private long mTransferred;
    private final Set<OnDatatransferProgressListener> mDataTransferListeners = new HashSet<>();

    public ChunkFromFileChannelRequestEntity(final FileChannel channel, final String contentType, long offset, 
                                             long chunkSize, final File file) {
        super();
        if (channel == null) {
            throw new IllegalArgumentException("File may not be null");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk length must be greater than zero");
        }
        mChannel = channel;
        mContentType = contentType;
        length = chunkSize;
        mFile = file;
        mOffset = offset;
        mTransferred = offset;
    }
    
    public long getContentLength() {
        try {
            return Math.min(length, mChannel.size() - mOffset);
        } catch (IOException e) {
            return length;
        }
    }

    public String getContentType() {
        return mContentType;
    }

    public boolean isRepeatable() {
        return true;
    }
    
    @Override
    public void addDataTransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (mDataTransferListeners) {
            mDataTransferListeners.add(listener);
        }
    }
    
    @Override
    public void addDataTransferProgressListeners(Collection<OnDatatransferProgressListener> listeners) {
        synchronized (mDataTransferListeners) {
            mDataTransferListeners.addAll(listeners);
        }
    }
    
    @Override
    public void removeDataTransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (mDataTransferListeners) {
            mDataTransferListeners.remove(listener);
        }
    }

    public void writeRequest(final OutputStream out) throws IOException {
        mChannel.position(mOffset);
        long maxCount = Math.min(mOffset + length, mChannel.size());
        long remaining = maxCount - mOffset;

        long rawSize = mFile.length();
        long fileSize = rawSize > 0 ? rawSize : -1;

        byte[] buffer = new byte[4096];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        while (remaining > 0) {
            int toRead = (int) Math.min(buffer.length, remaining);
            int bytesRead;

            byteBuffer.position(0);
            byteBuffer.limit(toRead);

            try {
                bytesRead = mChannel.read(byteBuffer);
            } catch (IOException e) {
                FileNotFoundException fnf = new FileNotFoundException("Exception reading source file");
                fnf.initCause(e);
                throw fnf;
            }

            if (bytesRead <= 0) {
                break;
            }

            out.write(buffer, 0, bytesRead);
            remaining -= bytesRead;

            if (mTransferred < maxCount) {
                mTransferred += bytesRead;
            }
            notifyTransferProgress(bytesRead, fileSize);
        }
    }

    private void notifyTransferProgress(int bytesRead, long fileSize) {
        synchronized (mDataTransferListeners) {
            for (OnDatatransferProgressListener listener : mDataTransferListeners) {
                listener.onTransferProgress(bytesRead, mTransferred, fileSize, mFile.getAbsolutePath());
            }
        }
    }
}
