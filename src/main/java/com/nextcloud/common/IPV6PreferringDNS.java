package com.nextcloud.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Dns;

/**
 * Implementation of DNS which prefers IPV6 addresses (if available) over IPV4.
 */
public class IPV6PreferringDNS implements Dns {

    private Map<String, ArrayList<InetAddress>> cache = new HashMap<>();

        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            ArrayList<InetAddress> addresses = cache.get(hostname.toLowerCase(Locale.ROOT));

            if (addresses != null) {
                return addresses;
            }

            addresses = new ArrayList<>(Dns.SYSTEM.lookup(hostname));
            Collections.sort(addresses, (address1, address2) -> {
                if (address1 instanceof Inet4Address) {
                    return 1;
                } else {
                    return -1;
                }
            });
            cache.put(hostname, addresses);

            return addresses;
        }
    }
