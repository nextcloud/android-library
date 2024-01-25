package com.owncloud.android.lib.common.network

import com.owncloud.android.lib.common.network.AdvancedX509KeyManager.AKMAlias
import org.junit.Assert
import org.junit.Test

private const val PORT_SAME = 123
private const val PORT_OTHER = 1234

private const val ALIAS_SAME = "alias"
private const val ALIAS_OTHER = "alias1"

private const val HOST_SAME = "hostname"
private const val HOST_OTHER = "hostname1"

class AdvancedX509KeyManagerTests {
    @Test
    fun testAKMAliasMatches() {
        val akmAlias1 = AKMAlias(AKMAlias.Type.KEYCHAIN, ALIAS_SAME, HOST_SAME, PORT_SAME)
        val akmAlias2 = AKMAlias(AKMAlias.Type.KEYCHAIN, ALIAS_SAME, HOST_SAME, PORT_SAME)

        Assert.assertTrue(akmAlias1.matches(akmAlias1))
        Assert.assertTrue(akmAlias1.matches(akmAlias2))

        val akmAlias3 = AKMAlias(AKMAlias.Type.KEYSTORE, ALIAS_SAME, HOST_SAME, PORT_SAME)
        Assert.assertFalse(akmAlias1.matches(akmAlias3))

        val akmAlias4 = AKMAlias(AKMAlias.Type.KEYCHAIN, ALIAS_OTHER, HOST_SAME, PORT_SAME)
        Assert.assertFalse(akmAlias1.matches(akmAlias4))

        val akmAlias5 = AKMAlias(AKMAlias.Type.KEYCHAIN, ALIAS_SAME, HOST_OTHER, PORT_SAME)
        Assert.assertFalse(akmAlias1.matches(akmAlias5))

        val akmAlias6 = AKMAlias(AKMAlias.Type.KEYCHAIN, ALIAS_SAME, HOST_SAME, PORT_OTHER)
        Assert.assertFalse(akmAlias1.matches(akmAlias6))

        // parameters being null are considered "do-not-care"
        val akmAlias7 = AKMAlias(null, ALIAS_SAME, HOST_SAME, PORT_SAME)
        Assert.assertTrue(akmAlias1.matches(akmAlias7))

        val akmAlias8 = AKMAlias(AKMAlias.Type.KEYCHAIN, null, HOST_SAME, PORT_SAME)
        Assert.assertTrue(akmAlias1.matches(akmAlias8))

        val akmAlias9 = AKMAlias(AKMAlias.Type.KEYCHAIN, ALIAS_SAME, null, PORT_SAME)
        Assert.assertTrue(akmAlias1.matches(akmAlias9))

        val akmAlias10 = AKMAlias(null, null, null, PORT_SAME)
        Assert.assertTrue(akmAlias1.matches(akmAlias10))
    }
}
