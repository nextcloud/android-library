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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.w3c.dom.Element

class XmlDataProcessorTest {
    @Test
    fun testProcessXmlDataWhenGivenEmptyArrayListShouldReturnNull() {
        val tag = "width"
        val xmlData: ArrayList<Element> = arrayListOf()
        val result = xmlData.processXmlData<Float>(tag)
        assertNull(result)
    }

    @Test
    fun testProcessXmlDataWhenGivenWrongArrayListShouldReturnNull() {
        val tag = "width"
        val xmlData: ArrayList<String> = arrayListOf("element")
        val result = xmlData.processXmlData<Float>(tag)
        assertNull(result)
    }

    @Test
    fun testProcessXmlDataWhenGivenValidDataShouldReturnFloat() {
        val tag = "width"
        val element = createElement(tag, "220")
        val xmlData: ArrayList<Element> = arrayListOf(element)
        val result = xmlData.processXmlData<Float>(tag)
        assertEquals(220f, result)
    }

    @Test
    fun testProcessXmlDataWhenGivenValidDataAndWrongTagShouldReturnNull() {
        val element = createElement("width", "220")
        val xmlData: ArrayList<Element> = arrayListOf(element)
        val result = xmlData.processXmlData<Float>("latitude")
        assertNull(result)
    }

    @Test
    fun testProcessXmlDataWhenGivenNullElementShouldReturnNull() {
        val tag = "width"
        val xmlData: ArrayList<Element?> = arrayListOf(null)
        val result = xmlData.processXmlData<Float>(tag)
        assertNull(result)
    }

    @Test
    fun testProcessXmlDataWhenGivenValidDataShouldReturnDouble() {
        val tag = "latitude"
        val element = createElement(tag, "12.4231")
        val xmlData: ArrayList<Element> = arrayListOf(element)
        val result = xmlData.processXmlData<Double>(tag)
        assertEquals(12.4231, result)
    }

    @Test
    fun testProcessXmlDataWhenGivenDifferentValueTypeShouldReturnNull() {
        val tag = "latitude"
        val element = createElement(tag, "StringData")
        val xmlData: ArrayList<Element> = arrayListOf(element)
        val result = xmlData.processXmlData<Float>(tag)
        assertNull(result)
    }
}
