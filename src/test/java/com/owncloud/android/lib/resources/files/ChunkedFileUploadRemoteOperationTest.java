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

package com.owncloud.android.lib.resources.files;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChunkedFileUploadRemoteOperationTest {

    private long chunkSize = 1024;

    @Test
    public void testUploadWithoutExistingChunks() {
        long length = 2 * chunkSize;

        List<Chunk> existingChunks = new ArrayList<>();

        List<Chunk> expectedMissingChunks = new ArrayList<>();
        expectedMissingChunks.add(new Chunk(0, 1023));
        expectedMissingChunks.add(new Chunk(1024, 2047));
        expectedMissingChunks.add(new Chunk(2048, 2048));

        assertTrue(test(existingChunks, expectedMissingChunks, chunkSize, length));
    }

    @Test
    public void testUploadWithoutExistingChunksBig() {
        long length = 4 * chunkSize;

        List<Chunk> existingChunks = new ArrayList<>();

        List<Chunk> expectedMissingChunks = new ArrayList<>();
        expectedMissingChunks.add(new Chunk(0, 1023));
        expectedMissingChunks.add(new Chunk(1024, 2047));
        expectedMissingChunks.add(new Chunk(2048, 3071));
        expectedMissingChunks.add(new Chunk(3072, 4095));
        expectedMissingChunks.add(new Chunk(4096, 4096));

        assertTrue(test(existingChunks, expectedMissingChunks, chunkSize, length));
    }

    @Test
    public void testUploadWithoutExistingChunksSmallChunks() {
        long chunkSize = 512;
        long length = 4 * chunkSize;

        List<Chunk> existingChunks = new ArrayList<>();

        List<Chunk> expectedMissingChunks = new ArrayList<>();
        expectedMissingChunks.add(new Chunk(0, 511));
        expectedMissingChunks.add(new Chunk(512, 1023));
        expectedMissingChunks.add(new Chunk(1024, 1535));
        expectedMissingChunks.add(new Chunk(1536, 2047));
        expectedMissingChunks.add(new Chunk(2048, 2048));

        assertTrue(test(existingChunks, expectedMissingChunks, chunkSize, length));
    }

    @Test
    public void testChunksSmallSizeMissingChunks() {
        long chunkSize = 512;
        long length = 4 * chunkSize;

        List<Chunk> existingChunks = new ArrayList<>();
        existingChunks.add(new Chunk(50, 89));
        existingChunks.add(new Chunk(551, 580));

        List<Chunk> expectedMissingChunks = new ArrayList<>();
        expectedMissingChunks.add(new Chunk(0, 49));
        expectedMissingChunks.add(new Chunk(90, 550));
        expectedMissingChunks.add(new Chunk(581, 1092));
        expectedMissingChunks.add(new Chunk(1093, 1604));
        expectedMissingChunks.add(new Chunk(1605, 2048));

        assertTrue(test(existingChunks, expectedMissingChunks, chunkSize, length));
    }

    @Test
    public void testUploadChunksMissingChunks() {
        long length = 2 * chunkSize;

        List<Chunk> existingChunks = new ArrayList<>();
        existingChunks.add(new Chunk(0, 1023));
        existingChunks.add(new Chunk(1028, 1100));

        List<Chunk> expectedMissingChunks = new ArrayList<>();
        expectedMissingChunks.add(new Chunk(1024, 1027));
        expectedMissingChunks.add(new Chunk(1101, 2048));

        assertTrue(test(existingChunks, expectedMissingChunks, chunkSize, length));
    }

    @Test
    public void testUploadChunksMissingChunks2() {
        long length = 2 * chunkSize;

        List<Chunk> existingChunks = new ArrayList<>();
        existingChunks.add(new Chunk(50, 1023));
        existingChunks.add(new Chunk(1028, 1100));

        List<Chunk> expectedMissingChunks = new ArrayList<>();
        expectedMissingChunks.add(new Chunk(0, 49));
        expectedMissingChunks.add(new Chunk(1024, 1027));
        expectedMissingChunks.add(new Chunk(1101, 2048));

        assertTrue(test(existingChunks, expectedMissingChunks, chunkSize, length));
    }

    private boolean test(List<Chunk> existingChunks,
                         List<Chunk> expectedMissingChunks,
                         long chunkSize,
                         long length) {
        String modificationTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        ChunkedFileUploadRemoteOperation sut = new ChunkedFileUploadRemoteOperation(null,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    modificationTimestamp,
                                                                                    false);

        List<Chunk> missingChunks = sut.checkMissingChunks(existingChunks, length, chunkSize);

        assertEquals(expectedMissingChunks.size(), missingChunks.size());

        for (Chunk expectedMissingChunk : expectedMissingChunks) {
            assertTrue(missingChunks.contains(expectedMissingChunk));
        }

        return true;
    }
}
