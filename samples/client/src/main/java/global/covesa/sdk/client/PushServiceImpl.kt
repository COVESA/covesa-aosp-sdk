package global.covesa.sdk.client

import android.content.Context
import global.covesa.sdk.api.client.PushService
import org.greenrobot.eventbus.EventBus
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage

class PushServiceImpl: PushService() {
    override fun onNewEndpoint(context: Context, endpoint: PushEndpoint, instance: String) {
        EventBus.getDefault().post(PushSubscriptionEvent())
    }

    override fun onMessage(context: Context, message: PushMessage, instance: String) {
        TODO("Not yet implemented")
    }

    override fun onRegistrationFailed(context: Context, reason: FailedReason, instance: String) {
        TODO("Not yet implemented")
    }

    override fun onUnregistered(context: Context, instance: String) {
        EventBus.getDefault().post(PushSubscriptionEvent())
    }
}