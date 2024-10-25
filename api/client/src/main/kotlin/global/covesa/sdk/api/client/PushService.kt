package global.covesa.sdk.api.client

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage
import java.io.Serializable

/**
 * Service to receive UnifiedPush messages (new endpoints, unregistrations, push messages, errors) from the distributors
 *
 * You need to declare a service that extend [PushService]. This service must not be exported and
 * must handle the following action:
 *
 * ```xml
 * <service android:name=".PushServiceImpl"
 *     android:exported="false">
 *     <intent-filter>
 *         <action android:name="global.covesa.sdk.PUSH_EVENT"/>
 *     </intent-filter>
 * </service>
 * ```
 * You need to use [UnifiedPush][org.unifiedpush.android.connector.UnifiedPush] to register for push notifications.
 */
abstract class PushService: Service() {
    /** Type of Push Event */
    internal enum class PushEventType {
        REGISTRATION_FAILED,
        UNREGISTERED,
        NEW_ENDPOINT,
        MESSAGE;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ret: () -> Int = fun (): Int {
            return super.onStartCommand(intent, flags, startId)
        }
        val type = intent?.getSerializableExtraForT("type", PushEventType::class.java)
            ?: return ret()
        val instance = intent.getStringExtra("instance")
            ?: return ret()
        when (type) {
            PushEventType.REGISTRATION_FAILED -> {
                val reason = intent.getSerializableExtraForT("reason", FailedReason::class.java)
                    ?: return ret()
                onRegistrationFailed(this, reason, instance)
            }
            PushEventType.UNREGISTERED -> onUnregistered(this, instance)
            PushEventType.NEW_ENDPOINT -> {
                val endpoint = intent.getParcelableExtraForT("endpoint", PushEndpoint::class.java)
                    ?: return ret()
                onNewEndpoint(this, endpoint, instance)
            }
            PushEventType.MESSAGE -> {
                val message = intent.getParcelableExtraForT("message", PushMessage::class.java)
                    ?: return ret()
                onMessage(this, message, instance)
            }
        }
        return ret()
    }

    private fun <T : Serializable>Intent.getSerializableExtraForT(name: String, tClass: Class<T>): T? {
        return if (Build.VERSION.SDK_INT > 32) {
            this.getSerializableExtra(name, tClass)
        } else {
            this.getSerializableExtra(name) as T?
        }
    }

    private fun <T : Parcelable>Intent.getParcelableExtraForT(name: String, tClass: Class<T>): T? {
        return if (Build.VERSION.SDK_INT > 32) {
            this.getParcelableExtra(name, tClass)
        } else {
            this.getParcelableExtra(name) as T?
        }
    }

    /**
     * A new endpoint is to be used for sending push messages. The new endpoint
     * should be send to the application server, and the app should sync for
     * missing notifications.
     */
    abstract fun onNewEndpoint(context: Context, endpoint: PushEndpoint, instance: String)

    /**
     * A new message is received. The message contains the decrypted content of the push message
     * for the instance
     */
    abstract fun onMessage(context: Context, message: PushMessage, instance: String)

    /**
     * The registration is not possible, eg. no network, depending on the reason,
     * you can try to register again directly.
     */
    abstract fun onRegistrationFailed(context: Context, reason: FailedReason, instance: String)

    /**
     * This application is unregistered by the distributor from receiving push messages
     */
    abstract fun onUnregistered(context: Context, instance: String)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    internal companion object {
        const val ACTION_PUSH_EVENT = "global.covesa.sdk.PUSH_EVENT"
    }
}