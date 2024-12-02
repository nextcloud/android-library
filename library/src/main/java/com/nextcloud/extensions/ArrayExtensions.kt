/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.extensions

import at.bitfire.dav4jvm.Property
import org.apache.jackrabbit.webdav.property.DavPropertyName
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.xml.Namespace

/**
 * Returns DavPropertyNameSet for given array of Property.Name.
 *
 * remove - only intended as a transitional aid
 */
fun Array<Property.Name>.toLegacyPropset(): DavPropertyNameSet {
    val propertySet = DavPropertyNameSet()
    for (property in this) {
        propertySet.add(DavPropertyName.create(property.name, Namespace.getNamespace(property.namespace)))
    }
    return propertySet
}
