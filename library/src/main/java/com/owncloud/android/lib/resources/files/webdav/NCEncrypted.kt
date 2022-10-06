package com.owncloud.android.lib.resources.files.webdav

import android.text.TextUtils
import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import com.owncloud.android.lib.common.network.WebdavEntry
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCEncrypted private constructor(var isNcEncrypted: Boolean) : Property {

    class Factory : PropertyFactory {
        override fun create(parser: XmlPullParser): Property {
            try {
                val text = XmlUtils.readText(parser)
                if (!TextUtils.isEmpty(text)) {
                    return NCEncrypted("1" == text)
                }
            } catch (e: IOException) {
                Log.e("NCEncrypted", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCEncrypted", "failed to create property", e)
            }
            return NCEncrypted(false)
        }

        override fun getName(): Property.Name {
            return NAME
        }
    }

    companion object {
        @JvmField
        val NAME: Property.Name =
            Property.Name(WebdavEntry.NAMESPACE_NC, WebdavEntry.EXTENDED_PROPERTY_IS_ENCRYPTED)
    }
}
