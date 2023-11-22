package com.nextcloud.extensions

import com.google.gson.Gson
import org.apache.jackrabbit.webdav.property.DavProperty

inline fun <reified T> Gson.fromDavProperty(davProperty: DavProperty<*>?): T? {
    return if (davProperty != null && davProperty.value != null) {
        fromJson(davProperty.value.toString(), T::class.java)
    } else {
        null
    }
}
