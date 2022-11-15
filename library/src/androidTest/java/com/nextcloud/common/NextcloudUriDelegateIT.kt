package com.nextcloud.common

import android.net.Uri
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NextcloudUriDelegateIT {

    lateinit var sut: NextcloudUriDelegate

    @Before
    fun setUp() {
        sut = NextcloudUriDelegate(Uri.parse(BASEURL), USERID)
    }

    @Test
    fun testFilesDavUri_leadingSlashInPath() {
        val expected = "$EXPECTED_FILES_DAV/path/to/file.txt"
        val actual = sut.getFilesDavUri("/path/to/file.txt")
        Assert.assertEquals("Wrong URL", expected, actual)
    }

    @Test
    fun testFilesDavUri_noLeadingSlashInPath() {
        val expected = "$EXPECTED_FILES_DAV/path/to/file.txt"
        val actual = sut.getFilesDavUri("path/to/file.txt")
        Assert.assertEquals("Wrong URL", expected, actual)
    }

    companion object {
        private const val USERID = "user"
        private const val BASEURL = "http://test.localhost"
        private const val EXPECTED_FILES_DAV = "$BASEURL/remote.php/dav/files/$USERID"
    }
}
