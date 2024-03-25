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
