/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.tos

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class TermsOfServicesIT : AbstractIT() {
    // @Test disabled for now as no good way to test on CI
    fun getAndSignTerms() {
        // user 3 with ToS
        var result = GetTermsRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        var terms = result.resultData
        assertTrue(terms.terms.isNotEmpty())
        assertFalse(terms.hasSigned)

        val id = terms.terms[0].id

        // sign
        assertTrue(SignTermRemoteOperation(id).execute(nextcloudClient).isSuccess)

        // signed terms
        result = GetTermsRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        terms = result.resultData
        assertTrue(terms.terms.isNotEmpty())
        assertTrue(terms.hasSigned)
    }
}
