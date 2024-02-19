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
