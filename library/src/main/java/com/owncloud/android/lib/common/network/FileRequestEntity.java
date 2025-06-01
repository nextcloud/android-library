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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import okio.Throttler;
import okio.Source;
import okio.Sink;
import okio.BufferedSink;
import okio.Okio;

/**
 * A RequestEntity that represents a File.
 */
public class FileRequestEntity implements RequestEntity, ProgressiveDataTransfer {

    private final File file;
    private final String contentType;
    private final Set<OnDatatransferProgressListener> dataTransferListeners = new HashSet<>();
    private final Throttler throttler = new Throttler();

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

    /**
     * @param limit Maximum upload speed in bytes per second.
     *              Disabled by default (limit 0).
     */
    public void setBandwidthLimit(long limit) {
        throttler.bytesPerSecond(limit);
    }

    @Override
    public void writeRequest(final OutputStream out) throws IOException {
        long readResult;
        Iterator<OnDatatransferProgressListener> it;
        long transferred = 0;
        long size = file.length();
        if (size == 0) size = -1;

        Source source = null;
        Source bufferSource = null;
        Sink sink = null;
        Sink throttledSink = null;
        BufferedSink bufferedThrottledSink = null;
        try {
            source = Okio.source(file);
            bufferSource = Okio.buffer(source);

            sink = Okio.sink(out);
            throttledSink = throttler.sink(sink);
            bufferedThrottledSink = Okio.buffer(throttledSink);

            while ((readResult = bufferSource.read(bufferedThrottledSink.getBuffer(), 4096)) >= 0) {
                try {
                    bufferedThrottledSink.emitCompleteSegments();

                } catch (IOException io) {
                    // work-around try catch to filter exception in writing
                    throw new WriteException(io);
                }

                transferred += readResult;
                synchronized (dataTransferListeners) {
                    it = dataTransferListeners.iterator();
                    while (it.hasNext()) {
                        it.next().onTransferProgress(readResult, transferred, size, file.getAbsolutePath());
                    }
                }
            }
            bufferedThrottledSink.flush();

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
                // TODO Which of these are even necessary? (Been a while since I last dealt with buffers)
                if (source != null) source.close();
                if (bufferSource != null) bufferSource.close();
                // if (sink != null) sink.close();
                // if (throttledSink != null) throttledSink.close();
                // if (bufferedThrottledSink != null) bufferedThrottledSink.close();
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
