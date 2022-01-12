package com.nextcloud.common

import okhttp3.Dns
import java.net.InetAddress

object IPV6PreferringDNS : Dns {

    override fun lookup(hostname: String): List<InetAddress> {
        return DNSCache.lookup(hostname)
    }
}
