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
    @Suppress("LongMethod")
    fun testParseJson() {
        val string = """
            {
                "version": 0.1,
                "root": {
                    "orientation": "vertical",
                    "rows": [
                        {
                            "children": [
                                    {
                                        "element": "Button",
                                        "type": "primary",
                                        "label": "Submit"
                                    },
                                    {
                                        "element": "Button",
                                        "type": "secondary",
                                        "label": "Cancel"
                                    }
                            ]
                        },
                        {
                            "children": [
                                {
                                    "element": "Text",
                                    "text": "Hello World!"
                                }, 
                                {
                                    "element": "Image",
                                    "url": "/core/img/logo/logo.png"
                                }
                            ]
                        }
                    ]
                }
            }
            """

        val sut = GetDeclarativeUiJsonOperation("")

        val declarativeUI = sut.parseResult(string)

        assertEquals(0.1, declarativeUI.version)
        assertEquals(Orientation.VERTICAL, declarativeUI.root.orientation)
        assertEquals(2, declarativeUI.root.rows.count())

        // row 1
        assertEquals(
            2,
            declarativeUI.root.rows[0]
                .children
                .count()
        )

        val button1 = declarativeUI.root.rows[0].children[0] as Button
        assertEquals("Submit", button1.label)

        // row 2
        assertEquals(
            2,
            declarativeUI.root.rows[1]
                .children
                .count()
        )
        val text = declarativeUI.root.rows[1].children[0] as Text
        assertEquals("Hello World!", text.text)

        val image = declarativeUI.root.rows[1].children[1] as Image
        assertEquals("/core/img/logo/logo.png", image.url)
    }
}
