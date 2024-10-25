package global.covesa.sdk.client

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import org.unifiedpush.android.connector.UnifiedPush
import org.unifiedpush.android.connector.data.PushEndpoint
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

/**
 * This class emulates an application server
 */
class MockApplicationServer(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREF_MASTER, Context.MODE_PRIVATE)

    /** Push endpoint. Should be saved on application server. */
    private var endpoint: String?
        get() = prefs.getString(PREF_ENDPOINT, null)
        set(value) {
            if (value == null) {
                prefs.edit().remove(PREF_ENDPOINT).apply()
            } else {
                prefs.edit().putString(PREF_ENDPOINT, value).apply()
            }
        }

    /** WebPush auth secret. Should be saved on application server. */
    private var authSecret: String?
        get() = prefs.getString(PREF_AUTHKEY, null)
        set(value) = prefs.edit().putString(PREF_AUTHKEY, value).apply()

    /** WebPush public key. Should be saved on application server. */
    private var pubKey: String?
        get() = prefs.getString(PREF_PUBKEY, null)
        set(value) = prefs.edit().putString(PREF_PUBKEY, value).apply()

    /** VAPID public key. Should be saved on application server. */
    private var vapidPubKey: String?
        get() = prefs.getString(PREF_VAPID_PUBKEY, null)
        set(value) = prefs.edit().putString(PREF_VAPID_PUBKEY, value).apply()

    /**
     * Emulate API to send and receive data with the server
     */
    inner class MockApi {
        /**
         * Save the endpoint on the application server
         *
         * This function should actually send to the server, here we just save locally
         */
        fun storePushEndpoint(endpoint: PushEndpoint?) {
            this@MockApplicationServer.endpoint = endpoint?.url
            authSecret = endpoint?.pubKeySet?.auth
            pubKey = endpoint?.pubKeySet?.pubKey
        }

        /**
         * Get the application server VAPID pubkey
         */
        fun getVapidPubKey(): String {
            return vapidPubKey
                // This store the generated key
                ?: (genVapidKey().public as ECPublicKey).encode()
        }
    }

    /**
     * Generate a new KeyPair for VAPID on the fake server side
     */
    private fun genVapidKey(): KeyPair {
        Log.d(TAG, "Generating a new KP.")
        val generator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_PROVIDER)
        generator.initialize(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKeyPair().also {
            val pubkey = (it.public as ECPublicKey).encode()
            Log.d(TAG, "Pubkey: $pubkey")
            vapidPubKey = pubkey
        }
    }


    private companion object {
        const val TAG = "MockApplicationServer"
        const val PREF_MASTER = "MockApplicationServer"
        const val PREF_ENDPOINT = "endpoint"
        const val PREF_PUBKEY = "pubkey"
        const val PREF_AUTHKEY = "authkey"
        const val PREF_VAPID_PUBKEY = "vapidPubkey"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val ALIAS = "ApplicationServer"
    }
}