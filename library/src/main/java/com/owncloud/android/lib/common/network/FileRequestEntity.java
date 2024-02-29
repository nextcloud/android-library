/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014-2016 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014-2016 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2012 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import org.apache.commons.httpclient.methods.RequestEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A RequestEntity that represents a File.
 */
public class FileRequestEntity implements RequestEntity, ProgressiveDataTransfer {

    private final File file;
    private final String contentType;
    private final Set<OnDatatransferProgressListener> dataTransferListeners = new HashSet<>();

    public FileRequestEntity(final File file, final String contentType) {
        super();
        this.file = file;
        this.contentType = contentType;
        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }
    }
    
    @Override
    public long getContentLength() {
        return file.length();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void addDataTransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (dataTransferListeners) {
            dataTransferListeners.add(listener);
        }
    }
    
    @Override
    public void addDataTransferProgressListeners(Collection<OnDatatransferProgressListener> listeners) {
        synchronized (dataTransferListeners) {
            dataTransferListeners.addAll(listeners);
        }
    }
    
    @Override
    public void removeDataTransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (dataTransferListeners) {
            dataTransferListeners.remove(listener);
        }
    }
    
    
    @Override
    public void writeRequest(final OutputStream out) throws IOException {
        ByteBuffer tmp = ByteBuffer.allocate(4096);
        int readResult;

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        Iterator<OnDatatransferProgressListener> it;
        long transferred = 0;
        long size = file.length();
        if (size == 0) size = -1;
        try {
            while ((readResult = channel.read(tmp)) >= 0) {
                try {
                    out.write(tmp.array(), 0, readResult);
                } catch (IOException io) {
                    // work-around try catch to filter exception in writing
                    throw new WriteException(io);
                }
                tmp.clear();
                transferred += readResult;
                synchronized (dataTransferListeners) {
                    it = dataTransferListeners.iterator();
                    while (it.hasNext()) {
                        it.next().onTransferProgress(readResult, transferred, size, file.getAbsolutePath());
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

        } catch (WriteException we) {
            throw we.getWrapped();

        } finally {
            try {
                channel.close();
                raf.close();
            } catch (IOException io) {
                // ignore failures closing source file
            }
        }
    }

    static class WriteException extends Exception {
        IOException mWrapped;

        WriteException(IOException wrapped) {
            mWrapped = wrapped;
        }

        IOException getWrapped() {
            return mWrapped;
        }
    }

}
