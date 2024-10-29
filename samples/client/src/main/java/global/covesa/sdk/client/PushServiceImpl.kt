package global.covesa.sdk.client

import android.content.Context
import android.util.Log
import global.covesa.sdk.api.client.PushService
import global.covesa.sdk.client.ui.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage

class PushServiceImpl: PushService() {
    override fun onNewEndpoint(context: Context, endpoint: PushEndpoint, instance: String) {
        MockApplicationServer(context).MockApi().storePushEndpoint(endpoint)
        publishEvent(true)
    }

    override fun onMessage(context: Context, message: PushMessage, instance: String) {
        Log.d(TAG, "Received message: ${message.content.toString(Charsets.UTF_8)}")
        Notification(context).showNotification("COVESA client sample", message.content.toString(Charsets.UTF_8))
    }

    override fun onRegistrationFailed(context: Context, reason: FailedReason, instance: String) {
        Notification(context).showNotification("Registration failed", "Can't register to the service: $reason")
    }

    override fun onUnregistered(context: Context, instance: String) {
        MockApplicationServer(context).MockApi().storePushEndpoint(null)
        publishEvent(false)
    }

    /**
     * Update the UI
     */
    private fun publishEvent(registered: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            EventBus.publish(PushSubscriptionEvent(registered))
        }
    }

    private companion object {
        const val TAG = "PushServiceImpl"
    }
}