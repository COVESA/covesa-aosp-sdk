package global.covesa.sdk.api.client.internal

import android.content.Context
import global.covesa.sdk.api.client.push.data.FailedReason
import global.covesa.sdk.api.client.push.data.PushEndpoint
import global.covesa.sdk.api.client.push.PushService
import org.unifiedpush.android.connector.FailedReason as UFailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import global.covesa.sdk.api.client.push.data.PushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.unifiedpush.android.connector.data.PushEndpoint as UPushEndpoint
import org.unifiedpush.android.connector.data.PushMessage as UPushMessage

/**
 * @hide
 *
 * Receive UnifiedPush events and forward to the implemented [PushService]
 */
class PushReceiver: MessagingReceiver() {

    override fun onUnregistered(context: Context, instance: String) {
        CoroutineScope(Dispatchers.IO).launch {
            InternalPushServiceClient(context).onUnregistered(instance)
        }
    }

    override fun onMessage(context: Context, message: UPushMessage, instance: String) {
        CoroutineScope(Dispatchers.IO).launch {
            InternalPushServiceClient(context).onMessage(PushMessage(message), instance)
        }
    }

    override fun onNewEndpoint(context: Context, endpoint: UPushEndpoint, instance: String) {
        CoroutineScope(Dispatchers.IO).launch {
            InternalPushServiceClient(context).onNewEndpoint(PushEndpoint(endpoint), instance)
        }
    }

    override fun onRegistrationFailed(context: Context, reason: UFailedReason, instance: String) {
        CoroutineScope(Dispatchers.IO).launch {
            InternalPushServiceClient(context).onRegistrationFailed(FailedReason.fromUp(reason), instance)
        }
    }
}
