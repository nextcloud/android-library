package com.owncloud.android.lib.common.network

import com.owncloud.android.lib.common.network.AdvancedX509KeyManager.AKMAlias
import org.junit.Assert
import org.junit.Test

class AdvancedX509KeyManagerTests {

    @Test
    fun testAKMAlias_matches() {
        val akmAlias1 = AKMAlias(AKMAlias.Type.KEYCHAIN, "alias", "hostname", 123)
        val akmAlias2 = AKMAlias(AKMAlias.Type.KEYCHAIN, "alias", "hostname", 123)

        Assert.assertTrue(akmAlias1.matches(akmAlias1))
        Assert.assertTrue(akmAlias1.matches(akmAlias2))

        val akmAlias3 = AKMAlias(AKMAlias.Type.KEYSTORE, "alias", "hostname", 123)
        Assert.assertFalse(akmAlias1.matches(akmAlias3))

        val akmAlias4 = AKMAlias(AKMAlias.Type.KEYCHAIN, "alias1", "hostname", 123)
        Assert.assertFalse(akmAlias1.matches(akmAlias4))

        val akmAlias5 = AKMAlias(AKMAlias.Type.KEYCHAIN, "alias", "hostname1", 123)
        Assert.assertFalse(akmAlias1.matches(akmAlias5))

        val akmAlias6 = AKMAlias(AKMAlias.Type.KEYCHAIN, "alias", "hostname", 1234)
        Assert.assertFalse(akmAlias1.matches(akmAlias6))

        // parameters being null are considered "do-not-care"
        val akmAlias7 = AKMAlias(null, "alias", "hostname", 123)
        Assert.assertTrue(akmAlias1.matches(akmAlias7))

        val akmAlias8 = AKMAlias(AKMAlias.Type.KEYCHAIN, null, "hostname", 123)
        Assert.assertTrue(akmAlias1.matches(akmAlias8))

        val akmAlias9 = AKMAlias(AKMAlias.Type.KEYCHAIN, "alias", null, 123)
        Assert.assertTrue(akmAlias1.matches(akmAlias9))

        val akmAlias10 = AKMAlias(null, null, null, 123)
        Assert.assertTrue(akmAlias1.matches(akmAlias10))

    }

}