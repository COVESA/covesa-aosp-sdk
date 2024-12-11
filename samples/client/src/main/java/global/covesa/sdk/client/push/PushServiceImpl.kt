package global.covesa.sdk.client.push

import android.util.Log
import global.covesa.sdk.api.client.push.FailedReason
import global.covesa.sdk.api.client.push.PushEndpoint
import global.covesa.sdk.api.client.push.PushMessage
import global.covesa.sdk.api.client.push.PushService
import global.covesa.sdk.client.ui.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PushServiceImpl: PushService() {
    class NewRegistrationState(val registered: Boolean)

    override fun onNewEndpoint(endpoint: PushEndpoint, instance: String) {
        MockApplicationServer(this).MockApi().storePushEndpoint(endpoint)
        updateRegistrationState(true)
    }

    override fun onMessage(message: PushMessage, instance: String) {
        Log.d(TAG, "Received message: ${message.content.toString(Charsets.UTF_8)}")
        Notification(this).showNotification("COVESA client sample", message.content.toString(Charsets.UTF_8))
    }

    override fun onRegistrationFailed(reason: FailedReason, instance: String) {
        Log.d(TAG, "Registration failed: $reason")
        Notification(this).showNotification("Registration failed", "Can't register to the service: $reason")
    }

    override fun onUnregistered(instance: String) {
        MockApplicationServer(this).MockApi().storePushEndpoint(null)
        updateRegistrationState(false)
    }

    /**
     * Update the UI
     */
    private fun updateRegistrationState(registered: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            _events.emit(NewRegistrationState(registered))
        }
    }

    companion object {
        private const val TAG = "PushServiceImpl"
        private val _events = MutableSharedFlow<NewRegistrationState>()
        val events = _events.asSharedFlow()
    }
}