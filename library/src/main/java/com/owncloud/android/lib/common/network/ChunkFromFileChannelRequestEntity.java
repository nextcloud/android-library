/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
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
import java.util.Iterator;
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
    private ByteBuffer mBuffer = ByteBuffer.allocate(4096);

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
        int readCount;
        Iterator<OnDatatransferProgressListener> progressListenerIterator;

        try {
            mChannel.position(mOffset);
            long size = mFile.length();
            if (size == 0) {
                size = -1;
            }
            long maxCount = Math.min(mOffset + length - 1, mChannel.size());
            while (mChannel.position() < maxCount) {
                readCount = mChannel.read(mBuffer);
                try {
                    out.write(mBuffer.array(), 0, readCount);
                } catch (IOException io) {
                    // work-around try catch to filter exception in writing
                    throw new FileRequestEntity.WriteException(io);
                }
                mBuffer.clear();
                if (mTransferred < maxCount) {  // condition to avoid accumulate progress for repeated chunks
                    mTransferred += readCount;
                }
                synchronized (mDataTransferListeners) {
                    progressListenerIterator = mDataTransferListeners.iterator();

                    while (progressListenerIterator.hasNext()) {
                        progressListenerIterator.next().onTransferProgress(readCount, mTransferred, size, 
                                                                           mFile.getAbsolutePath());
                    }
                }
            }

        } catch (IOException io) {
            // any read problem will be handled as if the file is not there
            if (io instanceof FileNotFoundException) {
                throw io;
            } else {
                FileNotFoundException fnf = new FileNotFoundException("Exception reading source file");
                fnf.initCause(io);
                throw fnf;
            }

        } catch (FileRequestEntity.WriteException we) {
            throw we.getWrapped();
        }
            
    }

}
