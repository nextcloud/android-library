/*
 * Nextcloud Android client application
 *
 * @author Alper Ozturk
 * Copyright (C) 2024 Alper Ozturk
 * Copyright (C) 2024 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.nextcloud.extensions

import com.google.gson.Gson
import org.apache.jackrabbit.webdav.property.DavProperty
import org.apache.jackrabbit.webdav.property.DavPropertyName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

class DavPropertyProcessorTest {
    data class TestData(val name: String, val age: Int)

    private val gson = Gson()

    @Test
    fun testFromDavPropertyWhenGivenValidDataShouldReturnExpectedData() {
        val result =
            gson.fromDavProperty<TestData>(
                object : DavProperty<String> {
                    override fun toXml(document: Document?): Element {
                        return createElement("TestData", value)
                    }

                    override fun getName(): DavPropertyName {
                        return DavPropertyName.DISPLAYNAME
                    }

                    override fun getValue(): String {
                        return "{\"name\":\"John\",\"age\":55}"
                    }

                    override fun isInvisibleInAllprop(): Boolean {
                        return true
                    }
                }
            )
        val expected = TestData("John", 55)
        assertEquals(expected, result)
    }

    @Test
    fun testFromDavPropertyWhenGivenValidDataAndExpectDifferentTypeShouldReturnNull() {
        val result =
            gson.fromDavProperty<ArrayList<String>>(
                object : DavProperty<String> {
                    override fun toXml(document: Document?): Element {
                        return createElement("TestData", value)
                    }

                    override fun getName(): DavPropertyName {
                        return DavPropertyName.DISPLAYNAME
                    }

                    override fun getValue(): String {
                        return "{\"name\":\"John\",\"age\":55}"
                    }

                    override fun isInvisibleInAllprop(): Boolean {
                        return true
                    }
                }
            )
        assertNull(result)
    }

    @Test
    fun testFromDavPropertyWhenGivenInvalidDataShouldReturnNull() {
        val result =
            gson.fromDavProperty<TestData>(
                object : DavProperty<String?> {
                    override fun toXml(document: Document?): Element {
                        return createElement("TestData", "")
                    }

                    override fun getName(): DavPropertyName {
                        return DavPropertyName.DISPLAYNAME
                    }

                    override fun getValue(): String? {
                        return null
                    }

                    override fun isInvisibleInAllprop(): Boolean {
                        return true
                    }
                }
            )
        assertNull(result)
    }

    @Test
    fun testFromDavPropertyWhenGivenNullDataShouldReturnNull() {
        val result = gson.fromDavProperty<TestData>(null)
        assertNull(result)
    }
}
