package global.covesa.sdk.api.client.internal

import android.content.Context
import android.content.Intent
import android.os.IBinder
import global.covesa.sdk.api.client.CovesaServiceClient
import global.covesa.sdk.api.client.push.PushService
import global.covesa.sdk.api.client.push.data.FailedReason
import global.covesa.sdk.api.client.push.data.PushEndpoint
import global.covesa.sdk.api.client.push.data.PushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

/**
 * This is used to connect to the service implementing [PushService]
 */
internal class InternalPushServiceClient(
    context: Context,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CovesaServiceClient<PushService.PushBinder>(context, coroutineScope, TAG) {

    private val packageName = context.packageName

    override fun interfaceFromBinder(service: IBinder): PushService.PushBinder {
        return service as PushService.PushBinder
    }

    override fun serviceIntent(): Intent {
        return Intent().apply {
            action = PushService.ACTION_PUSH_EVENT
            `package` = packageName
        }
    }

    suspend fun newEndpoint(endpoint: PushEndpoint, instance: String) {
        getService()?.onNewEndpoint(endpoint, instance)
    }

    suspend fun message(message: PushMessage, instance: String) {
        getService()?.onMessage(message, instance)
    }

    suspend fun registrationFailed(reason: FailedReason, instance: String) {
        getService()?.onRegistrationFailed(reason, instance)
    }

    suspend fun unregistered(instance: String) {
        getService()?.onUnregistered(instance)
    }

    private suspend fun getService(): PushService? {
        return remoteService.first()?.getService()
    }

    companion object {
        private const val TAG = "InternalPushServiceClient"
    }
}