/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

import junit.framework.TestCase.assertEquals
import org.junit.Test

class OCCapabilityTest {
    /**
     * This won't compile if the fields below are not nullable. This is sort of redundant,
     * but it's meant to prevent a crash in client apps when trying to assign null from Java.
     */
    @Test
    fun testFieldNullability() {
        OCCapability().apply {
            accountName = null
            versionString = null
            versionEdition = null
            serverName = null
            serverSlogan = null
            serverColor = null
            serverTextColor = null
            serverElementColor = null
            serverElementColorBright = null
            serverElementColorDark = null
            serverLogo = null
            serverBackground = null
            richDocumentsMimeTypeList = null
            richDocumentsOptionalMimeTypeList = null
            richDocumentsProductName = null
            directEditingEtag = null
        }
    }

    @Test
    @Suppress("LongMethod")
    fun parseDeclarativeUI() {
        val string = """{
              "analytics": {
                "context-menu": [
                  {
                    "name": "Show data in Analytics",
                    "url": "\/ocs\/v2.php\/apps\/analytics\/createFromDataFile?fileId={fileId}",
                    "method": "POST",
                    "mimetype_filters": "text\/csv",
                    "params": {
                        'file_id': '{fileId}',
                        'file_path': '{filePath}'
                    },
                    "icon": "\/apps\/analytics\/img\/app.svg"
                  }
                ],
                 "create-new": [
                  {
                    "name": "Analytic chart",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/newChart"
                  }
                  ]
              },
              "declarativetest": {
                "context-menu": [
                  {
                    "name": "List all UI elements",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/all"
                  },
                  {
                    "name": "First version",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/version1"
                  },
                  {
                    "name": "Convert to PDF",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/convertFile\/123123",
                    "mimetype_filters": "image\/"
                  },
                  {
                    "name": "Create transcript",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/version1",
                    "mimetype_filters": "audio\/",
                    "android_icon": "file_sound"
                  },
                  {
                    "name": "Create zip file",
                    "url": "\/ocs\/v2.php\/apps\/file_zip\/zip\/1231123",
                    "android_icon": "file_zip",
                    "ios_icon": "zip",
                    "desktop_icon": "zip"
                  },
                  {
                    "name": "Unzip",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/version1",
                    "mimetype_filters": "application\/zip",
                    "android_icon": "file_zip"
                  }
                ],
                "create-new": [
                  {
                    "name": "Deck board",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/newDeckBoard"
                  },
                  {
                    "name": "New Contact",
                    "url": "\/ocs\/v2.php\/apps\/declarativetest\/newContact",
                    "android_icon": "file_vcard"
                  }
                ]
              }
            }"""

        val sut = OCCapability()
        sut.declarativeUiJson = string

        // Markdown
        assertEquals(3, sut.getDeclarativeUiEndpoints(Type.CONTEXT_MENU, "text/markdown").size.toLong())
        assertEquals(3, sut.getDeclarativeUiEndpoints(Type.CREATE_NEW, "text/markdown").size.toLong())

        // Zip
        assertEquals(4, sut.getDeclarativeUiEndpoints(Type.CONTEXT_MENU, "application/zip").size.toLong())
        assertEquals(3, sut.getDeclarativeUiEndpoints(Type.CREATE_NEW, "application/zip").size.toLong())
    }
}
