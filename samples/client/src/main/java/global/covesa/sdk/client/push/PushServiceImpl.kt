package global.covesa.sdk.client.push

import android.util.Log
import global.covesa.sdk.api.client.push.PushService
import global.covesa.sdk.api.client.push.data.FailedReason
import global.covesa.sdk.api.client.push.data.PushEndpoint
import global.covesa.sdk.api.client.push.data.PushMessage
import global.covesa.sdk.client.ui.Notification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PushServiceImpl: PushService() {
    class NewRegistrationState(val registered: Boolean)

    override suspend fun onNewEndpoint(endpoint: PushEndpoint, instance: String) {
        Log.d(TAG, "OnNewEndpoint: ${endpoint.url}")
        FakeApplicationServer(this).MockApi().storePushEndpoint(endpoint)
        updateRegistrationState(true)
    }

    override suspend fun onMessage(message: PushMessage, instance: String) {
        Log.d(TAG, "Received message: ${message.content.toString(Charsets.UTF_8)}")
        Notification(this).showNotification("COVESA client sample", message.content.toString(Charsets.UTF_8))
        updateRegistrationState(true)
    }

    override suspend fun onRegistrationFailed(reason: FailedReason, instance: String) {
        Log.d(TAG, "Registration failed: $reason")
        Notification(this).showNotification("Registration failed", "Can't register to the service: $reason")
        updateRegistrationState(false)
    }

    override suspend fun onUnregistered(instance: String) {
        FakeApplicationServer(this).MockApi().storePushEndpoint(null)
        updateRegistrationState(false)
    }

    /**
     * Update the UI
     */
    private suspend fun updateRegistrationState(registered: Boolean) {
        _events.emit(NewRegistrationState(registered))
    }

    companion object {
        private const val TAG = "PushServiceImpl"
        private val _events = MutableSharedFlow<NewRegistrationState>()
        val events = _events.asSharedFlow()
    }
}