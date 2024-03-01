/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2023 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File
import kotlin.math.ceil

class ChunkedFileUploadRemoteOperationTest {
    @Mock
    var file: File? = null

    @Test
    fun testAssembleTimeout() {
        MockitoAnnotations.openMocks(this)
        val sut =
            ChunkedFileUploadRemoteOperation(
                null,
                null,
                null,
                null,
                System.currentTimeMillis() / 1000,
                false
            )

        // 0b
        Mockito.`when`(file!!.length()).thenReturn(0L)
        assertEquals(sut.ASSEMBLE_TIME_MIN, sut.calculateAssembleTimeout(file))

        // 100b
        Mockito.`when`(file!!.length()).thenReturn(100L)
        assertEquals(sut.ASSEMBLE_TIME_MIN, sut.calculateAssembleTimeout(file))

        // 1Mb
        Mockito.`when`(file!!.length()).thenReturn(1 * MB)
        assertEquals(sut.ASSEMBLE_TIME_MIN, sut.calculateAssembleTimeout(file))

        // 100Mb
        Mockito.`when`(file!!.length()).thenReturn(100 * MB)
        assertEquals(sut.ASSEMBLE_TIME_MIN, sut.calculateAssembleTimeout(file))

        // 1Gb
        Mockito.`when`(file!!.length()).thenReturn(1 * GB)
        assertEquals(sut.ASSEMBLE_TIME_PER_GB, sut.calculateAssembleTimeout(file))

        // 2Gb
        Mockito.`when`(file!!.length()).thenReturn(2 * GB)
        assertEquals((2 * sut.ASSEMBLE_TIME_PER_GB), sut.calculateAssembleTimeout(file))

        // 5Gb
        Mockito.`when`(file!!.length()).thenReturn(5 * GB)
        assertEquals((5 * sut.ASSEMBLE_TIME_PER_GB), sut.calculateAssembleTimeout(file))

        // 50Gb
        Mockito.`when`(file!!.length()).thenReturn(50 * GB)
        assertEquals(sut.ASSEMBLE_TIME_MAX, sut.calculateAssembleTimeout(file))

        // 500Gb
        Mockito.`when`(file!!.length()).thenReturn(500 * GB)
        assertEquals(sut.ASSEMBLE_TIME_MAX, sut.calculateAssembleTimeout(file))
    }

    @Test
    fun testChunkEquality() {
        val chunk1 = Chunk(0, 0, 5)
        val chunk2 = Chunk(0, 0, 5)
        val differentStart = Chunk(0, 1, 5)
        val differentLength = Chunk(0, 0, 6)
        val differentId = Chunk(1, 0, 5)
        val differentAll = Chunk(1, 1, 1)

        assertEquals(chunk1, chunk2)
        assertNotEquals(chunk1, null)
        assertNotEquals(chunk1, differentStart)
        assertNotEquals(chunk1, differentLength)
        assertNotEquals(chunk1, differentId)
        assertNotEquals(chunk1, differentAll)
        assertEquals(chunk1.hashCode(), chunk2.hashCode())
    }

    @Test
    fun testChunking() {
        listOf(1 * MB, 10 * MB, 100 * MB, 1 * GB).forEach { length ->
            checkChunks(length, ChunkedFileUploadRemoteOperation.CHUNK_SIZE_MOBILE, 0)
            checkChunks(length, ChunkedFileUploadRemoteOperation.CHUNK_SIZE_WIFI, 0)
        }
    }

    @Test
    fun testChunkingResume() {
        // test chunking with offset (chunks already on server)
        // -2: last byte missing (because the file starts at 0B, file.length() at 1; 1B offset)
        listOf(1, 1 * MB, 10 * MB, 100 * MB, 256 * MB, 1 * GB - 2).forEach { offset ->
            checkChunks(1 * GB, ChunkedFileUploadRemoteOperation.CHUNK_SIZE_MOBILE, offset)
            checkChunks(1 * GB, ChunkedFileUploadRemoteOperation.CHUNK_SIZE_WIFI, offset)
        }
    }

    @Test
    fun testChunkingChangeover() {
        val length = 1 * GB
        val chunks = mutableListOf<Chunk>()
        var id = 0
        var nextByte = 0L

        // segment first half with CHUNK_SIZE_WIFI
        while (nextByte + 1 < length / 2) {
            val chunk =
                ChunkedFileUploadRemoteOperation.calcNextChunk(
                    length,
                    ++id,
                    nextByte,
                    ChunkedFileUploadRemoteOperation.CHUNK_SIZE_WIFI
                )

            chunks.add(chunk)
            nextByte += chunk.length
        }

        // segment remaining half with CHUNK_SIZE_MOBILE
        while (nextByte + 1 < length) {
            val chunk =
                ChunkedFileUploadRemoteOperation.calcNextChunk(
                    length,
                    ++id,
                    nextByte,
                    ChunkedFileUploadRemoteOperation.CHUNK_SIZE_MOBILE
                )

            chunks.add(chunk)
            nextByte += chunk.length
        }

        // calculate expected number of chunks
        var expectedChunkCount =
            ceil((length / 2) / ChunkedFileUploadRemoteOperation.CHUNK_SIZE_WIFI.toFloat())
        expectedChunkCount +=
            ceil(
                (length - expectedChunkCount * ChunkedFileUploadRemoteOperation.CHUNK_SIZE_WIFI) /
                    ChunkedFileUploadRemoteOperation.CHUNK_SIZE_MOBILE.toFloat()
            )
        assertEquals(expectedChunkCount.toInt(), chunks.size)

        // does total length match file size?
        assertEquals(length, chunks.sumOf(Chunk::length))

        // ids distinct?
        assertEquals(chunks.size, chunks.distinctBy(Chunk::id).size)

        // all bytes contained?
        val cumulativeSize =
            chunks.sortedBy(Chunk::start).fold(0L) { acc, chunk ->
                assertEquals(acc, chunk.start)
                acc + chunk.length
            }

        // does total length match file size?
        assertEquals(length, cumulativeSize)
    }

    private fun checkChunks(
        length: Long,
        chunkSize: Long,
        offset: Long
    ) {
        val chunks = segmentFile(offset, length, chunkSize).sortedBy(Chunk::id)

        // as many chunks as expected?
        assertEquals(ceil((length - offset) / chunkSize.toFloat()).toInt(), chunks.size)

        // does total length match file size?
        assertEquals(length - offset, chunks.sumOf(Chunk::length))

        // ids distinct?
        assertEquals(chunks.size, chunks.distinctBy(Chunk::id).size)

        // all bytes contained?
        val cumulativeSize =
            chunks.sortedBy(Chunk::start).fold(offset) { acc, chunk ->
                assertEquals(acc, chunk.start)
                acc + chunk.length
            }

        // does total length match file size?
        assertEquals(length, cumulativeSize)
    }

    private fun segmentFile(
        firstByte: Long,
        length: Long,
        chunkSize: Long
    ): List<Chunk> {
        val chunks = mutableListOf<Chunk>()
        var id = 0
        var nextByte = firstByte

        while (nextByte + 1 < length) {
            val chunk =
                ChunkedFileUploadRemoteOperation.calcNextChunk(
                    length,
                    ++id,
                    nextByte,
                    chunkSize
                )

            chunks.add(chunk)
            nextByte += chunk.length
        }

        return chunks
    }

    companion object {
        private const val MB = 1000 * 1000L
        private const val GB = 1000 * MB
    }
}
