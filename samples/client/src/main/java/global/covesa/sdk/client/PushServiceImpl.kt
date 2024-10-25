package global.covesa.sdk.client

import android.content.Context
import android.util.Log
import global.covesa.sdk.api.client.PushService
import global.covesa.sdk.client.ui.Notification
import org.greenrobot.eventbus.EventBus
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage

class PushServiceImpl: PushService() {
    override fun onNewEndpoint(context: Context, endpoint: PushEndpoint, instance: String) {
        MockApplicationServer(context).MockApi().storePushEndpoint(endpoint)
        EventBus.getDefault().post(PushSubscriptionEvent())
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
        EventBus.getDefault().post(PushSubscriptionEvent())
    }

    private companion object {
        const val TAG = "PushServiceImpl"
    }
}