package global.covesa.sdk.client

import android.util.Log
import global.covesa.sdk.api.client.push.FailedReason
import global.covesa.sdk.api.client.push.PushEndpoint
import global.covesa.sdk.api.client.push.PushMessage
import global.covesa.sdk.api.client.push.PushService
import global.covesa.sdk.client.ui.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushServiceImpl: PushService() {
    override fun onNewEndpoint(endpoint: PushEndpoint, instance: String) {
        MockApplicationServer(this).MockApi().storePushEndpoint(endpoint)
        publishEvent(true)
    }

    override fun onMessage(message: PushMessage, instance: String) {
        Log.d(TAG, "Received message: ${message.content.toString(Charsets.UTF_8)}")
        Notification(this).showNotification("COVESA client sample", message.content.toString(Charsets.UTF_8))
    }

    override fun onRegistrationFailed(reason: FailedReason, instance: String) {
        Notification(this).showNotification("Registration failed", "Can't register to the service: $reason")
    }

    override fun onUnregistered(instance: String) {
        MockApplicationServer(this).MockApi().storePushEndpoint(null)
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