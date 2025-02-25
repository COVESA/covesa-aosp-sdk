package global.covesa.sdk.client.push

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.crypto.tink.apps.webpush.WebPushHybridEncrypt
import com.google.crypto.tink.subtle.EllipticCurves
import global.covesa.sdk.api.client.push.data.PushEndpoint
import org.json.JSONObject
import java.net.URL
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

/**
 * This class emulates an application server, this should not be implemented
 * by real applications.
 */
class FakeApplicationServer(private val context: Context) {

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
            this@FakeApplicationServer.endpoint = endpoint?.url
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

        /**
         * Send a notification
         */
        fun sendNotification() {
            val content = "This is a notification from server"
            sendWebPushNotification(content) { responseString, e ->
                var message = ""
                e?.let {
                    message = "An error occurred: $e with message ${e.cause?.localizedMessage}, data: ${e.networkResponse.data.decodeToString()} and code: ${e.networkResponse.statusCode}"
                }
                responseString?.let {
                    message = "Notification sent with content: $content"
                }
                context.mainExecutor.execute {
                    Log.i(TAG, message)
                }
            }
        }
    }

    /**
     * Send a notification encrypted with RFC8291
     */
    private fun sendWebPushNotification(content: String, callback: (response: String?, error: VolleyError?) -> Unit) {
        Log.w(TAG, "Sending WebPushNotification with content $content")
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val url = endpoint
        val request = object :
            StringRequest(
                Method.POST,
                url,
                Response.Listener { r ->
                    callback(r, null)
                },
                Response.ErrorListener { e ->
                    callback(null, e)
                },
            ) {
            override fun getBody(): ByteArray {
                val auth = authSecret?.b64decode()
                val hybridEncrypt =
                    WebPushHybridEncrypt.Builder()
                        .withAuthSecret(auth)
                        .withRecipientPublicKey(pubKey?.decodePubKey() as ECPublicKey)
                        .build()
                return hybridEncrypt.encrypt(content.toByteArray(), null)
            }

            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Encoding"] = "aes128gcm"
                params["TTL"] = "60"
                params["Urgency"] = "high"
                params["Authorization"] = getVapidHeader()
                return params
            }
        }
        requestQueue.add(request)
    }

    /**
     * Generate VAPID header for the endpoint, valid for 12h
     *
     * This is for the `Authorization` header.
     *
     * @return [String] "vapid t=$JWT,k=$PUBKEY"
     */
    private fun getVapidHeader(sub: String = "mailto"): String {
        val endpointStr = endpoint ?: return ""
        val header = JSONObject()
            .put("alg", "ES256")
            .put("typ", "JWT")
            .toString().toByteArray(Charsets.UTF_8).b64encode()
        val endpoint = URL(endpointStr)
        val exp = ((System.currentTimeMillis() / 1000) + 43200) // +12h

        /**
         * [org.json.JSONStringer#string] Doesn't follow RFC, '/' = 0x2F doesn't have to be escaped
         */
        val body = JSONObject()
            .put("aud", "${endpoint.protocol}://${endpoint.authority}")
            .put("exp", exp)
            .put("sub", sub)
            .toString().toByteArray(Charsets.UTF_8).b64encode()
        val toSign = "$header.$body".toByteArray(Charsets.UTF_8)
        val signature = sign(toSign)?.b64encode()
        val jwt = "$header.$body.$signature"
        return "vapid t=$jwt,k=$vapidPubKey"
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

    /**
     * Sign [data] using the generated VAPID key pair
     */
    private fun sign(data: ByteArray): ByteArray? {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
        if (!ks.containsAlias(ALIAS) || !ks.entryInstanceOf(ALIAS, PrivateKeyEntry::class.java)) {
            // This should never be called. When we sign something, the key are already created.
            genVapidKey()
        }
        val entry: KeyStore.Entry = ks.getEntry(ALIAS, null)
        if (entry !is PrivateKeyEntry) {
            Log.w(TAG, "Not an instance of a PrivateKeyEntry")
            return null
        }
        val signature = Signature.getInstance("SHA256withECDSA").run {
            initSign(entry.privateKey)
            update(data)
            sign()
        }.let { EllipticCurves.ecdsaDer2Ieee(it, 64) }
        return signature
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