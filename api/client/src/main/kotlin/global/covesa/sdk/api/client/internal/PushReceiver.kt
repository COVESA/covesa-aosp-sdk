package global.covesa.sdk.api.client.internal

import android.content.Context
import android.content.Intent
import android.util.Log
import global.covesa.sdk.api.client.PushService
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage

/**
 * @hide
 *
 * Receive UnifiedPush events and forward to the implemented [PushService]
 */
class PushReceiver: MessagingReceiver() {
    override fun onUnregistered(context: Context, instance: String) {
        sendToService(context, instance, PushService.PushEventType.UNREGISTERED)
    }

    override fun onMessage(context: Context, message: PushMessage, instance: String) {
        if (!message.decrypted) {
            Log.w(TAG, "Received a message that can't be decrypted.")
            return
        }
        sendToService(context, instance, PushService.PushEventType.MESSAGE) { intent ->
            intent.putExtra("message", message)
        }
    }

    override fun onNewEndpoint(context: Context, endpoint:PushEndpoint, instance: String) {
        sendToService(context, instance, PushService.PushEventType.NEW_ENDPOINT) { intent ->
            intent.putExtra("endpoint", endpoint)
        }
    }

    override fun onRegistrationFailed(context: Context, reason: FailedReason, instance: String) {
        sendToService(context, instance, PushService.PushEventType.NEW_ENDPOINT) { intent ->
            intent.putExtra("reason", reason)
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