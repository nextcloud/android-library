/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.declarativeui

import com.nextcloud.android.lib.resources.declarativeui.GetDeclarativeUiJsonOperation
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GetDeclarativeUiJsonOperationTest {
    @Test
    fun testParseJson() {
        val string1 = """{
            "version": 0.1,
            "root": Layout {
            }
                
            }
  "Button": {
    "label": "Submit",
    "type": "primary",
  },
  "Image": {
    "url": "/core/img/logo/logo.png"
  }
}"""

        val string = """{
            "version": "0.1"
             }
            """

        val sut = GetDeclarativeUiJsonOperation("")

        val declarativeUI = sut.parseResult(string)

        assertEquals(0.1, declarativeUI.version)
    }
}
