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
import kotlinx.coroutines.cancel
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
        runSuspend {
            InternalPushServiceClient.getService(context).onUnregistered(instance)
        }
    }

    override fun onMessage(context: Context, message: UPushMessage, instance: String) {
        runSuspend {
            InternalPushServiceClient.getService(context).onMessage(PushMessage(message), instance)
        }
    }

    override fun onNewEndpoint(context: Context, endpoint: UPushEndpoint, instance: String) {
        runSuspend {
            InternalPushServiceClient.getService(context).onNewEndpoint(PushEndpoint(endpoint), instance)
        }
    }

    override fun onRegistrationFailed(context: Context, reason: UFailedReason, instance: String) {
        runSuspend {
            InternalPushServiceClient.getService(context).onRegistrationFailed(FailedReason.fromUp(reason), instance)
        }
    }

    private fun runSuspend(block: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            block()
            coroutineContext.cancel()
        }
    }
}
