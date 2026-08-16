package com.dmesh.prototype.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.SecureRandom

class NodeIdentityManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("dmesh_identity", Context.MODE_PRIVATE)
    private val keyAlias = "dmesh_node_key"
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    }

    fun getOrCreateNodeId(): String {
        val existing = prefs.getString("node_id", null)
        if (existing != null) return existing
        val id = "NODE-" + SecureRandom().nextInt(0xFFFFFF).toString(16).uppercase().padStart(6, '0')
        prefs.edit().putString("node_id", id).apply()
        return id
    }

    fun getDisplayName(): String = prefs.getString("display_name", "Mesh Node") ?: "Mesh Node"

    fun setDisplayName(name: String) {
        prefs.edit().putString("display_name", name).apply()
    }

    fun ensureKeyPair() {
        if (keyStore.containsAlias(keyAlias)) return
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
            .apply { initialize(spec) }
            .generateKeyPair()
    }

    fun signPayload(payload: String): String {
        ensureKeyPair()
        val entry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(entry.privateKey)
        signature.update(payload.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    fun verifySignature(payload: String, signatureBase64: String, publicKeyBytes: ByteArray? = null): Boolean {
        if (signatureBase64.isBlank()) return false
        return runCatching {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            val publicKey = entry?.certificate?.publicKey
            if (publicKey == null) return false
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(payload.toByteArray(Charsets.UTF_8))
            sig.verify(Base64.decode(signatureBase64, Base64.NO_WRAP))
        }.getOrDefault(false)
    }

    fun hashPayload(payload: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(payload.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
