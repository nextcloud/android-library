/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.clientintegration

import com.nextcloud.android.lib.resources.clientintegration.Button
import com.nextcloud.android.lib.resources.clientintegration.GetClientIntegrationJsonOperation
import com.nextcloud.android.lib.resources.clientintegration.Image
import com.nextcloud.android.lib.resources.clientintegration.Orientation
import com.nextcloud.android.lib.resources.clientintegration.Text
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GetClientIntegrationJsonOperationTest {
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

        val sut = GetClientIntegrationJsonOperation("")

        val clientIntegrationUI = sut.parseResult(string)

        assertEquals(0.1, clientIntegrationUI.version)
        assertEquals(Orientation.VERTICAL, clientIntegrationUI.root.orientation)
        assertEquals(2, clientIntegrationUI.root.rows.count())

        // row 1
        assertEquals(
            2,
            clientIntegrationUI.root.rows[0]
                .children
                .count()
        )

        val button1 = clientIntegrationUI.root.rows[0].children[0] as Button
        assertEquals("Submit", button1.label)

        // row 2
        assertEquals(
            2,
            clientIntegrationUI.root.rows[1]
                .children
                .count()
        )
        val text = clientIntegrationUI.root.rows[1].children[0] as Text
        assertEquals("Hello World!", text.text)

        val image = clientIntegrationUI.root.rows[1].children[1] as Image
        assertEquals("/core/img/logo/logo.png", image.url)
    }
}
