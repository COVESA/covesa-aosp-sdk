package global.covesa.sdk.api.client.internal

import android.content.Context
import android.content.Intent
import android.util.Log
import global.covesa.sdk.api.client.push.FailedReason
import global.covesa.sdk.api.client.push.PushEndpoint
import global.covesa.sdk.api.client.push.PushService
import org.unifiedpush.android.connector.FailedReason as UFailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import global.covesa.sdk.api.client.push.PushMessage
import org.unifiedpush.android.connector.data.PushEndpoint as UPushEndpoint
import org.unifiedpush.android.connector.data.PushMessage as UPushMessage

/**
 * @hide
 *
 * Receive UnifiedPush events and forward to the implemented [PushService]
 */
class PushReceiver: MessagingReceiver() {
    override fun onUnregistered(context: Context, instance: String) {
        sendToService(context, instance, PushService.PushEventType.UNREGISTERED)
    }

    override fun onMessage(context: Context, message: UPushMessage, instance: String) {
        if (!message.decrypted) {
            Log.w(TAG, "Received a message that can't be decrypted.")
            return
        }
        sendToService(context, instance, PushService.PushEventType.MESSAGE) { intent ->
            intent.putExtra("message", PushMessage(message))
        }
    }

    override fun onNewEndpoint(context: Context, endpoint: UPushEndpoint, instance: String) {
        sendToService(context, instance, PushService.PushEventType.NEW_ENDPOINT) { intent ->
            intent.putExtra("endpoint", PushEndpoint(endpoint))
        }
    }

    override fun onRegistrationFailed(context: Context, reason: UFailedReason, instance: String) {
        sendToService(context, instance, PushService.PushEventType.NEW_ENDPOINT) { intent ->
            intent.putExtra("reason", FailedReason.fromUp(reason))
        }
    }

    private fun sendToService(context: Context, instance: String, type: PushService.PushEventType, processIntent: (Intent) -> Any = {}) {
        Intent().apply {
            `package` = context.packageName
            action = PushService.ACTION_PUSH_EVENT
            putExtra("instance", instance)
            putExtra("type", type)
            processIntent(this)
        }.also {
            context.startService(it)
        }
    }

    companion object {
        private const val TAG = "PushReceiver"
    }
}