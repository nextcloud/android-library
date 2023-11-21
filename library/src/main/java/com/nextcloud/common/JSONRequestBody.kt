package com.nextcloud.common

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class JSONRequestBody() {
    private val content = mutableMapOf<String, String>()

    constructor(key: String, value: String): this() {
        put(key, value)
    }

    fun put(key: String, value: String): JSONRequestBody {
        content[key] = value
        return this
    }

    fun get(): RequestBody {
        val json = Gson().toJson(content)
        return json.toRequestBody(JSON_MEDIATYPE)
    }

    override fun toString(): String {
        return content.toString()
    }

    companion object {
        private val JSON_MEDIATYPE = "application/json; charset=utf-8".toMediaType()
    }
}