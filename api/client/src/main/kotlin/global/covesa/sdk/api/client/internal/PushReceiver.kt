package global.covesa.sdk.api.client.internal

import android.content.Context
import global.covesa.sdk.api.client.push.FailedReason
import global.covesa.sdk.api.client.push.PushEndpoint
import global.covesa.sdk.api.client.push.PushService
import org.unifiedpush.android.connector.FailedReason as UFailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import global.covesa.sdk.api.client.push.PushMessage
import global.covesa.sdk.api.client.push.ServiceConnection
import org.unifiedpush.android.connector.data.PushEndpoint as UPushEndpoint
import org.unifiedpush.android.connector.data.PushMessage as UPushMessage

/**
 * @hide
 *
 * Receive UnifiedPush events and forward to the implemented [PushService]
 */
class PushReceiver: MessagingReceiver() {

    override fun onUnregistered(context: Context, instance: String) {
        ServiceConnection.sendEvent(
            context,
            ServiceConnection.Event.Unregistered(instance)
        )
    }

    override fun onMessage(context: Context, message: UPushMessage, instance: String) {
        ServiceConnection.sendEvent(
            context,
            ServiceConnection.Event.Message(PushMessage(message), instance)
        )
    }

    override fun onNewEndpoint(context: Context, endpoint: UPushEndpoint, instance: String) {
        ServiceConnection.sendEvent(
            context,
            ServiceConnection.Event.NewEndpoint(PushEndpoint(endpoint), instance)
        )
    }

    override fun onRegistrationFailed(context: Context, reason: UFailedReason, instance: String) {
        ServiceConnection.sendEvent(
            context,
            ServiceConnection.Event.RegistrationFailed(FailedReason.fromUp(reason), instance)
        )
    }
}
