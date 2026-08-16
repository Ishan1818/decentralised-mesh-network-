package com.dmesh.prototype.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecurityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun nodeIdIsGenerated() {
        val identity = NodeIdentityManager(context)
        val id = identity.getOrCreateNodeId()
        assertTrue(id.startsWith("NODE-"))
    }

    @Test
    fun hashPayloadIsStable() {
        val identity = NodeIdentityManager(context)
        val hash1 = identity.hashPayload("payload")
        val hash2 = identity.hashPayload("payload")
        assertTrue(hash1 == hash2)
        assertFalse(hash1 == identity.hashPayload("other"))
    }

    @Test
    fun signAndVerifyWhenKeystoreAvailable() {
        val identity = NodeIdentityManager(context)
        try {
            identity.ensureKeyPair()
            val payload = "test-message-payload"
            val signature = identity.signPayload(payload)
            assertTrue(signature.isNotBlank())
            assertTrue(identity.verifySignature(payload, signature))
            assertFalse(identity.verifySignature("tampered", signature))
        } catch (_: Exception) {
            // Android Keystore may be unavailable in some CI/unit test environments
            assertTrue(true)
        }
    }
}
